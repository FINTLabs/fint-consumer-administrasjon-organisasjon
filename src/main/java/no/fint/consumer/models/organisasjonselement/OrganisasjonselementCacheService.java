package no.fint.consumer.models.organisasjonselement;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import no.fint.cache.CacheService;
import no.fint.consumer.config.Constants;
import no.fint.consumer.config.ConsumerProps;
import no.fint.consumer.event.ConsumerEventUtil;
import no.fint.event.model.Event;
import no.fint.model.relation.FintResource;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.administrasjon.organisasjon.OrganisasjonActions;

@Slf4j
@Service
public class OrganisasjonselementCacheService extends CacheService<FintResource<Organisasjonselement>> {

    public static final String MODEL = Organisasjonselement.class.getSimpleName().toLowerCase();

    @Autowired
    private ConsumerEventUtil consumerEventUtil;

    @Autowired
    private ConsumerProps props;

    public OrganisasjonselementCacheService() {
        super(MODEL, OrganisasjonActions.GET_ALL_ORGANISASJONSELEMENT);
    }

    @PostConstruct
    public void init() {
        Arrays.stream(props.getOrgs()).forEach(this::createCache);
    }

    @Scheduled(initialDelayString = ConsumerProps.CACHE_INITIALDELAY_ORGANISASJONSELEMENT, fixedRateString = ConsumerProps.CACHE_FIXEDRATE_ORGANISASJONSELEMENT)
    public void populateCacheAll() {
        Arrays.stream(props.getOrgs()).forEach(this::populateCache);
    }

    public void rebuildCache(String orgId) {
		flush(orgId);
		populateCache(orgId);
	}

    private void populateCache(String orgId) {
		log.info("Populating Organisasjonselement cache for {}", orgId);
        Event event = new Event(orgId, Constants.COMPONENT, OrganisasjonActions.GET_ALL_ORGANISASJONSELEMENT, Constants.CACHE_SERVICE);
        consumerEventUtil.send(event);
    }


    public Optional<FintResource<Organisasjonselement>> getOrganisasjonselementByOrganisasjonsId(String orgId, String organisasjonsId) {
        return getOne(orgId, (fintResource) -> Optional
                .ofNullable(fintResource)
                .map(FintResource::getResource)
                .map(Organisasjonselement::getOrganisasjonsId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(id -> id.equals(organisasjonsId))
                .orElse(false));
    }

    public Optional<FintResource<Organisasjonselement>> getOrganisasjonselementByOrganisasjonsKode(String orgId, String organisasjonsKode) {
        return getOne(orgId, (fintResource) -> Optional
                .ofNullable(fintResource)
                .map(FintResource::getResource)
                .map(Organisasjonselement::getOrganisasjonsKode)
                .map(Identifikator::getIdentifikatorverdi)
                .map(id -> id.equals(organisasjonsKode))
                .orElse(false));
    }

    public Optional<FintResource<Organisasjonselement>> getOrganisasjonselementByOrganisasjonsnummer(String orgId, String organisasjonsnummer) {
        return getOne(orgId, (fintResource) -> Optional
                .ofNullable(fintResource)
                .map(FintResource::getResource)
                .map(Organisasjonselement::getOrganisasjonsnummer)
                .map(Identifikator::getIdentifikatorverdi)
                .map(id -> id.equals(organisasjonsnummer))
                .orElse(false));
    }


	@Override
    public void onAction(Event event) {
        update(event, new TypeReference<List<FintResource<Organisasjonselement>>>() {
        });
    }
}
