package no.nav.pia.survey.api

import io.ktor.http.HttpStatusCode

internal class Feil(
    val feilmelding: String? = null,
    val opprinneligException: Throwable? = null,
    val feilkode: HttpStatusCode,
) : Throwable(feilmelding, opprinneligException)
