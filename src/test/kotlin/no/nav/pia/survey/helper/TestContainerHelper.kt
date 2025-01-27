package no.nav.pia.survey.helper

import io.kotest.matchers.string.shouldContain
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import no.nav.pia.survey.api.dto.SurveyDto
import no.nav.pia.survey.api.vert.VERT_BASEPATH
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy
import org.testcontainers.images.builder.ImageFromDockerfile
import java.time.Duration
import java.util.UUID
import kotlin.io.path.Path

class TestContainerHelper {
    companion object {
        val log: Logger = LoggerFactory.getLogger(TestContainerHelper::class.java)
        private val httpClient = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
            followRedirects = true
        }

        private val network = Network.newNetwork()
        val kafkaContainer = KafkaContainer(network = network)
        val postgresContainer = PostgresContainer(network = network)

        val piaSurveyContainer =
            GenericContainer(
                ImageFromDockerfile().withDockerfile(Path("./Dockerfile")),
            )
                .dependsOn(
                    kafkaContainer.container,
                    postgresContainer.container,
                )
                .withNetwork(network)
                .withExposedPorts(8080)
                .withLogConsumer(Slf4jLogConsumer(log).withPrefix("pia-survey").withSeparateOutputStreams())
                .waitingFor(HttpWaitStrategy().forPath("/internal/isready").withStartupTimeout(Duration.ofSeconds(20)))
                .withEnv(
                    kafkaContainer.getEnv()
                        .plus(
                            postgresContainer.envVars(),
                        )
                        .plus(
                            mapOf(
                                "NAIS_CLUSTER_NAME" to "lokal",
                            ),
                        ),
                )
                .apply {
                    start()
                }

        infix fun GenericContainer<*>.shouldContainLog(regex: Regex) = logs shouldContain regex

        fun GenericContainer<*>.buildUrl(url: String) = "http://${this.host}:${this.getMappedPort(8080)}/$url"

        suspend fun hentSurveySomVert(
            opphav: String,
            type: String,
            eksternId: String,
        ) = httpClient.get(
            piaSurveyContainer.buildUrl("$VERT_BASEPATH/$opphav/$type/$eksternId"),
        ).body<SurveyDto>()

        suspend fun hentSurveySomVert(id: UUID) =
            httpClient.get(
                piaSurveyContainer.buildUrl("$VERT_BASEPATH/survey/$id"),
            ).body<SurveyDto>()
    }
}
