package no.nav.pia.survey.helper

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
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
import kotlin.io.path.Path
import kotlin.test.fail

class TestContainerHelper {
    companion object {
        val log: Logger = LoggerFactory.getLogger(TestContainerHelper::class.java)
        val httpClient = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
            followRedirects = true
        }

        private val network = Network.newNetwork()
        val kafkaContainer = KafkaContainer(network = network)
        val postgresContainer = PostgresContainer(network = network)
        val authContainer = AuthContainer(network = network)

        val piaSurveyContainer =
            GenericContainer(
                ImageFromDockerfile().withDockerfile(Path("./Dockerfile")),
            )
                .dependsOn(
                    kafkaContainer.container,
                    postgresContainer.container,
                    authContainer.container,
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
                            authContainer.getEnv(),
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
            token: String = authContainer.issueToken().serialize(),
        ) = httpGet<SurveyDto>(
            url = piaSurveyContainer.buildUrl("$VERT_BASEPATH/$opphav/$type/$eksternId"),
            httpConfig = {
                bearerAuth(token)
            },
        ) ?: fail("Fikk null verdi ved uthenting av survey")

        suspend inline fun <reified T> httpGet(
            url: String,
            forventetStatus: HttpStatusCode = HttpStatusCode.OK,
            httpConfig: HttpRequestBuilder.() -> Unit = {},
            block: (T) -> Unit = {},
        ): T? =
            try {
                val response = httpClient.get(url) {
                    httpConfig()
                }
                response.status shouldBe forventetStatus
                if (forventetStatus.value in (200..299)) {
                    val data = response.body<T>()
                    block(data)
                    data
                } else {
                    null
                }
            } catch (e: Throwable) {
                fail(e.message, e)
            }
    }
}
