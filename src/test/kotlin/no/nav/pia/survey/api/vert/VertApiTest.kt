package no.nav.pia.survey.api.vert

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nav.pia.survey.helper.TestContainerHelper
import no.nav.pia.survey.helper.TestContainerHelper.Companion.kafkaContainer
import no.nav.pia.survey.helper.TestContainerHelper.Companion.postgresContainer
import kotlin.test.Test

class VertApiTest {
    @Test
    fun `kan hente ut en survey`() {
        val behovsvurdering = kafkaContainer.enSurvey()
        kafkaContainer.sendMeldingPåKafka(
            melding = Json.encodeToString(behovsvurdering),
        )

        runBlocking {
            val survey = TestContainerHelper.hentSurveySomVert(behovsvurdering.opphav, behovsvurdering.type, behovsvurdering.id)
            survey.id shouldNotBe behovsvurdering.id
            postgresContainer.hentEnkelKolonne<String>(
                sql = "select ekstern_id from survey where id = '${survey.id}'",
            ) shouldBe behovsvurdering.id
        }
    }
}
