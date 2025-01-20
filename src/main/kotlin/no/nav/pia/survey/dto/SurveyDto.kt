package no.nav.pia.survey.dto

import ia.felles.integrasjoner.kafkameldinger.spørreundersøkelse.SpørreundersøkelseStatus
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class SurveyDto(
    val id: String,
    val orgnummer: String,
    val samarbeidsNavn: String,
    val virksomhetsNavn: String,
    val status: SpørreundersøkelseStatus,
    val temaer: List<TemaDto>,
    val type: String,
    val opprettet: LocalDateTime,
    val endret: LocalDateTime?,
    val gyldigTil: LocalDateTime,
    val plan: PlanDto?,
)
