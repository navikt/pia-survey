package no.nav.pia.survey

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import java.net.URI
import java.util.concurrent.TimeUnit

fun Application.configureSecurity() {
    val azureJwkProvider = JwkProviderBuilder(URI(MiljøVariabler.azureJwksUri).toURL())
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    authentication {
        jwt {
            val tokenFortsattGyldigFørUtløpISekunder = 3L
            verifier(azureJwkProvider, issuer = MiljøVariabler.azureIssuer) {
                acceptLeeway(tokenFortsattGyldigFørUtløpISekunder)
                withAudience(MiljøVariabler.azureClientId)
                withClaimPresence("NAVident")
                // -- TODO: sjekk gruppetilgang
            }
            validate { token ->
                JWTPrincipal(token.payload)
            }
        }
    }
}
