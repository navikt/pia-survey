package no.nav.pia.survey.helper

import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import no.nav.pia.survey.api.dto.SurveyDto
import no.nav.pia.survey.api.vert.VERT_BASEPATH
import no.nav.pia.survey.helper.TestContainerHelper.Companion.authContainer
import no.nav.pia.survey.helper.TestContainerHelper.Companion.piaSurveyContainer

internal suspend fun SurveyDto.hentAntallDeltakere(token: String = authContainer.issueToken().serialize()) =
    piaSurveyContainer.performGet("$VERT_BASEPATH/survey/$id/antall-deltakere") {
        bearerAuth(token)
    }.body<Int>()

internal suspend fun hentSurveySomVert(
    opphav: String,
    type: String,
    eksternId: String,
    token: String = authContainer.issueToken().serialize(),
) = piaSurveyContainer.performGet("$VERT_BASEPATH/$opphav/$type/$eksternId") {
    bearerAuth(token)
}.body<SurveyDto>()

internal suspend fun no.nav.pia.survey.kafka.dto.SpørreundersøkelseDto.hentSurveySomVert() = hentSurveySomVert(opphav, type, id)
