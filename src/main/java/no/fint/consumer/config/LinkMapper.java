package no.fint.consumer.config;

import no.fint.consumer.utils.RestEndpoints;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.administrasjon.personal.Arbeidsforhold;
import no.fint.model.administrasjon.personal.Personalressurs;

public class LinkMapper {

    public static Map<String, String> linkMapper(String contextPath) {
        return ImmutableMap.<String, String>builder()
                .put(Organisasjonselement.class.getName(), contextPath + RestEndpoints.ORGANISASJONSELEMENT)
                .put(Personalressurs.class.getName(), "/administrasjon/personal/personalressurs")
                .put(Arbeidsforhold.class.getName(), "/administrasjon/personal/arbeidsforhold")
                .build();
    }

}
