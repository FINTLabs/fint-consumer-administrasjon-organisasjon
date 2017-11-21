package no.fint.consumer.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ConsumerProps {

    public static final String CACHE_INITIALDELAY_ORGANISASJONSELEMENT = "${fint.consumer.cache.initialDelay.organisasjonselement:40000}";
    public static final String CACHE_FIXEDRATE_ORGANISASJONSELEMENT = "${fint.consumer.cache.fixedRate.organisasjonselement:900000}";

    @Value("${fint.events.orgIds:mock.no}")
    private String[] orgs;

}
