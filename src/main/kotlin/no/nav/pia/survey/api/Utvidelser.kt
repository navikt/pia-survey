package no.nav.pia.survey.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import java.util.UUID

internal val ApplicationCall.surveyId
    get() =
        parameters["surveyId"]?.let {
            UUID.fromString(it)
        } ?: throw Feil(feilmelding = "Mangler surveyId", feilkode = HttpStatusCode.BadRequest)

internal val ApplicationCall.temaId
    get() =
        parameters["temaId"]?.let {
            UUID.fromString(it)
        } ?: throw Feil(feilmelding = "Mangler temaId", feilkode = HttpStatusCode.BadRequest)
