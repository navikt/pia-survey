package no.nav.pia.survey.helper

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.pia.survey.kafka.KafkaTopics
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.StringSerializer
import org.testcontainers.containers.Network
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.utility.DockerImageName
import java.util.TimeZone

class KafkaContainer(
    network: Network,
) {
    private val containerAlias = "kafka-container"
    private var kafkaProducer: KafkaProducer<String, String>

    val container = ConfluentKafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.6.0"),
    )
        .withNetwork(network)
        .withNetworkAliases(containerAlias)
        .withLogConsumer(
            Slf4jLogConsumer(TestContainerHelper.log).withPrefix(containerAlias).withSeparateOutputStreams(),
        ).withEnv(
            mutableMapOf(
                "KAFKA_LOG4J_LOGGERS" to "org.apache.kafka.image.loader.MetadataLoader=WARN",
                "KAFKA_AUTO_LEADER_REBALANCE_ENABLE" to "false",
                "KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS" to "1",
                "TZ" to TimeZone.getDefault().id,
            ),
        )
        .waitingFor(HostPortWaitStrategy())
        .apply {
            start()
            kafkaProducer = producer()
        }

    fun getEnv() =
        mapOf(
            "KAFKA_BROKERS" to "BROKER://$containerAlias:9093,PLAINTEXT://$containerAlias:9093",
            "KAFKA_TRUSTSTORE_PATH" to "",
            "KAFKA_KEYSTORE_PATH" to "",
            "KAFKA_CREDSTORE_PASSWORD" to "",
        )

    fun sendMeldingPåKafka(
        nøkkel: String = "nøkkel",
        melding: String = "melding",
    ) = sendOgVent(nøkkel, melding, KafkaTopics.SPØRREUNDERSØKELSE)

    private fun sendOgVent(
        nøkkel: String,
        melding: String,
        topic: KafkaTopics,
    ) {
        runBlocking {
            kafkaProducer.send(ProducerRecord(topic.navnMedNamespace, nøkkel, melding)).get()
            delay(timeMillis = 30L)
        }
    }

    private fun ConfluentKafkaContainer.producer(): KafkaProducer<String, String> =
        KafkaProducer(
            mapOf(
                CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to this.bootstrapServers,
                CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "PLAINTEXT",
                ProducerConfig.ACKS_CONFIG to "all",
                ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION to "1",
                ProducerConfig.LINGER_MS_CONFIG to "0",
                ProducerConfig.RETRIES_CONFIG to "0",
                ProducerConfig.BATCH_SIZE_CONFIG to "1",
                SaslConfigs.SASL_MECHANISM to "PLAIN",
            ),
            StringSerializer(),
            StringSerializer(),
        )
}
