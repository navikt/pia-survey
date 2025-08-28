package no.nav.pia.survey.domene

import kotlinx.datetime.LocalDateTime
import java.util.UUID

class Survey(
    val id: UUID,
    val eksternId: String,
    val opphav: String,
    val type: String,
    val status: Status,
    val opprettet: LocalDateTime,
    val endret: LocalDateTime?,
    val gyldigTil: LocalDateTime,
    val temaer: List<Tema>,
) {
    enum class Status {
        OPPRETTET,
        PÅBEGYNT,
        AVSLUTTET,
        SLETTET,
    }
}
