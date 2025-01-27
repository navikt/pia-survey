package no.nav.pia.survey.kafka

import ia.felles.integrasjoner.kafkameldinger.spørreundersøkelse.SpørreundersøkelseStatus
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
            """
            select status from survey where ekstern_id = '${survey.id}'
            """.trimIndent(),
        ) shouldBe "OPPRETTET"

        kafkaContainer.sendMeldingPåKafka(
            melding = Json.encodeToString(survey.copy(status = SpørreundersøkelseStatus.PÅBEGYNT)),
        )
        postgresContainer.hentEnkelKolonne<String>(
            """
            select status from survey where ekstern_id = '${survey.id}'
            """.trimIndent(),
        ) shouldBe "PÅBEGYNT"
    }

    @Test
    fun `skal slette surveys basert på status i kafka`() {
        val survey = kafkaContainer.enSurvey()
        kafkaContainer.sendMeldingPåKafka(
            melding = Json.encodeToString(survey),
        )
        postgresContainer.hentEnkelKolonne<String>(
            """
            select type from survey where ekstern_id = '${survey.id}'
            """.trimIndent(),
        ) shouldBe "Behovsvurdering"

        kafkaContainer.sendMeldingPåKafka(
            melding = Json.encodeToString(survey.copy(status = SpørreundersøkelseStatus.SLETTET)),
        )

        postgresContainer.hentAlleRaderTilEnkelKolonne<String>(
            """
            select id from survey where ekstern_id = '${survey.id}'
            """.trimIndent(),
        ) shouldHaveSize 0
    }

    @Test
    fun `skal kunne lagre surveys fra kafka`() {
        val surveyId = UUID.randomUUID().toString()
        kafkaContainer.sendMeldingPåKafka(
            melding = Json.encodeToString(kafkaContainer.enSurvey(surveyId)),
        )
        postgresContainer.hentEnkelKolonne<String>(
            """
            select type from survey where ekstern_id = '$surveyId'
            """.trimIndent(),
        ) shouldBe "Behovsvurdering"

        postgresContainer.hentEnkelKolonne<String>(
            """
            select tema.navn from tema join survey on (tema.survey = survey.id)
             where survey.ekstern_id = '$surveyId'
             and tema.ekstern_id = '1'
            """.trimIndent(),
        ) shouldBe "Tema 1"

        postgresContainer.hentEnkelKolonne<String>(
            """
            select sporsmal.tekst from sporsmal 
             join tema on (sporsmal.tema = tema.id) 
             join survey on (tema.survey = survey.id)
             where survey.ekstern_id = '$surveyId'
             and tema.ekstern_id = '1'
             and sporsmal.ekstern_id = 'spm_id_1'
            """.trimIndent(),
        ) shouldBe "Hva?"
    }
}
