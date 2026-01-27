package no.fint.consumer.models.organisasjonselement;

import no.novari.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.novari.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResources;
import no.fint.relations.FintLinker;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
        return toResources(collection.stream(), 0, 0, collection.size());
    }

    @Override
    public OrganisasjonselementResources toResources(Stream<OrganisasjonselementResource> stream, int offset, int size, int totalItems) {
        OrganisasjonselementResources resources = new OrganisasjonselementResources();
        stream.map(this::toResource).forEach(resources::addResource);
        addPagination(resources, offset, size, totalItems);
        return resources;
    }

    @Override
    public String getSelfHref(OrganisasjonselementResource organisasjonselement) {
        return getAllSelfHrefs(organisasjonselement).findFirst().orElse(null);
    }

    @Override
    public Stream<String> getAllSelfHrefs(OrganisasjonselementResource organisasjonselement) {
        Stream.Builder<String> builder = Stream.builder();
        if (!isNull(organisasjonselement.getOrganisasjonsId()) && !isEmpty(organisasjonselement.getOrganisasjonsId().getIdentifikatorverdi())) {
            builder.add(createHrefWithId(organisasjonselement.getOrganisasjonsId().getIdentifikatorverdi(), "organisasjonsid"));
        }
        if (!isNull(organisasjonselement.getOrganisasjonsKode()) && !isEmpty(organisasjonselement.getOrganisasjonsKode().getIdentifikatorverdi())) {
            builder.add(createHrefWithId(organisasjonselement.getOrganisasjonsKode().getIdentifikatorverdi(), "organisasjonskode"));
        }
        if (!isNull(organisasjonselement.getOrganisasjonsnummer()) && !isEmpty(organisasjonselement.getOrganisasjonsnummer().getIdentifikatorverdi())) {
            builder.add(createHrefWithId(organisasjonselement.getOrganisasjonsnummer().getIdentifikatorverdi(), "organisasjonsnummer"));
        }
        
        return builder.build();
    }

    int[] hashCodes(OrganisasjonselementResource organisasjonselement) {
        IntStream.Builder builder = IntStream.builder();
        if (!isNull(organisasjonselement.getOrganisasjonsId()) && !isEmpty(organisasjonselement.getOrganisasjonsId().getIdentifikatorverdi())) {
            builder.add(organisasjonselement.getOrganisasjonsId().getIdentifikatorverdi().hashCode());
        }
        if (!isNull(organisasjonselement.getOrganisasjonsKode()) && !isEmpty(organisasjonselement.getOrganisasjonsKode().getIdentifikatorverdi())) {
            builder.add(organisasjonselement.getOrganisasjonsKode().getIdentifikatorverdi().hashCode());
        }
        if (!isNull(organisasjonselement.getOrganisasjonsnummer()) && !isEmpty(organisasjonselement.getOrganisasjonsnummer().getIdentifikatorverdi())) {
            builder.add(organisasjonselement.getOrganisasjonsnummer().getIdentifikatorverdi().hashCode());
        }
        
        return builder.build().toArray();
    }

}

