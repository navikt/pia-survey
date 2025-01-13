package no.nav.pia.survey

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.pia.survey.helse.ApplikasjonsHelse

fun main() {
    val applikasjonsHelse = ApplikasjonsHelse()
    val applikasjonsServer = embeddedServer(
        factory = Netty,
        port = 8080,
        host = "0.0.0.0",
        module = { piaSurvey(applikasjonsHelse = applikasjonsHelse) },
    )
    applikasjonsHelse.ready = true

    Runtime.getRuntime().addShutdownHook(
        Thread {
            applikasjonsHelse.ready = false
            applikasjonsHelse.alive = false
            applikasjonsServer.stop(1000, 5000)
        },
    )

    applikasjonsServer.start(wait = true)
}

fun Application.piaSurvey(applikasjonsHelse: ApplikasjonsHelse) {
    configureRouting(applikasjonsHelse = applikasjonsHelse)
}
