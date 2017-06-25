package no.fint.consumer.organisasjonselement;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import no.fint.audit.FintAuditService;
import no.fint.cache.utils.CacheUri;
import no.fint.consumer.config.Constants;
import no.fint.consumer.utils.RestEndpoints;
import no.fint.event.model.Event;
import no.fint.event.model.HeaderConstants;
import no.fint.event.model.Status;
import no.fint.model.administrasjon.organisasjon.OrganisasjonActions;
import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.relation.FintResource;
import no.fint.relations.annotations.FintRelations;
import no.fint.relations.annotations.FintSelf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@FintSelf(type = Organisasjonselement.class, property = "organisasjonsId")
@Slf4j
@CrossOrigin
@RestController
@RequestMapping(value = RestEndpoints.ORGANISASJONSELEMENT, produces = {"application/hal+json", MediaType.APPLICATION_JSON_UTF8_VALUE})
public class OrganisasjonselementController {

    @Autowired
    private FintAuditService fintAuditService;

    @Autowired
    private OrganisasjonselementCacheService cacheService;

    @RequestMapping(value = "/last-updated", method = RequestMethod.GET)
    public Map<String, String> getLastUpdated(@RequestHeader(value = HeaderConstants.ORG_ID, defaultValue = Constants.DEFAULT_HEADER_ORGID) String orgId) {
        String lastUpdated = Long.toString(cacheService.getLastUpdated(CacheUri.create(orgId, OrganisasjonselementCacheService.MODEL)));
        return ImmutableMap.of("lastUpdated", lastUpdated);
    }

    @FintRelations
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity getOrganisasjonselementer(@RequestHeader(value = HeaderConstants.ORG_ID, defaultValue = Constants.DEFAULT_HEADER_ORGID) String orgId,
                                                    @RequestHeader(value = HeaderConstants.CLIENT, defaultValue = Constants.DEFAULT_HEADER_CLIENT) String client,
                                                    @RequestParam(required = false) Long sinceTimeStamp) {
        log.info("OrgId: {}", orgId);
        log.info("Client: {}", client);
        log.info("SinceTimeStamp: {}", sinceTimeStamp);

        Event event = new Event(orgId, Constants.COMPONENT, OrganisasjonActions.GET_ALL_ORGANISASJONSELEMENT, client);
        fintAuditService.audit(event);

        event.setStatus(Status.CACHE);
        fintAuditService.audit(event);

        String cacheUri = CacheUri.create(orgId, OrganisasjonselementCacheService.MODEL);
        List<FintResource<Organisasjonselement>> organisasjonselements;
        if (sinceTimeStamp == null) {
            organisasjonselements = cacheService.getAll(cacheUri);
        } else {
            organisasjonselements = cacheService.getAll(cacheUri, sinceTimeStamp);
        }

        event.setStatus(Status.CACHE_RESPONSE);
        fintAuditService.audit(event);

        event.setStatus(Status.SENT_TO_CLIENT);
        fintAuditService.audit(event);

        return ResponseEntity.ok(organisasjonselements);
    }

    @FintRelations
    @RequestMapping(value = {"/organisasjonsId/{id:.+}", "/organisasjonsid/{id:.+}"}, method = RequestMethod.GET)
    public ResponseEntity getOrganisasjonselementOrgId(@PathVariable String id,
                                                       @RequestHeader(value = HeaderConstants.ORG_ID, defaultValue = Constants.DEFAULT_HEADER_ORGID) String orgId,
                                                       @RequestHeader(value = HeaderConstants.CLIENT, defaultValue = Constants.DEFAULT_HEADER_CLIENT) String client) {
        log.info("OrgId: {}", orgId);
        log.info("Client: {}", client);

        Event event = new Event(orgId, Constants.COMPONENT, OrganisasjonActions.GET_ORGANISASJONSELEMENT, client);
        fintAuditService.audit(event);

        event.setStatus(Status.CACHE);
        fintAuditService.audit(event);

        String cacheUri = CacheUri.create(orgId, OrganisasjonselementCacheService.MODEL);
        List<FintResource<Organisasjonselement>> organisasjonselements;

        organisasjonselements = cacheService.getAll(cacheUri);

        event.setStatus(Status.CACHE_RESPONSE);
        fintAuditService.audit(event);

        event.setStatus(Status.SENT_TO_CLIENT);
        fintAuditService.audit(event);


        Optional<FintResource<Organisasjonselement>> organisasjonselement = organisasjonselements.stream().filter(
                org -> org.getConvertedResource().getOrganisasjonsId().getIdentifikatorverdi().equals(id)
        ).findFirst();

        if (organisasjonselement.isPresent()) {
            return ResponseEntity.ok(organisasjonselement.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @FintRelations
    @RequestMapping(value = {"/organisasjonsKode/{kode}", "/organisasjonskode/{kode}"}, method = RequestMethod.GET)
    public ResponseEntity getOrganisasjonselementOrgKode(@PathVariable String kode,
                                                         @RequestHeader(value = HeaderConstants.ORG_ID, defaultValue = Constants.DEFAULT_HEADER_ORGID) String orgId,
                                                         @RequestHeader(value = HeaderConstants.CLIENT, defaultValue = Constants.DEFAULT_HEADER_CLIENT) String client) {
        log.info("OrgId: {}", orgId);
        log.info("Client: {}", client);

        Event event = new Event(orgId, Constants.COMPONENT, OrganisasjonActions.GET_ORGANISASJONSELEMENT, client);
        fintAuditService.audit(event);

        event.setStatus(Status.CACHE);
        fintAuditService.audit(event);

        String cacheUri = CacheUri.create(orgId, OrganisasjonselementCacheService.MODEL);
        List<FintResource<Organisasjonselement>> organisasjonselements;

        organisasjonselements = cacheService.getAll(cacheUri);

        event.setStatus(Status.CACHE_RESPONSE);
        fintAuditService.audit(event);

        event.setStatus(Status.SENT_TO_CLIENT);
        fintAuditService.audit(event);


        Optional<FintResource<Organisasjonselement>> organisasjonselement = organisasjonselements.stream().filter(
                org -> org.getConvertedResource().getOrganisasjonsKode().getIdentifikatorverdi().equals(kode)
        ).findFirst();

        if (organisasjonselement.isPresent()) {
            return ResponseEntity.ok(organisasjonselement.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
