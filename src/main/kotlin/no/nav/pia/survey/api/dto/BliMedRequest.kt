package no.nav.pia.survey.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class BliMedRequest(
    val surveyId: String,
)
