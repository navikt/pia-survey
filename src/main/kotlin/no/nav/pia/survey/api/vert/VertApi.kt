package no.nav.pia.survey.api.vert

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import no.nav.pia.survey.api.dto.tilDto
import no.nav.pia.survey.api.surveyId
import no.nav.pia.survey.api.temaId
import no.nav.pia.survey.domene.SurveyService
import no.nav.pia.survey.domene.Tema

const val VERT_BASEPATH = "/vert"

fun Route.vertApi(surveyService: SurveyService) {
    get("$VERT_BASEPATH/landing/{opphav}/{type}/{eksternId}") {
        val opphav = call.parameters["opphav"] ?: return@get call.respond(status = HttpStatusCode.BadRequest, message = "")
        val type = call.parameters["type"] ?: return@get call.respond(status = HttpStatusCode.BadRequest, message = "")
        val eksternId = call.parameters["eksternId"] ?: return@get call.respond(status = HttpStatusCode.BadRequest, message = "")

        surveyService.hentSurvey(opphav = opphav, type = type, eksternId = eksternId)?.let {
            return@get call.respond(it.tilDto())
        } ?: return@get call.respond(HttpStatusCode.NotFound)
    }

    get("$VERT_BASEPATH/survey/{surveyId}") {
        val surveyId = call.surveyId

        surveyService.hentSurvey(id = surveyId)?.let {
            return@get call.respond(it.tilDto())
        } ?: return@get call.respond(HttpStatusCode.NotFound)
    }

    get("$VERT_BASEPATH/antall-deltakere/{surveyId}") {
        val surveyId = call.surveyId

        surveyService.hentSurvey(surveyId)?.let {
            return@get call.respond(surveyService.hentAntallDeltakere(surveyId))
        } ?: return@get call.respond(HttpStatusCode.NotFound)
    }

    put("$VERT_BASEPATH/status/{surveyId}/{temaId}") {
        val surveyId = call.surveyId
        val temaId = call.temaId
        val status = call.receive<Tema.Companion.Status>()

        surveyService.hentTema(surveyId, temaId)?.let { tema ->
            surveyService.oppdaterTemaStatus(tema, status)?.let {
                return@put call.respond(it.tilDto())
            } ?: call.respond(HttpStatusCode.InternalServerError)
        } ?: call.respond(HttpStatusCode.NotFound)
    }
}
