package no.nav.pia.survey

import VerifisertSesjonId
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.RouteSelector
import io.ktor.server.routing.RouteSelectorEvaluation
import io.ktor.server.routing.RoutingResolveContext
import io.ktor.server.routing.routing
import no.nav.pia.survey.api.deltaker.bliMedApi
import no.nav.pia.survey.api.deltaker.deltakerApi
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

        bliMedApi(surveyService = surveyService)

        medVerifisertSesjonId(surveyService = surveyService) {
            deltakerApi(surveyService = surveyService)
        }

        authenticate {
            vertApi(surveyService = surveyService)
        }
    }
}

fun Route.medVerifisertSesjonId(
    surveyService: SurveyService,
    authorizedRoutes: Route.() -> Unit,
) = createChild(CustomSelector()).apply {
    install(VerifisertSesjonId(surveyService = surveyService))
    authorizedRoutes()
}

private class CustomSelector : RouteSelector() {
    override suspend fun evaluate(
        context: RoutingResolveContext,
        segmentIndex: Int,
    ) = RouteSelectorEvaluation.Transparent
}
