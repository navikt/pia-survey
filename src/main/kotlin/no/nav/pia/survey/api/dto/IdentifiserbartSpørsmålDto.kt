package no.nav.pia.survey.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class IdentifiserbartSpørsmålDto(
    val temaId: String,
    val spørsmålId: String,
)
