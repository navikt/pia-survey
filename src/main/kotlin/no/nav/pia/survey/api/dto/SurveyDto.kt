package no.nav.pia.survey.api.dto

import kotlinx.serialization.Serializable
import no.nav.pia.survey.domene.Survey

@Serializable
data class SurveyDto(
    val id: String,
    val type: String,
    val status: Survey.Status,
    val temaer: List<TemaDto>,
)

fun Survey.tilDto() =
    SurveyDto(
        id = id.toString(),
        type = type,
        status = status,
        temaer = temaer.tilDto(),
    )
