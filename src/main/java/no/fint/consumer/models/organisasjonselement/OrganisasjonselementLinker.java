package no.fint.consumer.models.organisasjonselement;

import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResources;
import no.fint.relations.FintLinker;
import org.springframework.stereotype.Component;

import java.util.Collection;

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
        if (organisasjonselement.getOrganisasjonsId() != null && organisasjonselement.getOrganisasjonsId().getIdentifikatorverdi() != null) {
            return createHrefWithId(organisasjonselement.getOrganisasjonsId().getIdentifikatorverdi(), "organisasjonsid");
        }
        if (organisasjonselement.getOrganisasjonsKode() != null && organisasjonselement.getOrganisasjonsKode().getIdentifikatorverdi() != null) {
            return createHrefWithId(organisasjonselement.getOrganisasjonsKode().getIdentifikatorverdi(), "organisasjonskode");
        }
        if (organisasjonselement.getOrganisasjonsnummer() != null && organisasjonselement.getOrganisasjonsnummer().getIdentifikatorverdi() != null) {
            return createHrefWithId(organisasjonselement.getOrganisasjonsnummer().getIdentifikatorverdi(), "organisasjonsnummer");
        }
        
        return null;
    }
    
}

