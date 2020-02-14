package no.fint.consumer.config;

import com.google.common.collect.ImmutableMap;
import no.fint.consumer.utils.RestEndpoints;
import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.administrasjon.personal.Arbeidsforhold;
import no.fint.model.administrasjon.personal.Personalressurs;

import java.util.Map;

public class LinkMapper {

    public static Map<String, String> linkMapper(String contextPath) {
        return ImmutableMap.<String, String>builder()
                .put(Organisasjonselement.class.getName(), contextPath + RestEndpoints.ORGANISASJONSELEMENT)
                .put(Personalressurs.class.getName(), "/administrasjon/personal/personalressurs")
                .put(Arbeidsforhold.class.getName(), "/administrasjon/personal/arbeidsforhold")
                .put("no.fint.model.utdanning.utdanningsprogram.Skole", "/utdanning/utdanningsprogram/skole")
                .build();
    }

}
