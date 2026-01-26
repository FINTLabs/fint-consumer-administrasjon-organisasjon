package no.fint.consumer.config;

import no.fint.audit.FintAuditService;
import no.fint.cache.CacheManager;
import no.fint.cache.FintCacheManager;
import no.fint.consumer.event.ConsumerEventUtil;
import no.fint.events.FintEvents;
import no.fint.relations.FintResourceCompatibility;
import no.fint.relations.config.FintRelationsProps;
import no.fint.relations.internal.FintLinkMapper;
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
    FintLinkMapper fintLinkMapper() {
        return mockFactory.Mock(FintLinkMapper.class);
    }

    @Bean
    FintRelationsProps fintRelationsProps() {
        return mockFactory.Mock(FintRelationsProps.class);
    }

    @Bean
    FintResourceCompatibility fintResourceCompatibility() {
        return mockFactory.Mock(FintResourceCompatibility.class);
    }

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
