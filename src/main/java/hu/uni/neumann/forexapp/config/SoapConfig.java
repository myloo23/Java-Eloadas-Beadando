package hu.uni.neumann.forexapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.client.core.WebServiceTemplate;

@Configuration
public class SoapConfig {
    @Bean
    public WebServiceTemplate webServiceTemplate() {
        WebServiceTemplate t = new WebServiceTemplate();
        // HTTPS helyett HTTP
        t.setDefaultUri("http://www.mnb.hu/arfolyamok.asmx");
        return t;
    }
}
