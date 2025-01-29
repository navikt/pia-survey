package no.nav.pia.survey.api.vert

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.pia.survey.api.dto.tilDto
import no.nav.pia.survey.api.surveyId
import no.nav.pia.survey.domene.SurveyService

const val VERT_BASEPATH = "/vert"

fun Route.vertApi(surveyService: SurveyService) {
    get("$VERT_BASEPATH/{opphav}/{type}/{eksternId}") {
        val opphav = call.parameters["opphav"] ?: return@get call.respond(status = HttpStatusCode.BadRequest, message = "")
        val type = call.parameters["type"] ?: return@get call.respond(status = HttpStatusCode.BadRequest, message = "")
        val eksternId = call.parameters["eksternId"] ?: return@get call.respond(status = HttpStatusCode.BadRequest, message = "")

        surveyService.hentSurvey(opphav = opphav, type = type, eksternId = eksternId)?.let {
            return@get call.respondRedirect("$VERT_BASEPATH/survey/${it.id}", true)
        } ?: return@get call.respond(HttpStatusCode.NotFound)
    }

    get("$VERT_BASEPATH/survey/{surveyId}") {
        val surveyId = call.surveyId

        surveyService.hentSurvey(id = surveyId)?.let {
            return@get call.respond(it.tilDto())
        } ?: return@get call.respond(HttpStatusCode.NotFound)
    }

    get("$VERT_BASEPATH/survey/{surveyId}/antall-deltakere") {
        val surveyId = call.surveyId

        surveyService.hentSurvey(surveyId)?.let {
            return@get call.respond(surveyService.hentAntallDeltakere(surveyId))
        } ?: return@get call.respond(HttpStatusCode.NotFound)
    }
}
