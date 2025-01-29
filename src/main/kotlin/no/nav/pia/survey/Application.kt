package no.nav.pia.survey

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.pia.survey.db.SurveyRepository
import no.nav.pia.survey.domene.SurveyService
import no.nav.pia.survey.kafka.KafkaConfig
import no.nav.pia.survey.kafka.KafkaKonsument
import no.nav.pia.survey.kafka.KafkaTopics
import org.slf4j.LoggerFactory
import javax.sql.DataSource

private val log = LoggerFactory.getLogger("no.nav.pia.survey")

fun main() {
    val applikasjonsHelse = ApplikasjonsHelse()

    val dataSource = createDataSource()
    runMigration(dataSource = dataSource)

    settOppKonsumenter(
        applikasjonsHelse = applikasjonsHelse,
        dataSource = dataSource,
    )

    val applikasjonsServer = embeddedServer(
        factory = Netty,
        port = 8080,
        host = "0.0.0.0",
        module = {
            piaSurveyApi(
                applikasjonsHelse = applikasjonsHelse,
                surveyService = SurveyService(SurveyRepository(dataSource)),
            )
        },
    )

    Runtime.getRuntime().addShutdownHook(
        Thread {
            log.info("Stopper applikajsonen fra shutdown hook")
            applikasjonsServer.stop(1000, 5000)
        },
    )

    applikasjonsServer.start(wait = true)
}

private fun settOppKonsumenter(
    applikasjonsHelse: ApplikasjonsHelse,
    dataSource: DataSource,
) {
    log.info("Setter opp kafkakonsumenter")
    val surveyRepository = SurveyRepository(dataSource = dataSource)
    val surveyService = SurveyService(surveyRepository = surveyRepository)
    val spørreundersøkelseKonsument = KafkaKonsument(
        kafkaConfig = KafkaConfig(),
        kafkaTopic = KafkaTopics.SPØRREUNDERSØKELSE,
        applikasjonsHelse = applikasjonsHelse,
    ) {
        surveyService.håndterKafkaMelding(it)
    }
    spørreundersøkelseKonsument.startKonsument()
}

internal fun Application.piaSurveyApi(
    applikasjonsHelse: ApplikasjonsHelse,
    surveyService: SurveyService,
) {
    monitor.subscribe(ApplicationStopped) {
        applikasjonsHelse.ready = false
        applikasjonsHelse.alive = false
    }
    configureSerialization()
    configureSecurity()
    configureStatusPages()
    configureRouting(
        applikasjonsHelse = applikasjonsHelse,
        surveyService = surveyService,
    )
}
