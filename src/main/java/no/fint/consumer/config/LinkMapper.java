package no.fint.consumer.config;

import no.fint.consumer.utils.RestEndpoints;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import no.novari.fint.model.administrasjon.organisasjon.Arbeidslokasjon;
import no.novari.fint.model.administrasjon.organisasjon.Organisasjonselement;

public class LinkMapper {

    public static Map<String, String> linkMapper(String contextPath) {
        return ImmutableMap.<String,String>builder()
            .put(Arbeidslokasjon.class.getName(), contextPath + RestEndpoints.ARBEIDSLOKASJON)
            .put(Organisasjonselement.class.getName(), contextPath + RestEndpoints.ORGANISASJONSELEMENT)
            .put("no.novari.fint.model.felles.kodeverk.iso.Landkode", "/felles/kodeverk/iso/landkode")
            .put("no.novari.fint.model.administrasjon.personal.Arbeidsforhold", "/administrasjon/personal/arbeidsforhold")
            .put("no.novari.fint.model.administrasjon.kodeverk.Ansvar", "/administrasjon/kodeverk/ansvar")
            .put("no.novari.fint.model.administrasjon.kodeverk.Organisasjonstype", "/administrasjon/kodeverk/organisasjonstype")
            .put("no.novari.fint.model.administrasjon.personal.Personalressurs", "/administrasjon/personal/personalressurs")
            .put("no.novari.fint.model.utdanning.utdanningsprogram.Skole", "/utdanning/utdanningsprogram/skole")
            /* .put(TODO,TODO) */
            .build();
    }

}
