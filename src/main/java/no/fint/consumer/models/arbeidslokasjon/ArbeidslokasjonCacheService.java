package no.fint.consumer.models.arbeidslokasjon;

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

import no.novari.fint.model.administrasjon.organisasjon.Arbeidslokasjon;
import no.novari.fint.model.resource.administrasjon.organisasjon.ArbeidslokasjonResource;
import no.novari.fint.model.administrasjon.organisasjon.OrganisasjonActions;
import no.novari.fint.model.felles.kompleksedatatyper.Identifikator;

@Slf4j
@Service
@ConditionalOnProperty(name = "fint.consumer.cache.disabled.arbeidslokasjon", havingValue = "false", matchIfMissing = true)
public class ArbeidslokasjonCacheService extends CacheService<ArbeidslokasjonResource> {

    public static final String MODEL = Arbeidslokasjon.class.getSimpleName().toLowerCase();

    @Value("${fint.consumer.compatibility.fintresource:true}")
    private boolean checkFintResourceCompatibility;

    @Autowired
    private FintResourceCompatibility fintResourceCompatibility;

    @Autowired
    private ConsumerEventUtil consumerEventUtil;

    @Autowired
    private ConsumerProps props;

    @Autowired
    private ArbeidslokasjonLinker linker;

    private JavaType javaType;

    private ObjectMapper objectMapper;

    public ArbeidslokasjonCacheService() {
        super(MODEL, OrganisasjonActions.GET_ALL_ARBEIDSLOKASJON, OrganisasjonActions.UPDATE_ARBEIDSLOKASJON);
        objectMapper = new ObjectMapper();
        javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, ArbeidslokasjonResource.class);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @PostConstruct
    public void init() {
        props.getAssets().forEach(this::createCache);
    }

    @Scheduled(initialDelayString = Constants.CACHE_INITIALDELAY_ARBEIDSLOKASJON, fixedRateString = Constants.CACHE_FIXEDRATE_ARBEIDSLOKASJON)
    public void populateCacheAll() {
        props.getAssets().forEach(this::populateCache);
    }

    public void rebuildCache(String orgId) {
		flush(orgId);
		populateCache(orgId);
	}

    @Override
    public void populateCache(String orgId) {
		log.info("Populating Arbeidslokasjon cache for {}", orgId);
        Event event = new Event(orgId, Constants.COMPONENT, OrganisasjonActions.GET_ALL_ARBEIDSLOKASJON, Constants.CACHE_SERVICE);
        consumerEventUtil.send(event);
    }


    public Optional<ArbeidslokasjonResource> getArbeidslokasjonByLokasjonskode(String orgId, String lokasjonskode) {
        return getOne(orgId, lokasjonskode.hashCode(),
            (resource) -> Optional
                .ofNullable(resource)
                .map(ArbeidslokasjonResource::getLokasjonskode)
                .map(Identifikator::getIdentifikatorverdi)
                .map(lokasjonskode::equals)
                .orElse(false));
    }

    public Optional<ArbeidslokasjonResource> getArbeidslokasjonByOrganisasjonsnummer(String orgId, String organisasjonsnummer) {
        return getOne(orgId, organisasjonsnummer.hashCode(),
            (resource) -> Optional
                .ofNullable(resource)
                .map(ArbeidslokasjonResource::getOrganisasjonsnummer)
                .map(Identifikator::getIdentifikatorverdi)
                .map(organisasjonsnummer::equals)
                .orElse(false));
    }


	@Override
    public void onAction(Event event) {
        List<ArbeidslokasjonResource> data;
        if (checkFintResourceCompatibility && fintResourceCompatibility.isFintResourceData(event.getData())) {
            log.info("Compatibility: Converting FintResource<ArbeidslokasjonResource> to ArbeidslokasjonResource ...");
            data = fintResourceCompatibility.convertResourceData(event.getData(), ArbeidslokasjonResource.class);
        } else {
            data = objectMapper.convertValue(event.getData(), javaType);
        }
        data.forEach(resource -> {
            linker.mapLinks(resource);
            linker.resetSelfLinks(resource);
        });
        if (OrganisasjonActions.valueOf(event.getAction()) == OrganisasjonActions.UPDATE_ARBEIDSLOKASJON) {
            if (event.getResponseStatus() == ResponseStatus.ACCEPTED || event.getResponseStatus() == ResponseStatus.CONFLICT) {
                List<CacheObject<ArbeidslokasjonResource>> cacheObjects = data
                    .stream()
                    .map(i -> new CacheObject<>(i, linker.hashCodes(i)))
                    .collect(Collectors.toList());
                addCache(event.getOrgId(), cacheObjects);
                log.info("Added {} cache objects to cache for {}", cacheObjects.size(), event.getOrgId());
            } else {
                log.debug("Ignoring payload for {} with response status {}", event.getOrgId(), event.getResponseStatus());
            }
        } else {
            List<CacheObject<ArbeidslokasjonResource>> cacheObjects = data
                    .stream()
                    .map(i -> new CacheObject<>(i, linker.hashCodes(i)))
                    .collect(Collectors.toList());
            updateCache(event.getOrgId(), cacheObjects);
            log.info("Updated cache for {} with {} cache objects", event.getOrgId(), cacheObjects.size());
        }
    }
}
