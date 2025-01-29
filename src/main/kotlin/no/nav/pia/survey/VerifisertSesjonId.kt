import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.request.header
import no.nav.pia.survey.api.Feil
import no.nav.pia.survey.api.surveyId
import no.nav.pia.survey.domene.SurveyService
import java.util.UUID

const val HEADER_SESJON_ID = "nav-fia-kartlegging-sesjon-id"

@Suppress("ktlint:standard:function-naming")
fun VerifisertSesjonId(surveyService: SurveyService) =
    createRouteScopedPlugin("VerifisertSesjonId") {
        pluginConfig.apply {
            onCall { call ->
                surveyService.hentDeltaker(
                    sesjonId = call.sesjonId,
                    surveyId = call.surveyId,
                ) ?: throw Feil(feilmelding = "Ugyldig sesjonId", feilkode = HttpStatusCode.Forbidden)
            }
        }
    }

val ApplicationCall.sesjonId
    get() =
        request.header(HEADER_SESJON_ID)?.let {
            UUID.fromString(it)
        } ?: throw Feil(
            feilmelding = "Mangler sesjonId",
            feilkode = HttpStatusCode.Forbidden,
        )
