package no.fint.consumer.models.organisasjonselement;

import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.relation.FintResource;
import no.fint.relations.FintResourceAssembler;
import no.fint.relations.FintResourceSupport;
import org.springframework.stereotype.Component;

@Component
public class OrganisasjonselementAssembler extends FintResourceAssembler<Organisasjonselement> {

    public OrganisasjonselementAssembler() {
        super(OrganisasjonselementController.class);
    }


    @Override
    public FintResourceSupport assemble(Organisasjonselement organisasjonselement , FintResource<Organisasjonselement> fintResource) {
        return createResourceWithId(organisasjonselement.getOrganisasjonsId().getIdentifikatorverdi(), fintResource, "organisasjonsid");
    }
    
    
    
}

