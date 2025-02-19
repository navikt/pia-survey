package no.nav.pia.survey.api.dto

import kotlinx.serialization.Serializable
import no.nav.pia.survey.domene.Tema

@Serializable
data class TemaDto(
    val id: String,
    val navn: String,
    val spørsmål: List<SpørsmålDto>,
    val status: Tema.Companion.Status,
)

fun List<Tema>.tilDto() = map { it.tilDto() }

fun Tema.tilDto() =
    TemaDto(
        id = id.toString(),
        navn = navn,
        spørsmål = spørsmål.tilDto(),
        status = status,
    )
