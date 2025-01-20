package no.nav.pia.survey.dto

import kotlinx.serialization.Serializable

@Serializable
data class SvaralternativDto(
    val id: String,
    val tekst: String,
)
