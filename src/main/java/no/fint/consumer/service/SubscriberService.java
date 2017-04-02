package no.fint.consumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fint.consumer.event.EventActions;
import no.fint.consumer.organisasjonselement.OrganisasjonselementCacheService;
import no.fint.consumer.utils.CacheUri;
import no.fint.event.model.Event;
import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.relation.FintResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SubscriberService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganisasjonselementCacheService cacheService;

    public void recieve(Event event) {
        log.info("Event: {}", event.getAction());
        try {
            EventActions action = EventActions.valueOf(event.getAction());

            if (action == EventActions.GET_ALL_ORGANISASJONSELEMENT) {
                List<?> organisasjonselements = event.getData();
                List<FintResource> convertedList = organisasjonselements.stream().map(organisasjonselement -> objectMapper.convertValue(organisasjonselement, FintResource.class)).collect(Collectors.toList());
                List<FintResource<Organisasjonselement>> organisasjonselementList = mapFintResource(Organisasjonselement.class, convertedList);
                cacheService.getCache(CacheUri.create(event.getOrgId(), "organisasjonselement")).ifPresent(cache -> cache.update(organisasjonselementList));
            } else {
                log.warn("Unhandled event: {}", event.getAction());
            }
        } catch (IllegalArgumentException e) {
            log.error("Unhandled event: " + event.getAction(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<FintResource<T>> mapFintResource(Class<T> type, List<FintResource> fintResources) {
        List<FintResource<T>> resources = new ArrayList<>();
        for (FintResource fintResource : fintResources) {
            FintResource<T> resource = new FintResource<>(type, (T) fintResource.getResource());
            resource.setRelasjoner(fintResource.getRelasjoner());
            resources.add(resource);
        }
        return resources;
    }

}
