package no.nav.pia.survey

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import no.nav.pia.survey.helse.ApplikasjonsHelse
import no.nav.pia.survey.helse.helse

fun Application.configureRouting(applikasjonsHelse: ApplikasjonsHelse) {
    routing {
        helse(
            lever = { applikasjonsHelse.alive },
            klar = { applikasjonsHelse.ready },
        )
    }
}
