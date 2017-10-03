package no.fint.consumer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.google.common.collect.ImmutableMap;
import no.fint.consumer.utils.RestEndpoints;
import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.administrasjon.personal.Personalressurs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Map;

@Configuration
public class Config {

    @Value("${server.context-path:}")
    private String contextPath;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        objectMapper.setDateFormat(new ISO8601DateFormat());
    }

    @Qualifier("linkMapper")
    @Bean
    public Map<String, String> linkMapper() {
        return ImmutableMap.of(
                Organisasjonselement.class.getName(), fullPath(RestEndpoints.ORGANISASJONSELEMENT),
                Personalressurs.class.getName(), "/administrasjon/personal/personalressurs"
        );
    }

    String fullPath(String path) {
        return String.format("%s%s", contextPath, path);
    }

}