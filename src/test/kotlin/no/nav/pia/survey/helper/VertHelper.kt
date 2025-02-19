package no.nav.pia.survey.helper

import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import no.nav.pia.survey.api.dto.SurveyDto
import no.nav.pia.survey.api.dto.TemaDto
import no.nav.pia.survey.api.vert.VERT_BASEPATH
import no.nav.pia.survey.domene.Tema
import no.nav.pia.survey.helper.TestContainerHelper.Companion.authContainer
import no.nav.pia.survey.helper.TestContainerHelper.Companion.piaSurveyContainer

internal suspend fun SurveyDto.hentAntallDeltakere(token: String = authContainer.issueToken().serialize()) =
    piaSurveyContainer.performGet("$VERT_BASEPATH/antall-deltakere/$id") {
        bearerAuth(token)
    }.body<Int>()

internal suspend fun hentSurveySomVert(
    opphav: String,
    type: String,
    eksternId: String,
    token: String = authContainer.issueToken().serialize(),
) = piaSurveyContainer.performGet("$VERT_BASEPATH/landing/$opphav/$type/$eksternId") {
    bearerAuth(token)
}.body<SurveyDto>()

internal suspend fun no.nav.pia.survey.kafka.dto.SpørreundersøkelseDto.hentSurveySomVert() = hentSurveySomVert(opphav, type, id)

internal suspend fun SurveyDto.oppdaterTemaStatus(
    tema: TemaDto,
    status: Tema.Companion.Status,
    token: String = authContainer.issueToken().serialize(),
) = piaSurveyContainer.performPut(
    url = "$VERT_BASEPATH/status/$id/${tema.id}",
    body = status,
) {
    bearerAuth(token)
}.body<TemaDto>()
