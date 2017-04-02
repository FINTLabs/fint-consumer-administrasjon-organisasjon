package no.fint.consumer.organisasjonselement;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import no.fint.audit.FintAuditService;
import no.fint.consumer.event.EventActions;
import no.fint.consumer.utils.CacheUri;
import no.fint.consumer.utils.RestEndpoints;
import no.fint.event.model.Event;
import no.fint.event.model.Status;
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
    public Map<String, String> getLastUpdated(@RequestHeader(value = "x-org-id") String orgId) {
        String lastUpdated = Long.toString(cacheService.getLastUpdated(CacheUri.create(orgId, "organisasjonselement")));
        return ImmutableMap.of("lastUpdated", lastUpdated);
    }

    @FintRelations
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity getOrganisasjonselementer(@RequestHeader(value = "x-org-id") String orgId,
                                                    @RequestHeader(value = "x-client") String client,
                                                    @RequestParam(required = false) Long sinceTimeStamp) {
        log.info("OrgId: {}", orgId);
        log.info("Client: {}", client);
        log.info("SinceTimeStamp: {}", sinceTimeStamp);

        Event event = new Event(orgId, "administrasjon/organisasjon", EventActions.GET_ALL_ORGANISASJONSELEMENT.name(), client);
        fintAuditService.audit(event, true);

        event.setStatus(Status.CACHE);
        fintAuditService.audit(event, true);

        String cacheUri = CacheUri.create(orgId, "organisasjonselement");
        List<FintResource<Organisasjonselement>> organisasjonselements;
        if (sinceTimeStamp == null) {
            organisasjonselements = cacheService.getAll(cacheUri);
        } else {
            organisasjonselements = cacheService.getAll(cacheUri, sinceTimeStamp);
        }

        event.setStatus(Status.CACHE_RESPONSE);
        fintAuditService.audit(event, true);

        event.setStatus(Status.SENT_TO_CLIENT);
        fintAuditService.audit(event, false);

        return ResponseEntity.ok(organisasjonselements);
    }

    @FintRelations
    @RequestMapping(value = {"/organisasjonsId/{id}", "/organisasjonsid/{id}"}, method = RequestMethod.GET)
    public ResponseEntity getOrganisasjonselement(@PathVariable String id,
                                                  @RequestHeader(value = "x-org-id") String orgId,
                                                  @RequestHeader(value = "x-client") String client) {
        log.info("OrgId: {}", orgId);
        log.info("Client: {}", client);

        Event event = new Event(orgId, "administrasjon/organisasjon", EventActions.GET_ORGANISASJONSELEMENT.name(), client);
        fintAuditService.audit(event, true);

        event.setStatus(Status.CACHE);
        fintAuditService.audit(event, true);

        String cacheUri = CacheUri.create(orgId, "organisasjonselement");
        List<FintResource<Organisasjonselement>> organisasjonselements;

        organisasjonselements = cacheService.getAll(cacheUri);

        event.setStatus(Status.CACHE_RESPONSE);
        fintAuditService.audit(event, true);

        event.setStatus(Status.SENT_TO_CLIENT);
        fintAuditService.audit(event, false);


        Optional<FintResource<Organisasjonselement>> organisasjonselement = organisasjonselements.stream().filter(
                org -> org.getConvertedResource().getOrganisasjonsId().getIdentifikatorverdi().equals(id)
        ).findFirst();

        if (organisasjonselement.isPresent()) {
            return ResponseEntity.ok(organisasjonselement.get());
        } else {
            return ResponseEntity.notFound().build();
        }

    }
}
