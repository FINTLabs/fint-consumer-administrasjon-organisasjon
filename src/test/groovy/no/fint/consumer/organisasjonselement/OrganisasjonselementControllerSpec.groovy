package no.fint.consumer.organisasjonselement

import no.fint.audit.FintAuditService
import no.fint.consumer.config.ConsumerProps
import no.fint.consumer.models.organisasjonselement.OrganisasjonselementAssembler
import no.fint.consumer.models.organisasjonselement.OrganisasjonselementCacheService
import no.fint.consumer.models.organisasjonselement.OrganisasjonselementController
import no.fint.consumer.utils.RestEndpoints
import no.fint.event.model.HeaderConstants
import no.fint.model.administrasjon.organisasjon.Organisasjonselement
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.relation.FintResource
import no.fint.test.utils.MockMvcSpecification
import org.springframework.http.ResponseEntity
import org.springframework.test.web.servlet.MockMvc

class OrganisasjonselementControllerSpec extends MockMvcSpecification {
    private OrganisasjonselementController controller
    private OrganisasjonselementCacheService cacheService
    private OrganisasjonselementAssembler assembler
    private MockMvc mockMvc
    private ConsumerProps props

    void setup() {
        cacheService = Mock(OrganisasjonselementCacheService)
        assembler = Mock(OrganisasjonselementAssembler) {
            resource(_) >> ResponseEntity.ok().build()
            resources(_) >> ResponseEntity.ok().build()
        }
        props = new ConsumerProps(overrideOrgId: false, defaultClient: "TEST", defaultOrgId: "mock.no")
        controller = new OrganisasjonselementController(cacheService: cacheService, assembler: assembler, fintAuditService: Mock(FintAuditService), props: props)
        mockMvc = standaloneSetup(controller)
    }

    def "GET last updated"() {
        when:
        def response = mockMvc.perform(get("${RestEndpoints.ORGANISASJONSELEMENT}/last-updated")
                .header(HeaderConstants.ORG_ID, 'mock.no'))

        then:
        1 * cacheService.getLastUpdated(_ as String) >> 123L
        response.andExpect(status().isOk())
                .andExpect(jsonPathEquals('$.lastUpdated', '123'))
    }

    def "GET organisasjonselementer"() {
        when:
        def response = mockMvc.perform(get(RestEndpoints.ORGANISASJONSELEMENT)
                .header(HeaderConstants.ORG_ID, 'rogfk.no')
                .header(HeaderConstants.CLIENT, 'test')
        )

        then:
        1 * cacheService.getAll('rogfk.no') >> [FintResource.with(new Organisasjonselement()), FintResource.with(new Organisasjonselement())]
        response.andExpect(status().isOk())
    }

    def "GET organisasjonselement by organisasjonsid"() {
        when:
        def response = mockMvc.perform(get("${RestEndpoints.ORGANISASJONSELEMENT}/organisasjonsid/123")
                .header(HeaderConstants.ORG_ID, 'rogfk.no')
                .header(HeaderConstants.CLIENT, 'test')
        )

        then:
        1 * cacheService.getOrganisasjonselementByOrganisasjonsId('rogfk.no', '123') >> [FintResource.with(new Organisasjonselement(organisasjonsId: new Identifikator(identifikatorverdi: '123')))]
        response.andExpect(status().isOk())
    }

    def "GET organisasjonselement by organisasjonskode"() {
        when:
        def response = mockMvc.perform(get("${RestEndpoints.ORGANISASJONSELEMENT}/organisasjonskode/abc")
                .header(HeaderConstants.ORG_ID, 'rogfk.no')
                .header(HeaderConstants.CLIENT, 'test')
        )

        then:
        1 * cacheService.getOrganisasjonselementByOrganisasjonsKode('rogfk.no', 'abc') >> [FintResource.with(new Organisasjonselement(organisasjonsKode: new Identifikator(identifikatorverdi: 'abc')))]
        response.andExpect(status().isOk())
    }

    def "GET organisasjonselement by organisasjonsnummer"() {
        when:
        def response = mockMvc.perform(get("${RestEndpoints.ORGANISASJONSELEMENT}/organisasjonsnummer/123456789")
                .header(HeaderConstants.ORG_ID, 'rogfk.no')
                .header(HeaderConstants.CLIENT, 'test')
        )

        then:
        1 * cacheService.getOrganisasjonselementByOrganisasjonsnummer('rogfk.no', '123456789') >> [FintResource.with(new Organisasjonselement(organisasjonsKode: new Identifikator(identifikatorverdi: 'abc')))]
        response.andExpect(status().isOk())
    }
}
