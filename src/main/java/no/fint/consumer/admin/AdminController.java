package no.fint.consumer.admin;

import lombok.AccessLevel;
import lombok.Setter;
import no.fint.consumer.config.Constants;
import no.fint.consumer.event.ConsumerEventUtil;
import no.fint.consumer.service.SubscriberService;
import no.fint.event.model.DefaultActions;
import no.fint.event.model.Event;
import no.fint.event.model.HeaderConstants;
import no.fint.event.model.health.Health;
import no.fint.event.model.health.HealthStatus;
import no.fint.events.FintEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@CrossOrigin
@RestController
@RequestMapping(value = "/admin", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class AdminController {

    @Autowired
    private ConsumerEventUtil consumerEventUtil;

    @Setter(AccessLevel.PACKAGE)
    private Map<String, Long> orgIds = new ConcurrentHashMap<>();

    @Autowired
    private FintEvents fintEvents;

    @GetMapping("/health")
    public ResponseEntity healthCheck(@RequestHeader(value = HeaderConstants.ORG_ID, defaultValue = Constants.DEFAULT_HEADER_ORGID) String orgId,
                                      @RequestHeader(value = HeaderConstants.CLIENT, defaultValue = Constants.DEFAULT_HEADER_CLIENT) String client) {
        Event<Health> event = new Event<>(orgId, Constants.COMPONENT, DefaultActions.HEALTH, client);
        event.addData(new Health(Constants.COMPONENT_CONSUMER, HealthStatus.SENT_FROM_CONSUMER_TO_PROVIDER));
        Optional<Event<Health>> health = consumerEventUtil.healthCheck(event);

        if (health.isPresent()) {
            Event<Health> receivedHealth = health.get();
            receivedHealth.addData(new Health(Constants.COMPONENT_CONSUMER, HealthStatus.RECEIVED_IN_CONSUMER_FROM_PROVIDER));
            return ResponseEntity.ok(receivedHealth);
        } else {
            event.setMessage("No response from adapter");
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(event);
        }
    }

    @PostMapping("/organization/orgIds/{orgId}")
    public ResponseEntity registerOrganization(@PathVariable String orgId) {
        if (orgIds.containsKey(orgId)) {
            return ResponseEntity.badRequest().body(String.format("OrgId %s is already registered", orgId));
        } else {
            Event event = new Event(orgId, Constants.COMPONENT, DefaultActions.REGISTER_ORG_ID, Constants.COMPONENT_CONSUMER);
            fintEvents.sendDownstream("system", event);

            fintEvents.registerUpstreamListener(SubscriberService.class, orgId);
            orgIds.put(orgId, System.currentTimeMillis());

            URI location = ServletUriComponentsBuilder.fromCurrentRequest().buildAndExpand().toUri();
            return ResponseEntity.created(location).build();
        }
    }

}
