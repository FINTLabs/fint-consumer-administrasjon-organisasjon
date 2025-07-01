package no.fint.consumer.config;

import no.fint.consumer.utils.RestEndpoints;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import no.fint.model.administrasjon.organisasjon.Arbeidslokasjon;
import no.fint.model.administrasjon.organisasjon.Organisasjonselement;

public class LinkMapper {

    public static Map<String, String> linkMapper(String contextPath) {
        return ImmutableMap.<String,String>builder()
            .put(Arbeidslokasjon.class.getName(), contextPath + RestEndpoints.ARBEIDSLOKASJON)
            .put(Organisasjonselement.class.getName(), contextPath + RestEndpoints.ORGANISASJONSELEMENT)
            .put("no.fint.model.felles.kodeverk.iso.Landkode", "/felles/kodeverk/iso/landkode")
            .put("no.fint.model.administrasjon.personal.Arbeidsforhold", "/administrasjon/personal/arbeidsforhold")
            .put("no.fint.model.administrasjon.kodeverk.Ansvar", "/administrasjon/kodeverk/ansvar")
            .put("no.fint.model.administrasjon.kodeverk.Organisasjonstype", "/administrasjon/kodeverk/organisasjonstype")
            .put("no.fint.model.administrasjon.personal.Personalressurs", "/administrasjon/personal/personalressurs")
            .put("no.fint.model.utdanning.utdanningsprogram.Skole", "/utdanning/utdanningsprogram/skole")
            /* .put(TODO,TODO) */
            .build();
    }

}
