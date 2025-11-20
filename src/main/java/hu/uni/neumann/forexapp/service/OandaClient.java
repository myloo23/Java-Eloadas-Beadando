package hu.uni.neumann.forexapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class OandaClient {

    @Value("${oanda.apiUrl}")
    private String apiUrl;

    @Value("${oanda.accountId}")
    private String accountId;

    @Value("${oanda.token}")
    private String token;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private RestTemplate rest() {
        return new RestTemplate();
    }

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    private JsonNode toJson(String body) {
        try {
            return objectMapper.readTree(body);
        } catch (Exception e) {
            throw new RuntimeException("Nem sikerült feldolgozni az OANDA választ", e);
        }
    }

    private JsonNode errorJson(String where, Exception e) {
        try {
            String msg = e.getMessage();
            return objectMapper.readTree("""
              {
                "error": "%s",
                "location": "%s"
              }
              """.formatted(msg.replace("\"", "'"), where));
        } catch (Exception ex) {
            throw new RuntimeException(e);
        }
    }

    public JsonNode getAccount() {
        String url = apiUrl + "/v3/accounts/" + accountId;
        try {
            HttpEntity<String> req = new HttpEntity<>(headers());
            ResponseEntity<String> res = rest().exchange(url, HttpMethod.GET, req, String.class);
            return toJson(res.getBody());
        } catch (RestClientResponseException e) {
            return errorJson("getAccount " + url + " status " + e.getRawStatusCode(), e);
        } catch (Exception e) {
            return errorJson("getAccount " + url, e);
        }
    }

    public JsonNode getPrice(String instrument) {
        String url = apiUrl + "/v3/accounts/" + accountId + "/pricing?instruments=" + instrument;
        try {
            HttpEntity<String> req = new HttpEntity<>(headers());
            ResponseEntity<String> res = rest().exchange(url, HttpMethod.GET, req, String.class);
            return toJson(res.getBody());
        } catch (RestClientResponseException e) {
            return errorJson("getPrice " + url + " status " + e.getRawStatusCode(), e);
        } catch (Exception e) {
            return errorJson("getPrice " + url, e);
        }
    }

    public JsonNode getCandles(String instrument, String granularity, int count) {
        String url = apiUrl + "/v3/instruments/" + instrument + "/candles"
                + "?granularity=" + granularity
                + "&count=" + count;
        try {
            HttpEntity<String> req = new HttpEntity<>(headers());
            ResponseEntity<String> res = rest().exchange(url, HttpMethod.GET, req, String.class);
            return toJson(res.getBody());
        } catch (RestClientResponseException e) {
            return errorJson("getCandles " + url + " status " + e.getRawStatusCode(), e);
        } catch (Exception e) {
            return errorJson("getCandles " + url, e);
        }
    }

    public JsonNode openMarketOrder(String instrument, long units) {
        String url = apiUrl + "/v3/accounts/" + accountId + "/orders";
        String body = """
            {
              "order": {
                "units": "%d",
                "instrument": "%s",
                "timeInForce": "FOK",
                "type": "MARKET",
                "positionFill": "DEFAULT"
              }
            }
            """.formatted(units, instrument);
        try {
            HttpEntity<String> req = new HttpEntity<>(body, headers());
            ResponseEntity<String> res = rest().exchange(url, HttpMethod.POST, req, String.class);
            return toJson(res.getBody());
        } catch (RestClientResponseException e) {
            return errorJson("openMarketOrder " + url + " status " + e.getRawStatusCode(), e);
        } catch (Exception e) {
            return errorJson("openMarketOrder " + url, e);
        }
    }

    public JsonNode listOpenTrades() {
        String url = apiUrl + "/v3/accounts/" + accountId + "/openTrades";
        try {
            HttpEntity<String> req = new HttpEntity<>(headers());
            ResponseEntity<String> res = rest().exchange(url, HttpMethod.GET, req, String.class);
            return toJson(res.getBody());
        } catch (RestClientResponseException e) {
            return errorJson("listOpenTrades " + url + " status " + e.getRawStatusCode(), e);
        } catch (Exception e) {
            return errorJson("listOpenTrades " + url, e);
        }
    }

    public JsonNode closeTrade(String tradeId) {
        String url = apiUrl + "/v3/accounts/" + accountId + "/trades/" + tradeId + "/close";
        try {
            HttpEntity<String> req = new HttpEntity<>("", headers());
            ResponseEntity<String> res = rest().exchange(url, HttpMethod.PUT, req, String.class);
            return toJson(res.getBody());
        } catch (RestClientResponseException e) {
            return errorJson("closeTrade " + url + " status " + e.getRawStatusCode(), e);
        } catch (Exception e) {
            return errorJson("closeTrade " + url, e);
        }
    }
}
