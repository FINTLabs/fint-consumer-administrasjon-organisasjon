package no.fint;

import com.google.common.collect.Lists;
import no.fint.data.OrganisasjonselementService;
import no.fint.event.model.Event;
import no.fint.event.model.Status;
import no.fint.event.model.health.Health;
import no.fint.events.FintEvents;
import no.fint.events.FintEventsHealth;
import no.fint.events.annotations.FintEventListener;
import no.fint.events.queue.QueueType;
import no.fint.model.relation.FintResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestListener {

    @Autowired
    private FintEvents fintEvents;

    @Autowired
    private FintEventsHealth fintEventsHealth;

    @Autowired
    private OrganisasjonselementService organisasjonselementService;


    @FintEventListener(type = QueueType.DOWNSTREAM)
    public void recieve(Event event) {
        if (event.isHealthCheck()) {
            event.addObject(new Health("test-provider", "Reply from test-client"));
            event.setStatus(Status.TEMP_UPSTREAM_QUEUE);
            fintEventsHealth.respondHealthCheck(event.getCorrId(), event);
        } else {
            List<FintResource> resources = Lists.newArrayList();
            switch (event.getAction()) {
                case "GET_ALL_ORGANISASJONSELEMENT":
                    resources.addAll(organisasjonselementService.getAll());
                    break;
                case "GET_ORGANISASJONSELEMENT":
                    resources.addAll(organisasjonselementService.getAll());
                    break;
            }

            Event<FintResource> response = new Event<>(event);
            response.setStatus(Status.UPSTREAM_QUEUE);
            response.setData(resources);
            fintEvents.sendUpstream(event.getOrgId(), response);
        }
    }

}
