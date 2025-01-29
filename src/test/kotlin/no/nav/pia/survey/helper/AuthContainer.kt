package no.nav.pia.survey.helper

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.AuthorizationCode
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant
import com.nimbusds.oauth2.sdk.Scope
import com.nimbusds.oauth2.sdk.TokenRequest
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic
import com.nimbusds.oauth2.sdk.auth.Secret
import com.nimbusds.oauth2.sdk.id.ClientID
import no.nav.security.mock.oauth2.OAuth2Config
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.net.URI

class AuthContainer(
    network: Network,
) {
    companion object {
        const val VERT_NAV_IDENT = "Z12345"
    }

    private val port = "6969"
    private val networkalias = "authserver"
    private val baseEndpointUrl = "http://$networkalias:$port"
    private val oAuth2Config = OAuth2Config()

    val container = GenericContainer(DockerImageName.parse("ghcr.io/navikt/mock-oauth2-server:2.1.10"))
        .withNetwork(network)
        .withNetworkAliases(networkalias)
        .withLogConsumer(Slf4jLogConsumer(TestContainerHelper.log).withPrefix("authContainer").withSeparateOutputStreams())
        .withExposedPorts(6969)
        .withEnv(
            mapOf(
                "SERVER_PORT" to port,
                "TZ" to "Europe/Oslo",
            ),
        )
        .waitingFor(Wait.forHttp("/default/.well-known/openid-configuration").forStatusCode(200))
        .apply { start() }

    internal fun issueToken(
        subject: String = "123",
        audience: String = "azure:pia-survey",
        claims: Map<String, Any> = mapOf(
            "NAVident" to VERT_NAV_IDENT,
        ),
        expiry: Long = 3600,
    ): SignedJWT {
        val issuerId = "azure"
        val issuerUrl = "$baseEndpointUrl/$issuerId"
        val tokenCallback = DefaultOAuth2TokenCallback(
            issuerId,
            subject,
            JOSEObjectType.JWT.type,
            listOf(audience),
            claims,
            expiry,
        )

        val tokenRequest = TokenRequest(
            URI.create(baseEndpointUrl),
            ClientSecretBasic(ClientID(issuerId), Secret("secret")),
            AuthorizationCodeGrant(AuthorizationCode("123"), URI.create("http://localhost")),
            Scope(audience),
        )
        return oAuth2Config.tokenProvider.accessToken(tokenRequest, issuerUrl.toHttpUrl(), tokenCallback, null)
    }

    fun getEnv() =
        mapOf(
            "AZURE_APP_CLIENT_ID" to "azure:pia-survey",
            "AZURE_OPENID_CONFIG_ISSUER" to "http://$networkalias:$port/azure",
            "AZURE_OPENID_CONFIG_JWKS_URI" to "http://$networkalias:$port/azure/jwks",
        )
}
