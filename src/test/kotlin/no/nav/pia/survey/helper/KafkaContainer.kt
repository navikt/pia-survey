package no.nav.pia.survey.helper

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.withTimeoutOrNull
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.json.Json
import no.nav.pia.survey.domene.Survey
import no.nav.pia.survey.kafka.KafkaTopics
import no.nav.pia.survey.kafka.dto.SpørreundersøkelseDto
import no.nav.pia.survey.kafka.dto.SpørsmålDto
import no.nav.pia.survey.kafka.dto.SvaralternativDto
import no.nav.pia.survey.kafka.dto.TemaDto
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.StringSerializer
import org.testcontainers.containers.Network
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.time.LocalDateTime
import java.util.TimeZone
import java.util.UUID

class KafkaContainer(
    network: Network,
) {
    private val containerAlias = "kafka-container"
    private var kafkaProducer: KafkaProducer<String, String>
    private var adminClient: AdminClient

    val container = ConfluentKafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.8.2"),
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
            adminClient = AdminClient.create(mapOf(BOOTSTRAP_SERVERS_CONFIG to this.bootstrapServers))
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
            val sendtMelding = kafkaProducer.send(ProducerRecord(topic.navnMedNamespace, nøkkel, melding)).get()
            ventTilKonsumert(
                konsumentGruppeId = topic.konsumentGruppe,
                recordMetadata = sendtMelding,
            )
        }
    }

    private suspend fun ventTilKonsumert(
        konsumentGruppeId: String,
        recordMetadata: RecordMetadata,
    ) = withTimeoutOrNull(Duration.ofSeconds(5)) {
        do {
            delay(timeMillis = 1L)
        } while (consumerSinOffset(
                consumerGroup = konsumentGruppeId,
                topic = recordMetadata.topic(),
            ) <= recordMetadata.offset()
        )
    }

    private fun consumerSinOffset(
        consumerGroup: String,
        topic: String,
    ): Long {
        val offsetMetadata = adminClient.listConsumerGroupOffsets(consumerGroup)
            .partitionsToOffsetAndMetadata().get()
        return offsetMetadata[offsetMetadata.keys.firstOrNull { it.topic().contains(topic) }]?.offset() ?: -1
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

    fun nySurvey(
        id: String = UUID.randomUUID().toString(),
        type: String = "Behovsvurdering",
    ): SpørreundersøkelseDto {
        val surveyDto = enSurvey(id = id, type = type)
        sendMeldingPåKafka(
            melding = Json.encodeToString(surveyDto),
        )
        return surveyDto
    }

    // --
    fun enSurvey(
        id: String = UUID.randomUUID().toString(),
        type: String = "Behovsvurdering",
    ): SpørreundersøkelseDto {
        val opprettet = LocalDateTime.now()
        val gyldigTil = opprettet.plusHours(24L)
        return SpørreundersøkelseDto(
            id = id,
            orgnummer = "123456789",
            samarbeidsNavn = "Samarbeid 1",
            virksomhetsNavn = "Virksomhet",
            status = Survey.Status.OPPRETTET,
            temaer = listOf(
                TemaDto(
                    id = 1,
                    navn = "Tema 1",
                    spørsmål = listOf(
                        SpørsmålDto(
                            id = "spm_id_1",
                            tekst = "Hva?",
                            flervalg = false,
                            kategori = null,
                            svaralternativer = listOf(
                                SvaralternativDto(
                                    id = "svaralt_id_1",
                                    tekst = "Dette er det riktige svaret",
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            type = type,
            opprettet = opprettet.toKotlinLocalDateTime(),
            endret = null,
            gyldigTil = gyldigTil.toKotlinLocalDateTime(),
            plan = null,
        )
    }
}
