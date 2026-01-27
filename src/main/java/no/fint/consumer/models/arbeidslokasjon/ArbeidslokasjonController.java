package no.fint.consumer.models.arbeidslokasjon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import no.fint.audit.FintAuditService;

import no.fint.cache.exceptions.*;
import no.fint.consumer.config.Constants;
import no.fint.consumer.config.ConsumerProps;
import no.fint.consumer.event.ConsumerEventUtil;
import no.fint.consumer.event.SynchronousEvents;
import no.fint.consumer.exceptions.*;
import no.fint.consumer.status.StatusCache;
import no.fint.consumer.utils.EventResponses;
import no.fint.consumer.utils.RestEndpoints;
import no.fint.antlr.FintFilterService;

import no.fint.event.model.*;

import no.fint.relations.FintRelationsMediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.UnknownHostException;

import java.util.Map;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import no.novari.fint.model.resource.administrasjon.organisasjon.ArbeidslokasjonResource;
import no.novari.fint.model.resource.administrasjon.organisasjon.ArbeidslokasjonResources;
import no.novari.fint.model.administrasjon.organisasjon.OrganisasjonActions;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Api(tags = {"Arbeidslokasjon"})
@CrossOrigin
@RestController
@RequestMapping(name = "Arbeidslokasjon", value = RestEndpoints.ARBEIDSLOKASJON, produces = {FintRelationsMediaType.APPLICATION_HAL_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE})
public class ArbeidslokasjonController {

    private static final String ODATA_FILTER_QUERY_OPTION = "$filter=";

    @Autowired(required = false)
    private ArbeidslokasjonCacheService cacheService;

    @Autowired
    private FintAuditService fintAuditService;

    @Autowired
    private ArbeidslokasjonLinker linker;

    @Autowired
    private ConsumerProps props;

    @Autowired
    private StatusCache statusCache;

    @Autowired
    private ConsumerEventUtil consumerEventUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SynchronousEvents synchronousEvents;

    @Autowired
    private FintFilterService fintFilterService;

    @GetMapping("/last-updated")
    public Map<String, String> getLastUpdated(@RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId) {
        if (cacheService == null) {
            throw new CacheDisabledException("Arbeidslokasjon cache is disabled.");
        }
        if (props.isOverrideOrgId() || orgId == null) {
            orgId = props.getDefaultOrgId();
        }
        String lastUpdated = Long.toString(cacheService.getLastUpdated(orgId));
        return ImmutableMap.of("lastUpdated", lastUpdated);
    }

    @GetMapping("/cache/size")
    public ImmutableMap<String, Integer> getCacheSize(@RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId) {
        if (cacheService == null) {
            throw new CacheDisabledException("Arbeidslokasjon cache is disabled.");
        }
        if (props.isOverrideOrgId() || orgId == null) {
            orgId = props.getDefaultOrgId();
        }
        return ImmutableMap.of("size", cacheService.getCacheSize(orgId));
    }

