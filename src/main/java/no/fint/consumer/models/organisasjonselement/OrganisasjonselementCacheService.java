package no.fint.consumer.models.organisasjonselement;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

import no.fint.cache.CacheService;
import no.fint.consumer.config.Constants;
import no.fint.consumer.config.ConsumerProps;
import no.fint.consumer.event.ConsumerEventUtil;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.relations.FintResourceCompatibility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.administrasjon.organisasjon.OrganisasjonActions;

@Slf4j
@Service
public class OrganisasjonselementCacheService extends CacheService<OrganisasjonselementResource> {

    public static final String MODEL = Organisasjonselement.class.getSimpleName().toLowerCase();

    @Value("${fint.consumer.compatibility.fintresource:true}")
    private boolean checkFintResourceCompatibility;

    @Autowired
    private FintResourceCompatibility fintResourceCompatibility;

    @Autowired
    private ConsumerEventUtil consumerEventUtil;

    @Autowired
    private ConsumerProps props;

    @Autowired
    private OrganisasjonselementLinker linker;

    private JavaType javaType;

    private ObjectMapper objectMapper;

    public OrganisasjonselementCacheService() {
        super(MODEL, OrganisasjonActions.GET_ALL_ORGANISASJONSELEMENT, OrganisasjonActions.UPDATE_ORGANISASJONSELEMENT);
        objectMapper = new ObjectMapper();
        javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, OrganisasjonselementResource.class);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @PostConstruct
    public void init() {
        props.getAssets().forEach(this::createCache);
    }

    @Scheduled(initialDelayString = Constants.CACHE_INITIALDELAY_ORGANISASJONSELEMENT, fixedRateString = Constants.CACHE_FIXEDRATE_ORGANISASJONSELEMENT)
    public void populateCacheAll() {
        props.getAssets().forEach(this::populateCache);
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


    public Optional<OrganisasjonselementResource> getOrganisasjonselementByOrganisasjonsId(String orgId, String organisasjonsId) {
        return getOne(orgId, (resource) -> Optional
                .ofNullable(resource)
                .map(OrganisasjonselementResource::getOrganisasjonsId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(_id -> _id.equals(organisasjonsId))
                .orElse(false));
    }

    public Optional<OrganisasjonselementResource> getOrganisasjonselementByOrganisasjonsKode(String orgId, String organisasjonsKode) {
        return getOne(orgId, (resource) -> Optional
                .ofNullable(resource)
                .map(OrganisasjonselementResource::getOrganisasjonsKode)
                .map(Identifikator::getIdentifikatorverdi)
                .map(_id -> _id.equals(organisasjonsKode))
                .orElse(false));
    }

    public Optional<OrganisasjonselementResource> getOrganisasjonselementByOrganisasjonsnummer(String orgId, String organisasjonsnummer) {
        return getOne(orgId, (resource) -> Optional
                .ofNullable(resource)
                .map(OrganisasjonselementResource::getOrganisasjonsnummer)
                .map(Identifikator::getIdentifikatorverdi)
                .map(_id -> _id.equals(organisasjonsnummer))
                .orElse(false));
    }


	@Override
    public void onAction(Event event) {
        List<OrganisasjonselementResource> data;
        if (checkFintResourceCompatibility && fintResourceCompatibility.isFintResourceData(event.getData())) {
            log.info("Compatibility: Converting FintResource<OrganisasjonselementResource> to OrganisasjonselementResource ...");
            data = fintResourceCompatibility.convertResourceData(event.getData(), OrganisasjonselementResource.class);
        } else {
            data = objectMapper.convertValue(event.getData(), javaType);
        }
        data.forEach(linker::mapLinks);
        if (OrganisasjonActions.valueOf(event.getAction()) == OrganisasjonActions.UPDATE_ORGANISASJONSELEMENT) {
            if (event.getResponseStatus() == ResponseStatus.ACCEPTED || event.getResponseStatus() == ResponseStatus.CONFLICT) {
                add(event.getOrgId(), data);
                log.info("Added {} elements to cache for {}", data.size(), event.getOrgId());
            } else {
                log.debug("Ignoring payload for {} with response status {}", event.getOrgId(), event.getResponseStatus());
            }
        } else {
            update(event.getOrgId(), data);
            log.info("Updated cache for {} with {} elements", event.getOrgId(), data.size());
        }
    }
}