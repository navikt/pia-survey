package no.nav.pia.survey.api.deltaker

import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.pia.survey.domene.SurveyService

const val DELTAKER_BASEPATH = "/vert"

fun Route.deltakerApi(surveyService: SurveyService) {
    get("$DELTAKER_BASEPATH/{id}") { }
}
