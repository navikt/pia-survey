package no.nav.pia.survey.helper

import io.ktor.client.call.body
import no.nav.pia.survey.api.deltaker.BLI_MED_PATH
import no.nav.pia.survey.api.dto.BliMedDto
import no.nav.pia.survey.api.dto.BliMedRequest
import no.nav.pia.survey.api.dto.SurveyDto
import no.nav.pia.survey.helper.TestContainerHelper.Companion.piaSurveyContainer

internal suspend fun bliMed(surveyId: String) =
    piaSurveyContainer.performPost(BLI_MED_PATH, BliMedRequest(surveyId))
        .body<BliMedDto>()

internal suspend fun SurveyDto.bliMed() = bliMed(id)
