package no.nav.pia.survey

internal object MiljøVariabler {
    val cluster = Cluster.valueOf(hentMiljøValiabel("NAIS_CLUSTER_NAME", "prod-gcp"))

    val kafkaBrokers: String = hentMiljøValiabel("KAFKA_BROKERS")
    val kafkaTruststoreLocation: String = hentMiljøValiabel("KAFKA_TRUSTSTORE_PATH")
    val kafkaKeystoreLocation: String = hentMiljøValiabel("KAFKA_KEYSTORE_PATH")
    val kafkaCredstorePassword: String = hentMiljøValiabel("KAFKA_CREDSTORE_PASSWORD")

    private fun hentMiljøValiabel(
        variabelNavn: String,
        defaultVerdi: String? = null,
    ) = System.getenv(variabelNavn) ?: defaultVerdi ?: throw RuntimeException("Mangler miljø variabel $variabelNavn")
}
