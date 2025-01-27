package no.nav.pia.survey.kafka.dto

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class PlanDto(
    val id: String,
    val sistEndret: LocalDateTime,
    val sistPublisert: LocalDate?,
    val temaer: List<PlanTemaDto>,
)
