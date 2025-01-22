package no.nav.pia.survey.db

import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.serialization.json.Json
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.pia.survey.dto.SpørsmålDto
import no.nav.pia.survey.dto.SurveyDto
import no.nav.pia.survey.dto.SvaralternativDto
import no.nav.pia.survey.dto.TemaDto
import java.util.UUID
import javax.sql.DataSource

class SurveyRepository(
    val dataSource: DataSource,
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun håndterKafkaMelding(melding: String) {
        val survey = json.decodeFromString<SurveyDto>(melding)
        using(sessionOf(dataSource)) { session ->
            session.transaction { tx ->
                val surveyId = UUID.randomUUID().toString()
                tx.run(
                    insertSurvey(survey, surveyId),
                )

                survey.temaer.forEach { tema ->
                    val temaId = UUID.randomUUID().toString()
                    tx.run(
                        insertTema(temaId, tema, surveyId),
                    )

                    tema.spørsmål.forEach { spørsmål ->
                        val spørsmålId = UUID.randomUUID().toString()
                        tx.run(
                            insertSpørsmål(spørsmålId, spørsmål, temaId),
                        )

                        spørsmål.svaralternativer.forEach { svaralternativ ->
                            tx.run(
                                insertSvaralternativ(svaralternativ, spørsmålId),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun insertSvaralternativ(
        svaralternativ: SvaralternativDto,
        spørsmålId: String,
    ) = queryOf(
        """
        INSERT INTO svaralternativ
        (id, ekstern_id, sporsmal, tekst) VALUES
        (:id, :eksternId, :sporsmalId, :tekst)
        """.trimIndent(),
        mapOf(
            "id" to UUID.randomUUID().toString(),
            "eksternId" to svaralternativ.id,
            "sporsmalId" to spørsmålId,
            "tekst" to svaralternativ.tekst,
        ),
    ).asUpdate

    private fun insertSpørsmål(
        spørsmålId: String,
        spørsmål: SpørsmålDto,
        temaId: String,
    ) = queryOf(
        """
        INSERT INTO sporsmal
        (id, ekstern_id, tema, flervalg, tekst, kategori) VALUES
        (:id, :eksternId, :temaId, :flervalg, :tekst, :kategori)
        """.trimIndent(),
        mapOf(
            "id" to spørsmålId,
            "eksternId" to spørsmål.id,
            "temaId" to temaId,
            "flervalg" to spørsmål.flervalg,
            "tekst" to spørsmål.tekst,
            "kategori" to spørsmål.kategori,
        ),
    ).asUpdate

    private fun insertTema(
        temaId: String,
        tema: TemaDto,
        surveyId: String,
    ) = queryOf(
        """
        INSERT INTO tema
        (id, ekstern_id, survey, navn) VALUES
        (:id, :eksternId, :surveyId, :navn)
        """.trimIndent(),
        mapOf(
            "id" to temaId,
            "eksternId" to tema.id,
            "surveyId" to surveyId,
            "navn" to tema.navn,
        ),
    ).asUpdate

    private fun insertSurvey(
        survey: SurveyDto,
        surveyId: String,
    ) = queryOf(
        """
        INSERT INTO survey
        (id, ekstern_id, opphav, type, status, opprettet, endret, gyldig_til)
        VALUES (:id, :eksternId, :opphav, :type, :status, :opprettet, :endret, :gyldigTil)
        """.trimIndent(),
        mapOf(
            "id" to surveyId,
            "eksternId" to survey.id,
            "opphav" to "fia",
            "type" to survey.type,
            "status" to survey.status.name,
            "opprettet" to survey.opprettet.toJavaLocalDateTime(),
            "endret" to survey.endret?.toJavaLocalDateTime(),
            "gyldigTil" to survey.gyldigTil.toJavaLocalDateTime(),
        ),
    ).asUpdate
}
