package no.fint.consumer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.google.common.collect.ImmutableMap;
import com.hazelcast.config.*;
import no.fint.cache.CacheManager;
import no.fint.cache.FintCacheManager;
import no.fint.cache.HazelcastCacheManager;
import no.fint.consumer.utils.RestEndpoints;
import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.administrasjon.personal.Arbeidsforhold;
import no.fint.model.administrasjon.personal.Personalressurs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Map;

@Configuration
public class Config {

    @Value("${server.context-path:}")
    private String contextPath;

    @Value("${fint.consumer.cache-manager:default}")
    private String cacheManagerType;

    @Bean
    public CacheManager<?> cacheManager() {
        switch (cacheManagerType.toUpperCase()) {
            case "HAZELCAST":
                return new HazelcastCacheManager<>();
            default:
                return new FintCacheManager<>();
        }
    }

    @Value("${fint.hazelcast.members}")
    private String members;

    @Bean
    public com.hazelcast.config.Config hazelcastConfig() {
        com.hazelcast.config.Config cfg = new ClasspathXmlConfig("fint-hazelcast.xml");
        return cfg.setNetworkConfig(new NetworkConfig().setJoin(new JoinConfig().setTcpIpConfig(new TcpIpConfig().setMembers(Arrays.asList(members.split(","))).setEnabled(true)).setMulticastConfig(new MulticastConfig().setEnabled(false))));
    }

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
                Personalressurs.class.getName(), "/administrasjon/personal/personalressurs",
                Arbeidsforhold.class.getName(), "/administrasjon/personal/arbeidsforhold"
        );
    }

    String fullPath(String path) {
        return String.format("%s%s", contextPath, path);
    }

}