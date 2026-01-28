package no.fint.consumer.models.organisasjonselement;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

import no.fint.cache.CacheService;
import no.fint.cache.model.CacheObject;
import no.fint.consumer.config.Constants;
import no.fint.consumer.config.ConsumerProps;
import no.fint.consumer.event.ConsumerEventUtil;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.relations.FintResourceCompatibility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.novari.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.novari.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.novari.fint.model.administrasjon.organisasjon.OrganisasjonActions;
import no.novari.fint.model.felles.kompleksedatatyper.Identifikator;

@Slf4j
@Service
@ConditionalOnProperty(name = "fint.consumer.cache.disabled.organisasjonselement", havingValue = "false", matchIfMissing = true)
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

    @Override
    public void populateCache(String orgId) {
		log.info("Populating Organisasjonselement cache for {}", orgId);
        Event event = new Event(orgId, Constants.COMPONENT, OrganisasjonActions.GET_ALL_ORGANISASJONSELEMENT, Constants.CACHE_SERVICE);
        consumerEventUtil.send(event);
    }


    public Optional<OrganisasjonselementResource> getOrganisasjonselementByOrganisasjonsId(String orgId, String organisasjonsId) {
        return getOne(orgId, organisasjonsId.hashCode(),
            (resource) -> Optional
                .ofNullable(resource)
                .map(OrganisasjonselementResource::getOrganisasjonsId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(organisasjonsId::equals)
                .orElse(false));
    }

    public Optional<OrganisasjonselementResource> getOrganisasjonselementByOrganisasjonsKode(String orgId, String organisasjonsKode) {
        return getOne(orgId, organisasjonsKode.hashCode(),
            (resource) -> Optional
                .ofNullable(resource)
                .map(OrganisasjonselementResource::getOrganisasjonsKode)
                .map(Identifikator::getIdentifikatorverdi)
                .map(organisasjonsKode::equals)
                .orElse(false));
    }

    public Optional<OrganisasjonselementResource> getOrganisasjonselementByOrganisasjonsnummer(String orgId, String organisasjonsnummer) {
        return getOne(orgId, organisasjonsnummer.hashCode(),
            (resource) -> Optional
                .ofNullable(resource)
                .map(OrganisasjonselementResource::getOrganisasjonsnummer)
                .map(Identifikator::getIdentifikatorverdi)
                .map(organisasjonsnummer::equals)
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
        data.forEach(resource -> {
            linker.mapLinks(resource);
            linker.resetSelfLinks(resource);
        });
        if (OrganisasjonActions.valueOf(event.getAction()) == OrganisasjonActions.UPDATE_ORGANISASJONSELEMENT) {
            if (event.getResponseStatus() == ResponseStatus.ACCEPTED || event.getResponseStatus() == ResponseStatus.CONFLICT) {
                List<CacheObject<OrganisasjonselementResource>> cacheObjects = data
                    .stream()
                    .map(i -> new CacheObject<>(i, linker.hashCodes(i)))
                    .collect(Collectors.toList());
                addCache(event.getOrgId(), cacheObjects);
                log.info("Added {} cache objects to cache for {}", cacheObjects.size(), event.getOrgId());
            } else {
                log.debug("Ignoring payload for {} with response status {}", event.getOrgId(), event.getResponseStatus());
            }
        } else {
            List<CacheObject<OrganisasjonselementResource>> cacheObjects = data
                    .stream()
                    .map(i -> new CacheObject<>(i, linker.hashCodes(i)))
                    .collect(Collectors.toList());
            updateCache(event.getOrgId(), cacheObjects);
            log.info("Updated cache for {} with {} cache objects", event.getOrgId(), cacheObjects.size());
        }
    }
}
