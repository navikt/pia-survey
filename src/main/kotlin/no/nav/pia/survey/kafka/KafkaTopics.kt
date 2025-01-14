package no.nav.pia.survey.kafka

import no.nav.pia.survey.kafka.KafkaConfig.Companion.CLIENT_ID

enum class KafkaTopics(
    val navn: String,
    private val prefix: String = "pia",
) {
    SPØRREUNDERSØKELSE("sporreundersokelse-v1"),
    ;

    val konsumentGruppe
        get() = "${navn}_$CLIENT_ID"

    val navnMedNamespace
        get() = "$prefix.$navn"
}
