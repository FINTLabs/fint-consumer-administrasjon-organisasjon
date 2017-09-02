package no.fint.consumer.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import no.fint.consumer.organisasjonselement.OrganisasjonselementCacheService;
import no.fint.event.model.Event;
import no.fint.event.model.EventUtil;
import no.fint.events.annotations.FintEventListener;
import no.fint.events.queue.QueueType;
import no.fint.model.administrasjon.organisasjon.OrganisasjonActions;
import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.relation.FintResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SubscriberService {

    @Autowired
    private OrganisasjonselementCacheService cacheService;

    @FintEventListener(type = QueueType.UPSTREAM)
    public void recieve(Event event) {
        log.debug("Received event: {}", event);
        try {
            String action = event.getAction();
            if (action.equals(OrganisasjonActions.GET_ALL_ORGANISASJONSELEMENT.name())) {
                List<FintResource<Organisasjonselement>> organisasjonselementList = EventUtil.convertEventData(event, new TypeReference<List<FintResource<Organisasjonselement>>>() {
                });
                cacheService.getCache(event.getOrgId()).ifPresent(cache -> cache.update(organisasjonselementList));
            } else {
                log.warn("Unhandled event: {}", event.getAction());
            }
        } catch (IllegalArgumentException e) {
            log.error("Unhandled event: " + event.getAction(), e);
        }
    }
}
