package no.nav.pia.survey.kafka

import ia.felles.integrasjoner.kafkameldinger.spørreundersøkelse.SpørreundersøkelseStatus
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nav.pia.survey.dto.SpørsmålDto
import no.nav.pia.survey.dto.SurveyDto
import no.nav.pia.survey.dto.SvaralternativDto
import no.nav.pia.survey.dto.TemaDto
import no.nav.pia.survey.helper.TestContainerHelper.Companion.kafkaContainer
import no.nav.pia.survey.helper.TestContainerHelper.Companion.postgresContainer
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test

class KafkaKonsumentTest {
    @Test
    fun `skal oppdatere status og endret på mottatte meldinger`() {
        val survey = enSurvey()
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
        val survey = enSurvey()
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
            melding = Json.encodeToString(enSurvey(surveyId)),
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

    // --
    private fun enSurvey(id: String = UUID.randomUUID().toString()): SurveyDto {
        val opprettet = LocalDateTime.now()
        val gyldigTil = opprettet.plusHours(24L)
        return SurveyDto(
            id = id,
            orgnummer = "123456789",
            samarbeidsNavn = "Samarbeid 1",
            virksomhetsNavn = "Virksomhet",
            status = SpørreundersøkelseStatus.OPPRETTET,
            temaer = listOf(
                TemaDto(
                    id = 1,
                    navn = "Tema 1",
                    spørsmål = listOf(
                        SpørsmålDto(
                            id = "spm_id_1",
                            tekst = "Hva?",
                            flervalg = false,
                            kategori = null,
                            svaralternativer = listOf(
                                SvaralternativDto(
                                    id = "svaralt_id_1",
                                    tekst = "Dette er det riktige svaret",
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            type = "Behovsvurdering",
            opprettet = opprettet.toKotlinLocalDateTime(),
            endret = null,
            gyldigTil = gyldigTil.toKotlinLocalDateTime(),
            plan = null,
        )
    }
}
