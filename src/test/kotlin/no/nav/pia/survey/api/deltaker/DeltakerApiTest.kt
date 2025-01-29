package no.nav.pia.survey.api.deltaker

import io.kotest.matchers.shouldBe
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import no.nav.pia.survey.api.dto.BliMedRequest
import no.nav.pia.survey.helper.TestContainerHelper.Companion.kafkaContainer
import no.nav.pia.survey.helper.TestContainerHelper.Companion.piaSurveyContainer
import no.nav.pia.survey.helper.bliMed
import no.nav.pia.survey.helper.hentSurveySomVert
import no.nav.pia.survey.helper.performPost
import java.util.UUID
import kotlin.test.Test

class DeltakerApiTest {
    @Test
    fun `skal ikke kunne bli med i surveys som ikke eksisterer`() {
        runBlocking {
            val response = piaSurveyContainer.performPost(BLI_MED_PATH, BliMedRequest(UUID.randomUUID().toString()))
            response.status shouldBe HttpStatusCode.NotFound
        }
    }

    @Test
    fun `skal få feil når man blir med dersom surveyId er ugyldig`() {
        runBlocking {
            val response = piaSurveyContainer.performPost(BLI_MED_PATH, BliMedRequest("dette er ikke en uuid"))
            response.status shouldBe HttpStatusCode.BadRequest
        }
    }

    @Test
    fun `skal kunne bli med i en survey`() {
        val behovsvurdering = kafkaContainer.nySurvey()

        runBlocking {
            val survey = behovsvurdering.hentSurveySomVert()
            val bliMedDto = survey.bliMed()
            bliMedDto.surveyId shouldBe survey.id
        }
    }
}
