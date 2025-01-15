package no.nav.pia.survey.helper

import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy

class PostgresContainer(
    network: Network,
) {
    private val containerAlias = "postgresql-container"
    private val dbNavn = "pia-survey-db"

    val container = PostgreSQLContainer("postgres:17")
        .withNetwork(network)
        .withNetworkAliases(containerAlias)
        .withLogConsumer(
            Slf4jLogConsumer(TestContainerHelper.log).withPrefix(containerAlias).withSeparateOutputStreams(),
        )
        .withDatabaseName(dbNavn)
        .waitingFor(HostPortWaitStrategy())
        .apply {
            start()
        }

    fun envVars() =
        "NAIS_DATABASE_PIA_SURVEY_PIA_SURVEY_DB_JDBC_URL" to
            "jdbc:postgresql://$containerAlias:5432/$dbNavn?password=${container.password}&user=${container.username}"
}
