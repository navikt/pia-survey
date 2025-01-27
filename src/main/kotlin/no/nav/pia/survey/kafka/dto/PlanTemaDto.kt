package no.nav.pia.survey.kafka.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlanTemaDto(
    val id: Int,
    val navn: String,
    val inkludert: Boolean,
    val undertemaer: List<PlanUndertemaDto>,
)
