package no.nav.pia.survey.helper

import io.kotest.matchers.string.shouldContain
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy
import org.testcontainers.images.builder.ImageFromDockerfile
import java.time.Duration
import kotlin.io.path.Path

class TestContainerHelper {
    companion object {
        val log: Logger = LoggerFactory.getLogger(TestContainerHelper::class.java)
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
    }
}
