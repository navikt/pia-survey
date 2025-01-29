package no.nav.pia.survey.api.vert

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nav.pia.survey.api.dto.SurveyDto
import no.nav.pia.survey.helper.TestContainerHelper.Companion.buildUrl
import no.nav.pia.survey.helper.TestContainerHelper.Companion.hentSurveySomVert
import no.nav.pia.survey.helper.TestContainerHelper.Companion.httpGet
import no.nav.pia.survey.helper.TestContainerHelper.Companion.kafkaContainer
import no.nav.pia.survey.helper.TestContainerHelper.Companion.piaSurveyContainer
import no.nav.pia.survey.helper.TestContainerHelper.Companion.postgresContainer
import java.util.UUID
import kotlin.test.Test

class VertApiTest {
    @Test
    fun `skal ikke kunne hente ut vertsider uten gyldig token`() {
        runBlocking {
            httpGet<SurveyDto>(
                url = piaSurveyContainer.buildUrl("$VERT_BASEPATH/survey/${UUID.randomUUID()}"),
                forventetStatus = HttpStatusCode.Unauthorized,
            )
        }
    }

    @Test
    fun `kan hente ut en survey`() {
        val behovsvurdering = kafkaContainer.enSurvey()
        kafkaContainer.sendMeldingPåKafka(
            melding = Json.encodeToString(behovsvurdering),
        )

        runBlocking {
            val survey = hentSurveySomVert(behovsvurdering.opphav, behovsvurdering.type, behovsvurdering.id)
            survey.id shouldNotBe behovsvurdering.id
            postgresContainer.hentEnkelKolonne<String>(
                sql = "select ekstern_id from survey where id = '${survey.id}'",
            ) shouldBe behovsvurdering.id
        }
    }
}
