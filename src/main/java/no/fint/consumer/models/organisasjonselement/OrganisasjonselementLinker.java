package no.fint.consumer.models.organisasjonselement;

import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResources;
import no.fint.relations.FintLinker;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static java.util.Objects.isNull;
import static org.springframework.util.StringUtils.isEmpty;


@Component
public class OrganisasjonselementLinker extends FintLinker<OrganisasjonselementResource> {

    public OrganisasjonselementLinker() {
        super(OrganisasjonselementResource.class);
    }

    public void mapLinks(OrganisasjonselementResource resource) {
        super.mapLinks(resource);
    }

    @Override
    public OrganisasjonselementResources toResources(Collection<OrganisasjonselementResource> collection) {
        OrganisasjonselementResources resources = new OrganisasjonselementResources();
        collection.stream().map(this::toResource).forEach(resources::addResource);
        resources.addSelf(Link.with(self()));
        return resources;
    }

    @Override
    public String getSelfHref(OrganisasjonselementResource organisasjonselement) {
        if (!isNull(organisasjonselement.getOrganisasjonsId()) && !isEmpty(organisasjonselement.getOrganisasjonsId().getIdentifikatorverdi())) {
            return createHrefWithId(organisasjonselement.getOrganisasjonsId().getIdentifikatorverdi(), "organisasjonsid");
        }
        if (!isNull(organisasjonselement.getOrganisasjonsKode()) && !isEmpty(organisasjonselement.getOrganisasjonsKode().getIdentifikatorverdi())) {
            return createHrefWithId(organisasjonselement.getOrganisasjonsKode().getIdentifikatorverdi(), "organisasjonskode");
        }
        if (!isNull(organisasjonselement.getOrganisasjonsnummer()) && !isEmpty(organisasjonselement.getOrganisasjonsnummer().getIdentifikatorverdi())) {
            return createHrefWithId(organisasjonselement.getOrganisasjonsnummer().getIdentifikatorverdi(), "organisasjonsnummer");
        }
        
        return null;
    }
    
}

