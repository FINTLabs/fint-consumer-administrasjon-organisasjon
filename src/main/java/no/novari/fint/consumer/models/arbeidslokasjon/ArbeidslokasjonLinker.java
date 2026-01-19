package no.novari.fint.consumer.models.arbeidslokasjon;

import no.novari.fint.model.resource.administrasjon.organisasjon.ArbeidslokasjonResource;
import no.novari.fint.model.resource.administrasjon.organisasjon.ArbeidslokasjonResources;
import no.novari.fint.relations.FintLinker;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static org.springframework.util.StringUtils.isEmpty;

@Component
public class ArbeidslokasjonLinker extends FintLinker<ArbeidslokasjonResource> {

    public ArbeidslokasjonLinker() {
        super(ArbeidslokasjonResource.class);
    }

    public void mapLinks(ArbeidslokasjonResource resource) {
        super.mapLinks(resource);
    }

    @Override
    public ArbeidslokasjonResources toResources(Collection<ArbeidslokasjonResource> collection) {
        return toResources(collection.stream(), 0, 0, collection.size());
    }

    @Override
    public ArbeidslokasjonResources toResources(Stream<ArbeidslokasjonResource> stream, int offset, int size, int totalItems) {
        ArbeidslokasjonResources resources = new ArbeidslokasjonResources();
        stream.map(this::toResource).forEach(resources::addResource);
        addPagination(resources, offset, size, totalItems);
        return resources;
    }

    @Override
    public String getSelfHref(ArbeidslokasjonResource arbeidslokasjon) {
        return getAllSelfHrefs(arbeidslokasjon).findFirst().orElse(null);
    }

    @Override
    public Stream<String> getAllSelfHrefs(ArbeidslokasjonResource arbeidslokasjon) {
        Stream.Builder<String> builder = Stream.builder();
        if (!isNull(arbeidslokasjon.getLokasjonskode()) && !isEmpty(arbeidslokasjon.getLokasjonskode().getIdentifikatorverdi())) {
            builder.add(createHrefWithId(arbeidslokasjon.getLokasjonskode().getIdentifikatorverdi(), "lokasjonskode"));
        }
        if (!isNull(arbeidslokasjon.getOrganisasjonsnummer()) && !isEmpty(arbeidslokasjon.getOrganisasjonsnummer().getIdentifikatorverdi())) {
            builder.add(createHrefWithId(arbeidslokasjon.getOrganisasjonsnummer().getIdentifikatorverdi(), "organisasjonsnummer"));
        }
        
        return builder.build();
    }

    int[] hashCodes(ArbeidslokasjonResource arbeidslokasjon) {
        IntStream.Builder builder = IntStream.builder();
        if (!isNull(arbeidslokasjon.getLokasjonskode()) && !isEmpty(arbeidslokasjon.getLokasjonskode().getIdentifikatorverdi())) {
            builder.add(arbeidslokasjon.getLokasjonskode().getIdentifikatorverdi().hashCode());
        }
        if (!isNull(arbeidslokasjon.getOrganisasjonsnummer()) && !isEmpty(arbeidslokasjon.getOrganisasjonsnummer().getIdentifikatorverdi())) {
            builder.add(arbeidslokasjon.getOrganisasjonsnummer().getIdentifikatorverdi().hashCode());
        }
        
        return builder.build().toArray();
    }

}

