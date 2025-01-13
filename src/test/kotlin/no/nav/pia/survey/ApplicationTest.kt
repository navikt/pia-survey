package no.nav.pia.survey

import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import no.nav.pia.survey.TestContainerHelper.Companion.piaSurveyContainer
import kotlin.test.Test

class ApplicationTest {
    val client = HttpClient()

    @Test
    fun `appen svarer på readyness`() {
        runBlocking {
            val response = client.get("http://localhost:${piaSurveyContainer.getMappedPort(8080)}/internal/isready")
            response.status shouldBe HttpStatusCode.OK
        }
    }

    @Test
    fun `appen svarer på aliveness`() {
        runBlocking {
            val response = client.get("http://localhost:${piaSurveyContainer.getMappedPort(8080)}/internal/isalive")
            response.status shouldBe HttpStatusCode.OK
        }
    }
}
