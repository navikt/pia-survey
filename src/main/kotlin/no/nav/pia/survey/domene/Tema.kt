package no.nav.pia.survey.domene

import java.util.UUID

class Tema(
    val id: UUID,
    val eksternId: String,
    val navn: String,
    val spørsmål: List<Spørsmål>,
)
