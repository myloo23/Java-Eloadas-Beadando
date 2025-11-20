package hu.uni.neumann.forexapp.service;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MnbParser {

    public record RatePoint(LocalDate date, String currency, double value) {}

    public List<RatePoint> parse(String xml) {
        List<RatePoint> out = new ArrayList<>();
        try {
            var db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            var doc = db.parse(new InputSource(new StringReader(xml)));

            NodeList days = doc.getElementsByTagName("Day");
            for (int i = 0; i < days.getLength(); i++) {
                Element day = (Element) days.item(i);
                String date = day.getAttribute("date");

                NodeList rates = day.getElementsByTagName("Rate");
                for (int j = 0; j < rates.getLength(); j++) {
                    Element r = (Element) rates.item(j);
                    String curr = r.getAttribute("curr");
                    String txt = r.getTextContent().trim().replace(",", ".");
                    out.add(new RatePoint(LocalDate.parse(date), curr, Double.parseDouble(txt)));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("MNB XML feldolgozási hiba", e);
        }
        out.sort(Comparator.comparing(RatePoint::date));
        return out;
    }
}
