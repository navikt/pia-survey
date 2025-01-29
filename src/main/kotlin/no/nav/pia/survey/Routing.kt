package no.nav.pia.survey

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.routing
import no.nav.pia.survey.api.helse.helse
import no.nav.pia.survey.api.vert.vertApi
import no.nav.pia.survey.domene.SurveyService

fun Application.configureRouting(
    applikasjonsHelse: ApplikasjonsHelse,
    surveyService: SurveyService,
) {
    routing {
        helse(
            lever = { applikasjonsHelse.alive },
            klar = { applikasjonsHelse.ready },
        )
        authenticate {
            vertApi(surveyService = surveyService)
        }
    }
}
