package no.nav.pia.survey.domene

import java.util.UUID

class Spørsmål(
    val id: UUID,
    val eksternId: String,
    val flervalg: Boolean,
    val tekst: String,
    val kategori: String?,
    val svaralternativ: List<Svaralternativ>,
)
