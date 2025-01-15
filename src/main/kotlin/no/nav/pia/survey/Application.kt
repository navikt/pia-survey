package no.nav.pia.survey

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.pia.survey.helse.ApplikasjonsHelse
import no.nav.pia.survey.kafka.KafkaConfig
import no.nav.pia.survey.kafka.KafkaKonsument
import no.nav.pia.survey.kafka.KafkaTopics
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("no.nav.pia.survey")

fun main() {
    val applikasjonsHelse = ApplikasjonsHelse()

    val dataSource = createDataSource()
    runMigration(dataSource = dataSource)

    settOppKonsumenter(
        applikasjonsHelse = applikasjonsHelse,
    )

    val applikasjonsServer = embeddedServer(
        factory = Netty,
        port = 8080,
        host = "0.0.0.0",
        module = { piaSurveyApi(applikasjonsHelse = applikasjonsHelse) },
    )

    Runtime.getRuntime().addShutdownHook(
        Thread {
            log.info("Stopper applikajsonen fra shutdown hook")
            applikasjonsServer.stop(1000, 5000)
        },
    )

    applikasjonsServer.start(wait = true)
}

fun settOppKonsumenter(applikasjonsHelse: ApplikasjonsHelse) {
    log.info("Setter opp kafkakonsumenter")
    val spørreundersøkelseKonsument = KafkaKonsument(
        kafkaConfig = KafkaConfig(),
        kafkaTopic = KafkaTopics.SPØRREUNDERSØKELSE,
        applikasjonsHelse = applikasjonsHelse,
    )
    spørreundersøkelseKonsument.startKonsument()
}

fun Application.piaSurveyApi(applikasjonsHelse: ApplikasjonsHelse) {
    monitor.subscribe(ApplicationStopped) {
        applikasjonsHelse.ready = false
        applikasjonsHelse.alive = false
    }
    configureRouting(applikasjonsHelse = applikasjonsHelse)
}
