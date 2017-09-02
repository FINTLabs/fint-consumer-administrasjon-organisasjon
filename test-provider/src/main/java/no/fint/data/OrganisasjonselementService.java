package no.fint.data;

import com.google.common.collect.ImmutableList;
import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.relation.FintResource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganisasjonselementService {

    public List<FintResource<Organisasjonselement>> getAll() {
        Identifikator organisasjonsId = new Identifikator();
        organisasjonsId.setIdentifikatorverdi("12345");

        Identifikator organisasjonsKode = new Identifikator();
        organisasjonsKode.setIdentifikatorverdi("abc");

        Organisasjonselement organisasjonselement = new Organisasjonselement();
        organisasjonselement.setOrganisasjonsId(organisasjonsId);
        organisasjonselement.setOrganisasjonsKode(organisasjonsKode);

        FintResource<Organisasjonselement> fintResource = FintResource.with(organisasjonselement);
        return ImmutableList.of(fintResource);
    }
}
