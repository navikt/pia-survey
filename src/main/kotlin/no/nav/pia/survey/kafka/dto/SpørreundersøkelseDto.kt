package no.nav.pia.survey.kafka.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import no.nav.pia.survey.domene.Survey

@Serializable
data class SpørreundersøkelseDto(
    val id: String,
    val orgnummer: String = "",
    val samarbeidsNavn: String = "",
    val virksomhetsNavn: String = "",
    val status: Survey.Status,
    val temaer: List<TemaDto>,
    val type: String,
    val opprettet: LocalDateTime,
    val endret: LocalDateTime?,
    val gyldigTil: LocalDateTime,
    val plan: PlanDto? = null,
    val opphav: String = "fia",
)
