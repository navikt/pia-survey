package no.nav.pia.survey.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import java.util.UUID

internal val ApplicationCall.surveyId
    get() =
        parameters["surveyId"]?.let {
            UUID.fromString(it)
        } ?: throw Feil(feilmelding = "", feilkode = HttpStatusCode.BadRequest)
