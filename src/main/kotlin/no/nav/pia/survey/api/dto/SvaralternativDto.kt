package no.nav.pia.survey.api.dto

import kotlinx.serialization.Serializable
import no.nav.pia.survey.domene.Svaralternativ

@Serializable
data class SvaralternativDto(
    val id: String,
    val tekst: String,
)

fun List<Svaralternativ>.tilDto() = map { it.tilDto() }

fun Svaralternativ.tilDto() =
    SvaralternativDto(
        id = id.toString(),
        tekst = tekst,
    )
