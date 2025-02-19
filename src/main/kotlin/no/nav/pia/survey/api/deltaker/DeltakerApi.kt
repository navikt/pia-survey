package no.nav.pia.survey.api.deltaker

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import no.nav.pia.survey.api.Feil
import no.nav.pia.survey.api.dto.BliMedDto
import no.nav.pia.survey.api.dto.BliMedRequest
import no.nav.pia.survey.domene.SurveyService
import java.util.UUID

const val BLI_MED_PATH = "/bli-med"

fun Route.bliMedApi(surveyService: SurveyService) {
    post(BLI_MED_PATH) {
        val bliMedRequest = call.receive<BliMedRequest>()
        val surveyId = try {
            UUID.fromString(bliMedRequest.surveyId)
        } catch (e: Exception) {
            throw Feil(feilmelding = "Ikke gyldig uuid", feilkode = HttpStatusCode.BadRequest)
        }

        val sesjonId = surveyService.bliMed(surveyId)
            ?: throw Feil(feilmelding = "Fant ikke survey", feilkode = HttpStatusCode.NotFound)

        call.respond(
            BliMedDto(
                surveyId = surveyId.toString(),
                sesjonsId = sesjonId.toString(),
            ),
        )
    }
}

const val DELTAKER_BASEPATH = "/deltaker"

fun Route.deltakerApi(surveyService: SurveyService) {
    get("$DELTAKER_BASEPATH/{surveyId}") {
    }
}
