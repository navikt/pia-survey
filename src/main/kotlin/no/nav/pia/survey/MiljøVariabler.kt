package no.nav.pia.survey

internal object MiljøVariabler {
    // -- nais cluster
    val cluster = Cluster.valueOf(hentMiljøValiabel("NAIS_CLUSTER_NAME", "prod-gcp"))

    // -- kafka
    val kafkaBrokers: String = hentMiljøValiabel("KAFKA_BROKERS")
    val kafkaTruststoreLocation: String = hentMiljøValiabel("KAFKA_TRUSTSTORE_PATH")
    val kafkaKeystoreLocation: String = hentMiljøValiabel("KAFKA_KEYSTORE_PATH")
    val kafkaCredstorePassword: String = hentMiljøValiabel("KAFKA_CREDSTORE_PASSWORD")

    // -- DB
    val jdbcUrl = hentMiljøValiabel("NAIS_DATABASE_PIA_SURVEY_PIA_SURVEY_DB_JDBC_URL")

    private fun hentMiljøValiabel(
        variabelNavn: String,
        defaultVerdi: String? = null,
    ) = System.getenv(variabelNavn) ?: defaultVerdi ?: throw RuntimeException("Mangler miljø variabel $variabelNavn")
}
