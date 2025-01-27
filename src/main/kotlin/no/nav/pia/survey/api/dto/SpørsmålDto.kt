package no.nav.pia.survey.api.dto

import kotlinx.serialization.Serializable
import no.nav.pia.survey.domene.Spørsmål

@Serializable
data class SpørsmålDto(
    val id: String,
    val flervalg: Boolean,
    val tekst: String,
    val kategori: String?,
    val svaralternativ: List<SvaralternativDto>,
)

fun List<Spørsmål>.tilDto() = map { it.tilDto() }

fun Spørsmål.tilDto() =
    SpørsmålDto(
        id = id.toString(),
        flervalg = flervalg,
        tekst = tekst,
        kategori = kategori,
        svaralternativ = svaralternativ.tilDto(),
    )
