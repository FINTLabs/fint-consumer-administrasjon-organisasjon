package no.fint.consumer.organisasjonselement

import no.fint.cache.exceptions.CacheNotFoundException
import no.fint.consumer.config.FintTestConfiguration
import no.fint.consumer.event.SynchronousEvents
import no.fint.consumer.models.organisasjonselement.OrganisasjonselementCacheService
import no.fint.consumer.models.organisasjonselement.OrganisasjonselementLinker
import no.novari.fint.model.felles.kompleksedatatyper.Identifikator
import no.novari.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes=[FintTestConfiguration, OrganisasjonselementLinker, OrganisasjonselementCacheService, SynchronousEvents])
class OrganisasjonselementCacheServiceSpec extends Specification {

    @Autowired
    OrganisasjonselementCacheService cacheService

    def cleanup() {
        cacheService.remove("mock.no")
    }

    def "Get Organisasjon by Organisasjonsnummer"() {
        given:
        cacheService.createCache("mock.no").add([new OrganisasjonselementResource(
                organisasjonsId: new Identifikator(identifikatorverdi: "12345"),
                organisasjonsKode: new Identifikator(identifikatorverdi: "ABCD"),
                organisasjonsnummer: new Identifikator(identifikatorverdi: "123456789"),
                organisasjonsnavn: "TEST AS")])

        when:
        def result = cacheService.getOrganisasjonselementByOrganisasjonsnummer("mock.no", "123456789")

        then:
        result
        result.isPresent()
        result.get().organisasjonsnummer.identifikatorverdi == "123456789"
    }

    def "Get Organisation, null Organisasjonsnummer"() {
        given:
        cacheService.createCache("mock.no").add([new OrganisasjonselementResource(
                organisasjonsId: new Identifikator(identifikatorverdi: "12345"),
                organisasjonsKode: new Identifikator(identifikatorverdi: "ABCD"),
                organisasjonsnavn: "TEST AS")])

        when:
        def result = cacheService.getOrganisasjonselementByOrganisasjonsnummer("mock.no", "123456789")

        then:
        result
        !result.isPresent()
    }

    def "Get Organisation by Id"() {
        given:
        cacheService.createCache("mock.no").add([new OrganisasjonselementResource(
                organisasjonsId: new Identifikator(identifikatorverdi: "12345"),
                organisasjonsKode: new Identifikator(identifikatorverdi: "ABCD"),
                organisasjonsnummer: new Identifikator(identifikatorverdi: "123456789"),
                organisasjonsnavn: "TEST AS")])

        when:
        def result = cacheService.getOrganisasjonselementByOrganisasjonsId("mock.no", "12345")

        then:
        result
        result.isPresent()
        result.get().organisasjonsnummer.identifikatorverdi == "123456789"
    }

    def "Get Organisation by Kode"() {
        given:
        cacheService.createCache("mock.no").add([new OrganisasjonselementResource(
                organisasjonsId: new Identifikator(identifikatorverdi: "12345"),
                organisasjonsKode: new Identifikator(identifikatorverdi: "ABCD"),
                organisasjonsnummer: new Identifikator(identifikatorverdi: "123456789"),
                organisasjonsnavn: "TEST AS")])

        when:
        def result = cacheService.getOrganisasjonselementByOrganisasjonsKode("mock.no", "ABCD")

        then:
        result
        result.isPresent()
        result.get().organisasjonsnummer.identifikatorverdi == "123456789"
    }

    def "Organisation Not Created"() {
        when:
        cacheService.getOrganisasjonselementByOrganisasjonsnummer("mock.no", "123456789")

        then:
        thrown(CacheNotFoundException)
    }

    def "Organisation Not Found By Kode"() {
        given:
        cacheService.createCache("mock.no").add([new OrganisasjonselementResource(
                organisasjonsId: new Identifikator(identifikatorverdi: "12345"),
                organisasjonsKode: new Identifikator(identifikatorverdi: "ABCD"),
                organisasjonsnummer: new Identifikator(identifikatorverdi: "123456789"),
                organisasjonsnavn: "TEST AS")])

        when:
        def result = cacheService.getOrganisasjonselementByOrganisasjonsKode("mock.no", "XYZZY")

        then:
        result
        !result.isPresent()
    }

    def "Organisation Not Found By Id"() {
        given:
        cacheService.createCache("mock.no").add([new OrganisasjonselementResource(
                organisasjonsId: new Identifikator(identifikatorverdi: "12345"),
                organisasjonsKode: new Identifikator(identifikatorverdi: "ABCD"),
                organisasjonsnummer: new Identifikator(identifikatorverdi: "123456789"),
                organisasjonsnavn: "TEST AS")])

        when:
        def result = cacheService.getOrganisasjonselementByOrganisasjonsId("mock.no", "33333")

        then:
        result
        !result.isPresent()
    }

    def "Organisation Not Found By Nummer"() {
        given:
        cacheService.createCache("mock.no").add([new OrganisasjonselementResource(
                organisasjonsId: new Identifikator(identifikatorverdi: "12345"),
                organisasjonsKode: new Identifikator(identifikatorverdi: "ABCD"),
                organisasjonsnummer: new Identifikator(identifikatorverdi: "123456789"),
                organisasjonsnavn: "TEST AS")])

        when:
        def result = cacheService.getOrganisasjonselementByOrganisasjonsnummer("mock.no", "987654321")

        then:
        result
        !result.isPresent()
    }

    def "Wrong Org Id"() {
        given:
        cacheService.createCache("mock.no").add([new OrganisasjonselementResource(
                organisasjonsId: new Identifikator(identifikatorverdi: "12345"),
                organisasjonsKode: new Identifikator(identifikatorverdi: "ABCD"),
                organisasjonsnummer: new Identifikator(identifikatorverdi: "123456789"),
                organisasjonsnavn: "TEST AS")])

        when:
        cacheService.getOrganisasjonselementByOrganisasjonsnummer("fake.no", "123456789")

        then:
        thrown(CacheNotFoundException)
    }

}