package no.nav.pia.survey.domene

import ia.felles.integrasjoner.kafkameldinger.spørreundersøkelse.SpørreundersøkelseStatus
import kotlinx.datetime.LocalDateTime
import java.util.UUID

data class Survey(
    val id: UUID,
    val eksternId: String,
    val opphav: String,
    val type: String,
    val status: SpørreundersøkelseStatus,
    val opprettet: LocalDateTime,
    val endret: LocalDateTime?,
    val gyldigTil: LocalDateTime,
    val temaer: List<Tema>,
)
