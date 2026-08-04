package no.nav.pia.survey.kafka

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import no.nav.pia.survey.domene.Survey
import no.nav.pia.survey.helper.TestContainerHelper.Companion.kafkaContainer
import no.nav.pia.survey.helper.TestContainerHelper.Companion.postgresContainer
import java.util.UUID
import kotlin.test.Test

class KafkaKonsumentTest {
    @Test
    fun `skal oppdatere status og endret på mottatte meldinger`() {
        val survey = kafkaContainer.enSurvey()
        kafkaContainer.sendMeldingPåKafka(
            melding = Json.encodeToString(survey),
        )
        postgresContainer.hentEnkelKolonne<String>(
            sql = "select status from survey where ekstern_id = ?",
            params = listOf(survey.id),
        ) shouldBe "OPPRETTET"

        kafkaContainer.sendMeldingPåKafka(
            melding = Json.encodeToString(survey.copy(status = Survey.Status.PÅBEGYNT)),
        )
        postgresContainer.hentEnkelKolonne<String>(
            sql = "select status from survey where ekstern_id = ?",
            params = listOf(survey.id),
        ) shouldBe "PÅBEGYNT"
    }

    @Test
    fun `skal slette surveys basert på status i kafka`() {
        val survey = kafkaContainer.enSurvey()
        kafkaContainer.sendMeldingPåKafka(
            melding = Json.encodeToString(survey),
        )
        postgresContainer.hentEnkelKolonne<String>(
            sql = "select type from survey where ekstern_id = ?",
            params = listOf(survey.id),
        ) shouldBe "Behovsvurdering"

        kafkaContainer.sendMeldingPåKafka(
            melding = Json.encodeToString(survey.copy(status = Survey.Status.SLETTET)),
        )

        postgresContainer.hentAlleRaderTilEnkelKolonne<String>(
            sql = "select id from survey where ekstern_id = ?",
            params = listOf(survey.id),
        ) shouldHaveSize 0
    }

    @Test
    fun `skal kunne lagre surveys fra kafka`() {
        val surveyId = UUID.randomUUID().toString()
        kafkaContainer.sendMeldingPåKafka(
            melding = Json.encodeToString(kafkaContainer.enSurvey(surveyId)),
        )
        postgresContainer.hentEnkelKolonne<String>(
            sql = "select type from survey where ekstern_id = ?",
            params = listOf(surveyId),
        ) shouldBe "Behovsvurdering"

        postgresContainer.hentEnkelKolonne<String>(
            sql = """
            select tema.navn from tema join survey on (tema.survey = survey.id)
             where survey.ekstern_id = ?
             and tema.ekstern_id = '1'
            """.trimIndent(),
            params = listOf(surveyId),
        ) shouldBe "Tema 1"

        postgresContainer.hentEnkelKolonne<String>(
            sql = """
            select sporsmal.tekst from sporsmal 
             join tema on (sporsmal.tema = tema.id) 
             join survey on (tema.survey = survey.id)
             where survey.ekstern_id = ?
             and tema.ekstern_id = '1'
             and sporsmal.ekstern_id = 'spm_id_1'
            """.trimIndent(),
            params = listOf(surveyId),
        ) shouldBe "Hva?"
    }
}
