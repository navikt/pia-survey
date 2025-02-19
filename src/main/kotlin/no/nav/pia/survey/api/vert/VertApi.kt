package no.nav.pia.survey.api.vert

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import no.nav.pia.survey.api.Feil
import no.nav.pia.survey.api.StandarFeil
import no.nav.pia.survey.api.dto.tilDto
import no.nav.pia.survey.api.surveyId
import no.nav.pia.survey.api.temaId
import no.nav.pia.survey.domene.SurveyService
import no.nav.pia.survey.domene.Tema

const val VERT_BASEPATH = "/vert"

fun Route.vertApi(surveyService: SurveyService) {
    get("$VERT_BASEPATH/landing/{opphav}/{type}/{eksternId}") {
        val opphav = call.parameters["opphav"] ?: throw Feil(feilmelding = "Mangler opphav", feilkode = HttpStatusCode.BadRequest)
        val type = call.parameters["type"] ?: throw Feil(feilmelding = "Mangler type", feilkode = HttpStatusCode.BadRequest)
        val eksternId = call.parameters["eksternId"] ?: throw Feil(feilmelding = "Mangler eksternId", feilkode = HttpStatusCode.BadRequest)

        surveyService.hentSurvey(opphav = opphav, type = type, eksternId = eksternId)?.let {
            return@get call.respond(it.tilDto())
        } ?: throw StandarFeil.fantIkkeSurvey
    }

    get("$VERT_BASEPATH/survey/{surveyId}") {
        val surveyId = call.surveyId

        surveyService.hentSurvey(id = surveyId)?.let {
            return@get call.respond(it.tilDto())
        } ?: throw StandarFeil.fantIkkeSurvey
    }

    get("$VERT_BASEPATH/antall-deltakere/{surveyId}") {
        val surveyId = call.surveyId

        surveyService.hentSurvey(surveyId)?.let {
            return@get call.respond(surveyService.hentAntallDeltakere(surveyId))
        } ?: throw StandarFeil.fantIkkeSurvey
    }

    put("$VERT_BASEPATH/status/{surveyId}/{temaId}") {
        val surveyId = call.surveyId
        val temaId = call.temaId
        val status = call.receive<Tema.Companion.Status>()

        surveyService.hentTema(surveyId, temaId)?.let { tema ->
            surveyService.oppdaterTemaStatus(tema, status)?.let {
                return@put call.respond(it.tilDto())
            } ?: throw Feil(feilmelding = "Kunne ikke oppdatere tema status", feilkode = HttpStatusCode.InternalServerError)
        } ?: throw StandarFeil.fantIkkeSurvey
    }
}
