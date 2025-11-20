package hu.uni.neumann.forexapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

@Service
@RequiredArgsConstructor
public class MnbService {

    private final WebServiceTemplate ws;

    public String getExchangeRatesXml(String currency, String from, String to) {
        // csak a body payload megy be
        String payload =
                """
                <GetExchangeRates xmlns="http://www.mnb.hu/webservices/">
                  <startDate>%s</startDate>
                  <endDate>%s</endDate>
                  <currencyNames>%s</currencyNames>
                </GetExchangeRates>
                """.formatted(from, to, currency);

        var req = new StringSource(payload);
        var res = new StringResult();

        ws.sendSourceAndReceiveToResult(req, message ->
                        ((SoapMessage) message).setSoapAction("http://www.mnb.hu/webservices/GetExchangeRates"),
                res);

        // A SOAP válaszban a Day/Rate XML egy szövegmezőben jön
        // Ezt kiemeljük a GetExchangeRatesResult elemből
        String soapXml = res.toString();
        return extractInnerMnbXml(soapXml);
    }

    private String extractInnerMnbXml(String soapXml) {
        try {
            var db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(soapXml)));
            // névteret nem erőltetünk, egyszerű tag-név keresés
            NodeList list = doc.getElementsByTagName("GetExchangeRatesResult");
            if (list.getLength() == 0) {
                // néha prefix-szel jön
                list = doc.getElementsByTagNameNS("http://www.mnb.hu/webservices/", "GetExchangeRatesResult");
            }
            if (list.getLength() > 0) {
                String inner = list.item(0).getTextContent();
                // ez már egy teljes XML, ezt adjuk vissza a parsernek
                return inner;
            }
            throw new RuntimeException("Nincs GetExchangeRatesResult elem a válaszban");
        } catch (Exception e) {
            throw new RuntimeException("MNB SOAP válasz bontási hiba", e);
        }
    }
}
