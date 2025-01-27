package no.nav.pia.survey.kafka.dto

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class PlanUndertemaDto(
    val id: Int,
    val navn: String,
    val målsetning: String,
    val inkludert: Boolean,
    val status: String?,
    val startDato: LocalDate?,
    val sluttDato: LocalDate?,
)
