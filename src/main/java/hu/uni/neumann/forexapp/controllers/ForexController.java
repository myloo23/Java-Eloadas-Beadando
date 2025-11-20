package hu.uni.neumann.forexapp.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import hu.uni.neumann.forexapp.service.OandaClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/forex")
@RequiredArgsConstructor
public class ForexController {

    private final OandaClient oandaClient;

    @ModelAttribute("instruments")
    public List<String> instruments() {
        return List.of("EUR_USD", "GBP_USD", "USD_JPY");
    }

    @ModelAttribute("granularities")
    public List<String> granularities() {
        return List.of("D", "H1", "M15");
    }

    // 4. Forex-account menü: számlainformációk
    @GetMapping("/account")
    public String account(Model model) {
        JsonNode account = oandaClient.getAccount();
        model.addAttribute("account", account);
        return "forex-account";
    }

    // 5. Forex-AktÁr menü: aktuális ár
    @GetMapping("/price")
    public String pricePage() {
        return "forex-price";
    }

    @ResponseBody
    @GetMapping("/price/data")
    public JsonNode price(@RequestParam String instrument) {
        return oandaClient.getPrice(instrument);
    }

    // 6. Forex-HistÁr menü: 10 historikus ár
    @GetMapping("/hist")
    public String histPage() {
        return "forex-hist";
    }

    @ResponseBody
    @GetMapping("/hist/data")
    public JsonNode hist(@RequestParam String instrument,
                         @RequestParam String granularity) {
        return oandaClient.getCandles(instrument, granularity, 10);
    }

    // 7. Forex-Nyit menü: pozíció nyitása
    @GetMapping("/open")
    public String openPage() {
        return "forex-open";
    }

    @ResponseBody
    @PostMapping("/open")
    public JsonNode open(@RequestParam String instrument,
                         @RequestParam long units) {
        return oandaClient.openMarketOrder(instrument, units);
    }

    // 8. Forex-Poz menü: nyitott pozíciók táblázat
    @GetMapping("/positions")
    public String positions(Model model) {
        JsonNode trades = oandaClient.listOpenTrades();
        model.addAttribute("trades", trades);
        return "forex-positions";
    }

    // 9. Forex-Zár menü: tradeId alapján zárás
    @GetMapping("/close")
    public String closePage(Model model) {
        JsonNode trades = oandaClient.listOpenTrades();
        model.addAttribute("trades", trades);
        return "forex-close";
    }

    @ResponseBody
    @PostMapping("/close")
    public JsonNode close(@RequestParam String tradeId) {
        return oandaClient.closeTrade(tradeId);
    }
}
