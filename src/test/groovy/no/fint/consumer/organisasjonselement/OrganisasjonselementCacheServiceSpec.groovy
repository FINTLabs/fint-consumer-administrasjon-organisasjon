package no.fint.consumer.organisasjonselement

import no.fint.consumer.config.FintTestConfiguration
import no.fint.model.administrasjon.organisasjon.Organisasjonselement
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.relation.FintResource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes=[FintTestConfiguration.class, OrganisasjonselementCacheService.class])
class OrganisasjonselementCacheServiceSpec extends Specification {

    @Autowired
    OrganisasjonselementCacheService cacheService

    def cleanup() {
        cacheService.remove("mock.no")
    }

    def "Get Organisasjon by Organisasjonsnummer"() {
        given:
        cacheService.createCache("mock.no").add([FintResource.with(new Organisasjonselement(
                organisasjonsId: new Identifikator(identifikatorverdi: "12345"),
                organisasjonsKode: new Identifikator(identifikatorverdi: "ABCD"),
                organisasjonsnummer: new Identifikator(identifikatorverdi: "123456789"),
                organisasjonsnavn: "TEST AS"))])

        when:
        def result = cacheService.getOrganisasjonselementByNummer("mock.no", "123456789")

        then:
        result
        result.isPresent()
        result.get().getResource().organisasjonsnummer.identifikatorverdi == "123456789"
    }

    def "Get Organisation, null Organisasjonsnummer"() {
        given:
        cacheService.createCache("mock.no").add([FintResource.with(new Organisasjonselement(
                organisasjonsId: new Identifikator(identifikatorverdi: "12345"),
                organisasjonsKode: new Identifikator(identifikatorverdi: "ABCD"),
                organisasjonsnavn: "TEST AS"))])

        when:
        def result = cacheService.getOrganisasjonselementByNummer("mock.no", "123456789")

        then:
        result
        !result.isPresent()
    }

    def "Get Organisation by Id"() {
        given:
        cacheService.createCache("mock.no").add([FintResource.with(new Organisasjonselement(
                organisasjonsId: new Identifikator(identifikatorverdi: "12345"),
                organisasjonsKode: new Identifikator(identifikatorverdi: "ABCD"),
                organisasjonsnummer: new Identifikator(identifikatorverdi: "123456789"),
                organisasjonsnavn: "TEST AS"))])

        when:
        def result = cacheService.getOrganisasjonselementById("mock.no", "12345")

        then:
        result
        result.isPresent()
        result.get().getResource().organisasjonsnummer.identifikatorverdi == "123456789"
    }

    def "Get Organisation by Kode"() {
        given:
        cacheService.createCache("mock.no").add([FintResource.with(new Organisasjonselement(
                organisasjonsId: new Identifikator(identifikatorverdi: "12345"),
                organisasjonsKode: new Identifikator(identifikatorverdi: "ABCD"),
                organisasjonsnummer: new Identifikator(identifikatorverdi: "123456789"),
                organisasjonsnavn: "TEST AS"))])

        when:
        def result = cacheService.getOrganisasjonselementByKode("mock.no", "ABCD")

        then:
        result
        result.isPresent()
        result.get().getResource().organisasjonsnummer.identifikatorverdi == "123456789"
    }

    def "Organisation Not Created"() {
        when:
        def result = cacheService.getOrganisasjonselementByNummer("mock.no", "123456789")

        then:
        result
        !result.isPresent()
    }

    def "Organisation Not Found By Kode"() {
        given:
        cacheService.createCache("mock.no").add([FintResource.with(new Organisasjonselement(
                organisasjonsId: new Identifikator(identifikatorverdi: "12345"),
                organisasjonsKode: new Identifikator(identifikatorverdi: "ABCD"),
                organisasjonsnummer: new Identifikator(identifikatorverdi: "123456789"),
                organisasjonsnavn: "TEST AS"))])

        when:
        def result = cacheService.getOrganisasjonselementByKode("mock.no", "XYZZY")

        then:
        result
        !result.isPresent()
    }

    def "Organisation Not Found By Id"() {
        given:
        cacheService.createCache("mock.no").add([FintResource.with(new Organisasjonselement(
                organisasjonsId: new Identifikator(identifikatorverdi: "12345"),
                organisasjonsKode: new Identifikator(identifikatorverdi: "ABCD"),
                organisasjonsnummer: new Identifikator(identifikatorverdi: "123456789"),
                organisasjonsnavn: "TEST AS"))])

        when:
        def result = cacheService.getOrganisasjonselementById("mock.no", "33333")

        then:
        result
        !result.isPresent()
    }

    def "Organisation Not Found By Nummer"() {
        given:
        cacheService.createCache("mock.no").add([FintResource.with(new Organisasjonselement(
                organisasjonsId: new Identifikator(identifikatorverdi: "12345"),
                organisasjonsKode: new Identifikator(identifikatorverdi: "ABCD"),
                organisasjonsnummer: new Identifikator(identifikatorverdi: "123456789"),
                organisasjonsnavn: "TEST AS"))])

        when:
        def result = cacheService.getOrganisasjonselementByNummer("mock.no", "987654321")

        then:
        result
        !result.isPresent()
    }

    def "Wrong Org Id"() {
        given:
        cacheService.createCache("mock.no").add([FintResource.with(new Organisasjonselement(
                organisasjonsId: new Identifikator(identifikatorverdi: "12345"),
                organisasjonsKode: new Identifikator(identifikatorverdi: "ABCD"),
                organisasjonsnummer: new Identifikator(identifikatorverdi: "123456789"),
                organisasjonsnavn: "TEST AS"))])

        when:
        def result = cacheService.getOrganisasjonselementByNummer("fake.no", "123456789")

        then:
        result
        !result.isPresent()
    }

}
