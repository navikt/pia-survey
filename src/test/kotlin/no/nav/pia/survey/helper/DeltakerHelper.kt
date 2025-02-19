package no.nav.pia.survey.helper

import HEADER_SESJON_ID
import io.ktor.client.call.body
import io.ktor.client.request.header
import no.nav.pia.survey.api.deltaker.BLI_MED_PATH
import no.nav.pia.survey.api.deltaker.DELTAKER_BASEPATH
import no.nav.pia.survey.api.dto.BliMedDto
import no.nav.pia.survey.api.dto.BliMedRequest
import no.nav.pia.survey.api.dto.IdentifiserbartSpørsmålDto
import no.nav.pia.survey.api.dto.SurveyDto
import no.nav.pia.survey.helper.TestContainerHelper.Companion.piaSurveyContainer

internal suspend fun bliMed(surveyId: String) =
    piaSurveyContainer.performPost(BLI_MED_PATH, BliMedRequest(surveyId))
        .body<BliMedDto>()

internal suspend fun SurveyDto.bliMed() = bliMed(id)

internal suspend fun SurveyDto.hentFøsteSpørsmål(sesjonId: String? = null) =
    piaSurveyContainer.performGet("$DELTAKER_BASEPATH/$id") {
        sesjonId?.let {
            header(HEADER_SESJON_ID, it)
        }
    }.body<IdentifiserbartSpørsmålDto>()
