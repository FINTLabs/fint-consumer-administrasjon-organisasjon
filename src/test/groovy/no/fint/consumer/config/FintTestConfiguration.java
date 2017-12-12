package no.fint.consumer.config;

import no.fint.audit.FintAuditService;
import no.fint.consumer.event.ConsumerEventUtil;
import no.fint.events.FintEvents;
import no.fint.events.FintEventsHealth;
import no.fint.events.config.FintEventsProps;
import no.fint.events.config.RedissonConfig;
import no.fint.events.queue.FintEventsQueue;
import no.fint.events.scheduling.FintEventsScheduling;
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
    FintAuditService fintAuditService() {
        return mockFactory.Mock(FintAuditService.class);
    }

    @Bean
    FintEventsHealth fintEventsHealth() {
        return mockFactory.Mock(FintEventsHealth.class);
    }

    @Bean
    FintEventsQueue fintEventsQueue() {
        return mockFactory.Mock(FintEventsQueue.class);
    }

    @Bean
    RedissonConfig redissonConfig() {
        return mockFactory.Mock(RedissonConfig.class);
    }

    @Bean
    FintEventsProps fintEventsProps() {
        return mockFactory.Mock(FintEventsProps.class);
    }

    @Bean
    FintEventsScheduling fintEventsScheduling() {
        return mockFactory.Mock(FintEventsScheduling.class);
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
