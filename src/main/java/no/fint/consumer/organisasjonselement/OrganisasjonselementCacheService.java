package no.fint.consumer.organisasjonselement;

import lombok.extern.slf4j.Slf4j;
import no.fint.cache.FintCache;
import no.fint.consumer.CacheService;
import no.fint.consumer.event.Actions;
import no.fint.consumer.event.ConsumerEventUtil;
import no.fint.consumer.utils.CacheUri;
import no.fint.event.model.Event;
import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.relation.FintResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@Slf4j
@Service
public class OrganisasjonselementCacheService extends CacheService<FintResource<Organisasjonselement>> {

    @Autowired
    private ConsumerEventUtil consumerEventUtil;

    @Value("${fint.events.orgsIds:mock.no}")
    private String[] orgs;

    @PostConstruct
    public void init() {
        Arrays.stream(orgs).forEach(orgId -> {
            FintCache<FintResource<Organisasjonselement>> cache = new FintCache<>();
            String cacheUri = CacheUri.create(orgId, "organisasjonselement");
            caches.put(cacheUri, cache);
        });
    }

    @Scheduled(initialDelayString = "${fint.consumer.cache.initialDelay.organisasjonselement:40000}", fixedRateString = "${fint.consumer.cache.fixedRate.organisasjonselement:55000}")
    public void getAllPersons() {
        Arrays.stream(orgs).forEach(orgId -> {
            log.info("Populating person cache for {}", orgId);
            Event event = new Event(orgId, "administrasjon/organisasjon", Actions.GET_ALL_ORGANISASJONSELEMENT.name(), "CACHE_SERVICE");
            consumerEventUtil.send(event);
        });
    }
}
