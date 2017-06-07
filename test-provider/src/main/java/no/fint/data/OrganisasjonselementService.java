package no.fint.data;

import com.google.common.collect.ImmutableList;
import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.felles.Identifikator;
import no.fint.model.relation.FintResource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganisasjonselementService {

    public List<FintResource<Organisasjonselement>> getAll() {
        Identifikator organisasjonsId = new Identifikator();
        organisasjonsId.setIdentifikatorverdi("12345");

        Organisasjonselement organisasjonselement = new Organisasjonselement();
        organisasjonselement.setOrganisasjonsId(organisasjonsId);

        FintResource<Organisasjonselement> fintResource = FintResource.with(organisasjonselement);
        return ImmutableList.of(fintResource);
    }
}
