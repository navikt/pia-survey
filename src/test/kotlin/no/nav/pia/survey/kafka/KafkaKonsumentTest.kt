package no.nav.pia.survey.kafka

import ia.felles.integrasjoner.kafkameldinger.spørreundersøkelse.SpørreundersøkelseStatus
import io.kotest.matchers.shouldBe
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nav.pia.survey.dto.SpørsmålDto
import no.nav.pia.survey.dto.SurveyDto
import no.nav.pia.survey.dto.SvaralternativDto
import no.nav.pia.survey.dto.TemaDto
import no.nav.pia.survey.helper.TestContainerHelper
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test

class KafkaKonsumentTest {
    @Test
    fun `skal kunne konsumere meldinger fra kafka`() {
        val id = UUID.randomUUID().toString()
        TestContainerHelper.kafkaContainer.sendMeldingPåKafka(
            melding = Json.encodeToString(enSurvey(id)),
        )
        TestContainerHelper.postgresContainer.hentEnkelKolonne<String>(
            """
            select type from survey where ekstern_id = '$id'
            """.trimIndent(),
        ) shouldBe "Behovsvurdering"
    }

    // --
    private fun enSurvey(id: String): SurveyDto {
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
                            id = UUID.randomUUID().toString(),
                            tekst = "Hva?",
                            flervalg = false,
                            kategori = null,
                            svaralternativer = listOf(
                                SvaralternativDto(
                                    id = UUID.randomUUID().toString(),
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
