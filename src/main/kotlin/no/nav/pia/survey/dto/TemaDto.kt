package no.nav.pia.survey.dto

import kotlinx.serialization.Serializable

@Serializable
data class TemaDto(
    val id: Int,
    val navn: String,
    val spørsmål: List<SpørsmålDto>,
)
