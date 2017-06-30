package no.fint.consumer.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.model.administrasjon.organisasjon.Organisasjonselement
import no.fint.model.administrasjon.personal.Arbeidsforhold
import no.fint.model.administrasjon.personal.Personalressurs
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.relation.FintResource
import no.fint.model.relation.Relation

class TestDataGenerator {

    static void main(String[] args) {
        def objectMapper = new ObjectMapper()
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

        Relation relation = new Relation.Builder()
                .with(Personalressurs.Relasjonsnavn.PERSON)
                .forType(Arbeidsforhold)
                .field('ansattnummer')
                .value('10025').build()

        Identifikator identifikator = new Identifikator(identifikatorverdi: '12345')
        Organisasjonselement organisasjonselement = new Organisasjonselement(organisasjonsId: identifikator)

        FintResource fintResource = FintResource.with(organisasjonselement).addRelasjoner(relation)

        def json = objectMapper.writeValueAsString(fintResource)

        json != null
        println json
    }
}