    @GetMapping
    public ArbeidslokasjonResources getArbeidslokasjon(
            @RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT, required = false) String client,
            @RequestParam(defaultValue = "0") long sinceTimeStamp,
            @RequestParam(defaultValue = "0") int size,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) String $filter,
            HttpServletRequest request) throws InterruptedException {
        if (cacheService == null) {
            if (StringUtils.isNotBlank($filter)) {
                return getArbeidslokasjonByOdataFilter(client, orgId, $filter);
            }
            throw new CacheDisabledException("Arbeidslokasjon cache is disabled.");
        }
        if (props.isOverrideOrgId() || orgId == null) {
            orgId = props.getDefaultOrgId();
        }
        if (client == null) {
            client = props.getDefaultClient();
        }
        log.debug("OrgId: {}, Client: {}", orgId, client);

        Event event = new Event(orgId, Constants.COMPONENT, OrganisasjonActions.GET_ALL_ARBEIDSLOKASJON, client);
        event.setOperation(Operation.READ);
        if (StringUtils.isNotBlank(request.getQueryString())) {
            event.setQuery("?" + request.getQueryString());
        }
        fintAuditService.audit(event);
        fintAuditService.audit(event, Status.CACHE);

        Stream<ArbeidslokasjonResource> resources;
        if (size > 0 && offset >= 0 && sinceTimeStamp > 0) {
            resources = cacheService.streamSliceSince(orgId, sinceTimeStamp, offset, size);
        } else if (size > 0 && offset >= 0) {
            resources = cacheService.streamSlice(orgId, offset, size);
        } else if (sinceTimeStamp > 0) {
            resources = cacheService.streamSince(orgId, sinceTimeStamp);
        } else {
            resources = cacheService.streamAll(orgId);
        }

        fintAuditService.audit(event, Status.CACHE_RESPONSE, Status.SENT_TO_CLIENT);

        return linker.toResources(resources, offset, size, cacheService.getCacheSize(orgId));
    }
    
    @PostMapping("/$query")
    public ArbeidslokasjonResources getArbeidslokasjonByQuery(
            @RequestHeader(name = HeaderConstants.ORG_ID, required = false)   String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT, required = false) String client,
            @RequestParam(defaultValue = "0") long sinceTimeStamp,
            @RequestParam(defaultValue = "0") int  size,
            @RequestParam(defaultValue = "0") int  offset,
            @RequestBody(required = false) String query,
            HttpServletRequest request
    ) throws InterruptedException {
        return getArbeidslokasjon(orgId, client, sinceTimeStamp, size, offset, query, request);
    }

    private ArbeidslokasjonResources getArbeidslokasjonByOdataFilter(
        String client, String orgId, String $filter
    ) throws InterruptedException {
        if (!fintFilterService.validate($filter))
            throw new IllegalArgumentException("OData Filter is not valid");
    
        if (props.isOverrideOrgId() || orgId == null) orgId = props.getDefaultOrgId();
        if (client == null) client = props.getDefaultClient();
    
        Event event = new Event(
                orgId, Constants.COMPONENT,
                OrganisasjonActions.GET_ARBEIDSLOKASJON, client);
        event.setOperation(Operation.READ);
        event.setQuery(ODATA_FILTER_QUERY_OPTION.concat($filter));
    
        BlockingQueue<Event> queue = synchronousEvents.register(event);
        consumerEventUtil.send(event);
    
        Event response = EventResponses.handle(queue.poll(5, TimeUnit.MINUTES));
        if (response.getData() == null || response.getData().isEmpty())
            return new ArbeidslokasjonResources();
    
        ArrayList<ArbeidslokasjonResource> list = objectMapper.convertValue(
                response.getData(),
                new TypeReference<ArrayList<ArbeidslokasjonResource>>() {});
        fintAuditService.audit(response, Status.SENT_TO_CLIENT);
        list.forEach(r -> linker.mapAndResetLinks(r));
        return linker.toResources(list);
    }


    @GetMapping("/lokasjonskode/{id:.+}")
    public ArbeidslokasjonResource getArbeidslokasjonByLokasjonskode(
            @PathVariable String id,
            @RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT, required = false) String client) throws InterruptedException {
        if (props.isOverrideOrgId() || orgId == null) {
            orgId = props.getDefaultOrgId();
        }
        if (client == null) {
            client = props.getDefaultClient();
        }
        log.debug("lokasjonskode: {}, OrgId: {}, Client: {}", id, orgId, client);

        Event event = new Event(orgId, Constants.COMPONENT, OrganisasjonActions.GET_ARBEIDSLOKASJON, client);
        event.setOperation(Operation.READ);
        event.setQuery("lokasjonskode/" + id);

        if (cacheService != null) {
            fintAuditService.audit(event);
            fintAuditService.audit(event, Status.CACHE);

            Optional<ArbeidslokasjonResource> arbeidslokasjon = cacheService.getArbeidslokasjonByLokasjonskode(orgId, id);

            fintAuditService.audit(event, Status.CACHE_RESPONSE, Status.SENT_TO_CLIENT);

            return arbeidslokasjon.map(linker::toResource).orElseThrow(() -> new EntityNotFoundException(id));

        } else {
            BlockingQueue<Event> queue = synchronousEvents.register(event);
            consumerEventUtil.send(event);

            Event response = EventResponses.handle(queue.poll(5, TimeUnit.MINUTES));

            if (response.getData() == null ||
                    response.getData().isEmpty()) throw new EntityNotFoundException(id);

            ArbeidslokasjonResource arbeidslokasjon = objectMapper.convertValue(response.getData().get(0), ArbeidslokasjonResource.class);

            fintAuditService.audit(response, Status.SENT_TO_CLIENT);

            return linker.mapAndResetLinks(arbeidslokasjon);
        }    
    }

    @GetMapping("/organisasjonsnummer/{id:.+}")
    public ArbeidslokasjonResource getArbeidslokasjonByOrganisasjonsnummer(
            @PathVariable String id,
            @RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT, required = false) String client) throws InterruptedException {
        if (props.isOverrideOrgId() || orgId == null) {
            orgId = props.getDefaultOrgId();
        }
        if (client == null) {
            client = props.getDefaultClient();
        }
        log.debug("organisasjonsnummer: {}, OrgId: {}, Client: {}", id, orgId, client);

        Event event = new Event(orgId, Constants.COMPONENT, OrganisasjonActions.GET_ARBEIDSLOKASJON, client);
        event.setOperation(Operation.READ);
        event.setQuery("organisasjonsnummer/" + id);

        if (cacheService != null) {
            fintAuditService.audit(event);
            fintAuditService.audit(event, Status.CACHE);

            Optional<ArbeidslokasjonResource> arbeidslokasjon = cacheService.getArbeidslokasjonByOrganisasjonsnummer(orgId, id);

            fintAuditService.audit(event, Status.CACHE_RESPONSE, Status.SENT_TO_CLIENT);

            return arbeidslokasjon.map(linker::toResource).orElseThrow(() -> new EntityNotFoundException(id));

        } else {
            BlockingQueue<Event> queue = synchronousEvents.register(event);
            consumerEventUtil.send(event);

            Event response = EventResponses.handle(queue.poll(5, TimeUnit.MINUTES));

            if (response.getData() == null ||
                    response.getData().isEmpty()) throw new EntityNotFoundException(id);

            ArbeidslokasjonResource arbeidslokasjon = objectMapper.convertValue(response.getData().get(0), ArbeidslokasjonResource.class);

            fintAuditService.audit(response, Status.SENT_TO_CLIENT);

            return linker.mapAndResetLinks(arbeidslokasjon);
        }    
    }



    // Writable class
    @GetMapping("/status/{id}")
    public ResponseEntity getStatus(
            @PathVariable String id,
            @RequestHeader(HeaderConstants.ORG_ID) String orgId,
            @RequestHeader(HeaderConstants.CLIENT) String client) {
        log.debug("/status/{} for {} from {}", id, orgId, client);
        return statusCache.handleStatusRequest(id, orgId, linker, ArbeidslokasjonResource.class);
    }

    @PostMapping
    public ResponseEntity postArbeidslokasjon(
            @RequestHeader(name = HeaderConstants.ORG_ID) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT) String client,
            @RequestBody ArbeidslokasjonResource body,
            @RequestParam(name = "validate", required = false) boolean validate
    ) {
        log.debug("postArbeidslokasjon, Validate: {}, OrgId: {}, Client: {}", validate, orgId, client);
        log.trace("Body: {}", body);
        linker.mapLinks(body);
        Event event = new Event(orgId, Constants.COMPONENT, OrganisasjonActions.UPDATE_ARBEIDSLOKASJON, client);
        event.addObject(objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).convertValue(body, Map.class));
        event.setOperation(validate ? Operation.VALIDATE : Operation.CREATE);
        consumerEventUtil.send(event);

        statusCache.put(event.getCorrId(), event);

        URI location = UriComponentsBuilder.fromUriString(linker.self()).path("status/{id}").buildAndExpand(event.getCorrId()).toUri();
        return ResponseEntity.status(HttpStatus.ACCEPTED).location(location).build();
    }

  
    @PutMapping("/lokasjonskode/{id:.+}")
    public ResponseEntity putArbeidslokasjonByLokasjonskode(
            @PathVariable String id,
            @RequestHeader(name = HeaderConstants.ORG_ID) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT) String client,
            @RequestBody ArbeidslokasjonResource body
    ) {
        log.debug("putArbeidslokasjonByLokasjonskode {}, OrgId: {}, Client: {}", id, orgId, client);
        log.trace("Body: {}", body);
        linker.mapLinks(body);
        Event event = new Event(orgId, Constants.COMPONENT, OrganisasjonActions.UPDATE_ARBEIDSLOKASJON, client);
        event.setQuery("lokasjonskode/" + id);
        event.addObject(objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).convertValue(body, Map.class));
        event.setOperation(Operation.UPDATE);
        fintAuditService.audit(event);

        consumerEventUtil.send(event);

        statusCache.put(event.getCorrId(), event);

        URI location = UriComponentsBuilder.fromUriString(linker.self()).path("status/{id}").buildAndExpand(event.getCorrId()).toUri();
        return ResponseEntity.status(HttpStatus.ACCEPTED).location(location).build();
    }
  
    @PutMapping("/organisasjonsnummer/{id:.+}")
    public ResponseEntity putArbeidslokasjonByOrganisasjonsnummer(
            @PathVariable String id,
            @RequestHeader(name = HeaderConstants.ORG_ID) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT) String client,
            @RequestBody ArbeidslokasjonResource body
    ) {
        log.debug("putArbeidslokasjonByOrganisasjonsnummer {}, OrgId: {}, Client: {}", id, orgId, client);
        log.trace("Body: {}", body);
        linker.mapLinks(body);
        Event event = new Event(orgId, Constants.COMPONENT, OrganisasjonActions.UPDATE_ARBEIDSLOKASJON, client);
        event.setQuery("organisasjonsnummer/" + id);
        event.addObject(objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).convertValue(body, Map.class));
        event.setOperation(Operation.UPDATE);
        fintAuditService.audit(event);

        consumerEventUtil.send(event);

        statusCache.put(event.getCorrId(), event);

        URI location = UriComponentsBuilder.fromUriString(linker.self()).path("status/{id}").buildAndExpand(event.getCorrId()).toUri();
        return ResponseEntity.status(HttpStatus.ACCEPTED).location(location).build();
    }
  

    //
    // Exception handlers
    //
    @ExceptionHandler(EventResponseException.class)
    public ResponseEntity handleEventResponseException(EventResponseException e) {
        return ResponseEntity.status(e.getStatus()).body(e.getResponse());
    }

    @ExceptionHandler(UpdateEntityMismatchException.class)
    public ResponseEntity handleUpdateEntityMismatch(Exception e) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(e));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity handleEntityNotFound(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(e));
    }

    @ExceptionHandler(CreateEntityMismatchException.class)
    public ResponseEntity handleCreateEntityMismatch(Exception e) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(e));
    }

    @ExceptionHandler(EntityFoundException.class)
    public ResponseEntity handleEntityFound(Exception e) {
        return ResponseEntity.status(HttpStatus.FOUND).body(ErrorResponse.of(e));
    }

    @ExceptionHandler(CacheDisabledException.class)
    public ResponseEntity handleBadRequest(Exception e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ErrorResponse.of(e));
    }

    @ExceptionHandler(UnknownHostException.class)
    public ResponseEntity handleUnkownHost(Exception e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ErrorResponse.of(e));
    }

    @ExceptionHandler(CacheNotFoundException.class)
    public ResponseEntity handleCacheNotFound(Exception e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ErrorResponse.of(e));
    }

}

