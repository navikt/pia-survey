package no.nav.pia.survey.kafka.dto

import kotlinx.serialization.Serializable

@Serializable
data class SvaralternativDto(
    val id: String,
    val tekst: String,
)
