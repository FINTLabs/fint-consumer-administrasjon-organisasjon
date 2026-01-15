package no.novari.fint.consumer.config;

import no.novari.fint.consumer.utils.RestEndpoints;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import no.novari.fint.model.administrasjon.organisasjon.Arbeidslokasjon;
import no.novari.fint.model.administrasjon.organisasjon.Organisasjonselement;

public class LinkMapper {

    public static Map<String, String> linkMapper(String contextPath) {
        return ImmutableMap.<String,String>builder()
            .put(Arbeidslokasjon.class.getName(), contextPath + RestEndpoints.ARBEIDSLOKASJON)
            .put(Organisasjonselement.class.getName(), contextPath + RestEndpoints.ORGANISASJONSELEMENT)
            .put("no.novari.fint.model.felles.kodeverk.iso.Landkode", "/model/felles/kodeverk/iso/landkode")
            .put("no.novari.fint.model.administrasjon.personal.Arbeidsforhold", "/model/administrasjon/personal/arbeidsforhold")
            .put("no.novari.fint.model.administrasjon.kodeverk.Ansvar", "/model/administrasjon/kodeverk/ansvar")
            .put("no.novari.fint.model.administrasjon.kodeverk.Organisasjonstype", "/model/administrasjon/kodeverk/organisasjonstype")
            .put("no.novari.fint.model.administrasjon.personal.Personalressurs", "/model/administrasjon/personal/personalressurs")
            .put("no.novari.fint.model.utdanning.utdanningsprogram.Skole", "/model/utdanning/utdanningsprogram/skole")
            /* .put(TODO,TODO) */
            .build();
    }

}
