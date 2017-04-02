module.exports = {
    corrId: '',
    action: 'GET_ALL_ORGANISASJONSELEMENT',
    status: 'UPSTREAM_QUEUE',
    time: new Date().getTime(),
    orgId: '',
    source: 'fk',
    client: 'vfs',
    message: null,
    data: [
        {
            resource: {
                aktiv: false,
                organisasjonsId: {
                    identifikatorverdi: "12345"
                }
            },
            type: "no.fint.model.administrasjon.organisasjon.Organisasjonselement",
            "relasjoner": []
        }
    ]
}