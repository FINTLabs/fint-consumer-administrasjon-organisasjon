package no.fint.consumer.config;

import no.fint.audit.FintAuditService;
import no.fint.cache.CacheManager;
import no.fint.cache.FintCacheManager;
import no.fint.consumer.event.ConsumerEventUtil;
import no.fint.events.FintEvents;
import org.springframework.context.annotation.Bean;
import spock.mock.DetachedMockFactory;
import spock.mock.MockFactory;

public class FintTestConfiguration {

    MockFactory mockFactory = new DetachedMockFactory();

    @Bean
    ConsumerProps consumerProps() {
        return new ConsumerProps();
    }

    // TODO: Possible to avoid declaring all of these??

    @Bean
    CacheManager cacheManager() {
        return new FintCacheManager();
    }

    @Bean
    FintAuditService fintAuditService() {
        return mockFactory.Mock(FintAuditService.class);
    }

    @Bean
    FintEvents fintEvents() {
        return mockFactory.Mock(FintEvents.class);
    }

    @Bean
    ConsumerEventUtil consumerEventUtil() {
        return mockFactory.Mock(ConsumerEventUtil.class);
    }

}
