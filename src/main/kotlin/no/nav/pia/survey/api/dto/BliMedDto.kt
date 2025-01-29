package no.nav.pia.survey.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class BliMedDto(
    val surveyId: String,
    val sesjonsId: String,
)
