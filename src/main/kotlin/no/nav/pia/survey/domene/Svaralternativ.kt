package no.nav.pia.survey.domene

import java.util.UUID

data class Svaralternativ(
    val id: UUID,
    val eksternId: String,
    val tekst: String,
)
