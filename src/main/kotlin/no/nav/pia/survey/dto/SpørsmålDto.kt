package no.nav.pia.survey.dto

import kotlinx.serialization.Serializable

@Serializable
data class SpørsmålDto(
    val id: String,
    val tekst: String,
    val flervalg: Boolean,
    val svaralternativer: List<SvaralternativDto>,
    val kategori: String? = null,
)
