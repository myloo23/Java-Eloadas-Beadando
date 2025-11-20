package hu.uni.neumann.forexapp.controllers;

import hu.uni.neumann.forexapp.service.MnbParser;
import hu.uni.neumann.forexapp.service.MnbService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/soap")
@RequiredArgsConstructor
public class MnbController {

    private final MnbService mnbService;
    private final MnbParser parser = new MnbParser();

    @GetMapping
    public String page(Model model) {
        model.addAttribute("currencies", List.of("EUR","USD","CHF","GBP"));
        return "soap";
    }

    @ResponseBody
    @GetMapping("/data")
    public List<MnbParser.RatePoint> data(
            @RequestParam String currency,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        String xml = mnbService.getExchangeRatesXml(currency, from.toString(), to.toString());
        return parser.parse(xml);
    }
}
