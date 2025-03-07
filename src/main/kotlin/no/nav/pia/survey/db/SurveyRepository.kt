package no.nav.pia.survey.db

import ia.felles.integrasjoner.kafkameldinger.spørreundersøkelse.SpørreundersøkelseStatus
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.pia.survey.domene.Spørsmål
import no.nav.pia.survey.domene.Survey
import no.nav.pia.survey.domene.Svaralternativ
import no.nav.pia.survey.domene.Tema
import no.nav.pia.survey.kafka.dto.SpørreundersøkelseDto
import no.nav.pia.survey.kafka.dto.SpørsmålDto
import no.nav.pia.survey.kafka.dto.SvaralternativDto
import no.nav.pia.survey.kafka.dto.TemaDto
import java.util.UUID
import javax.sql.DataSource

class SurveyRepository(
    val dataSource: DataSource,
) {
    fun bliMed(
        surveyId: UUID,
        sesjonId: UUID,
    ) = using(sessionOf(dataSource)) { session ->
        session.run(
            queryOf(
                """
                INSERT INTO deltaker 
                VALUES (:sesjonId, :surveyId)
                """.trimIndent(),
                mapOf(
                    "sesjonId" to sesjonId,
                    "surveyId" to surveyId,
                ),
            ).asUpdate,
        )
    }

    fun hentDeltaker(
        sesjonId: UUID,
        surveyId: UUID,
    ) = using(sessionOf(dataSource)) { session ->
        session.run(
            queryOf(
                """
                SELECT sesjon_id FROM deltaker
                WHERE sesjon_id = :sesjonId AND survey = :surveyId
                """.trimIndent(),
                mapOf(
                    "sesjonId" to sesjonId.toString(),
                    "surveyId" to surveyId.toString(),
                ),
            ).map { UUID.fromString(it.string("sesjon_id")) }.asSingle,
        )
    }

    fun hentAntallDeltakere(surveyId: UUID) =
        using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    """
                    SELECT count(sesjon_id) AS antall FROM deltaker
                    WHERE survey = :surveyId
                    """.trimIndent(),
                    mapOf(
                        "surveyId" to surveyId.toString(),
                    ),
                ).map { it.int("antall") }.asSingle,
            )
        }

    fun hentSurvey(id: UUID) =
        using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    """
                    SELECT * FROM survey
                    WHERE id = :id
                    """.trimIndent(),
                    mapOf(
                        "id" to id.toString(),
                    ),
                ).map(this::mapTilSurvey).asSingle,
            )
        }

    fun hentTema(
        surveyId: UUID,
        temaId: UUID,
    ) = using(sessionOf(dataSource)) { session ->
        session.run(
            queryOf(
                """
                SELECT * FROM tema
                WHERE survey = :surveyId
                AND id = :temaId
                """.trimIndent(),
                mapOf(
                    "surveyId" to surveyId.toString(),
                    "temaId" to temaId.toString(),
                ),
            ).map(this::mapTilTema).asSingle,
        )
    }

    private fun mapTilTema(row: Row): Tema {
        val id = UUID.fromString(row.string("id"))
        return Tema(
            id = id,
            eksternId = row.string("ekstern_id"),
            navn = row.string("navn"),
            status = Tema.Companion.Status.valueOf(row.string("status")),
            spørsmål = hentSpørsmål(id),
        )
    }

    fun oppdaterTemaStatus(
        temaId: UUID,
        status: Tema.Companion.Status,
    ) = using(sessionOf(dataSource)) { session ->
        session.transaction { transaction ->
            transaction.run(
                queryOf(
                    """
                    UPDATE tema SET STATUS = :status
                    WHERE id = :temaId
                    """.trimIndent(),
                    mapOf(
                        "temaId" to temaId.toString(),
                        "status" to status.name,
                    ),
                ).asUpdate,
            )
            transaction.run(
                queryOf(
                    """
                    SELECT * FROM tema
                    WHERE id = :temaId
                    """.trimIndent(),
                    mapOf(
                        "temaId" to temaId.toString(),
                    ),
                ).map(this::mapTilTema).asSingle,
            )
        }
    }

    fun hentSurvey(
        eksternId: String,
        opphav: String,
        type: String,
    ) = using(sessionOf(dataSource)) { session ->
        session.run(
            queryOf(
                """
                SELECT * FROM survey
                WHERE ekstern_id = :eksternId
                AND opphav = :opphav
                AND type = :type 
                """.trimIndent(),
                mapOf(
                    "eksternId" to eksternId,
                    "opphav" to opphav,
                    "type" to type,
                ),
            ).map(this::mapTilSurvey).asSingle,
        )
    }

    private fun mapTilSurvey(surveyRad: Row): Survey {
        val id = UUID.fromString(surveyRad.string("id"))
        return Survey(
            id = id,
            eksternId = surveyRad.string("ekstern_id"),
            opphav = surveyRad.string("opphav"),
            type = surveyRad.string("type"),
            status = SpørreundersøkelseStatus.valueOf(surveyRad.string("status")),
            opprettet = surveyRad.localDateTime("opprettet").toKotlinLocalDateTime(),
            endret = surveyRad.localDateTimeOrNull("endret")?.toKotlinLocalDateTime(),
            gyldigTil = surveyRad.localDateTime("gyldig_til").toKotlinLocalDateTime(),
            temaer = hentTemaer(id),
        )
    }

    private fun hentTemaer(surveyId: UUID) =
        using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    """
                    SELECT * FROM tema
                    WHERE survey = :surveyId
                    """.trimIndent(),
                    mapOf(
                        "surveyId" to surveyId.toString(),
                    ),
                ).map(this::mapTilTema).asList,
            )
        }

    private fun hentSpørsmål(temaId: UUID) =
        using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    """
                    SELECT * FROM sporsmal
                    WHERE tema = :temaId
                    """.trimIndent(),
                    mapOf(
                        "temaId" to temaId.toString(),
                    ),
                ).map { spørsmålRad ->
                    val id = UUID.fromString(spørsmålRad.string("id"))
                    Spørsmål(
                        id = id,
                        eksternId = spørsmålRad.string("ekstern_id"),
                        flervalg = spørsmålRad.boolean("flervalg"),
                        tekst = spørsmålRad.string("tekst"),
                        kategori = spørsmålRad.stringOrNull("kategori"),
                        svaralternativ = hentSvaralternativer(id),
                    )
                }.asList,
            )
        }

    private fun hentSvaralternativer(spørsmålId: UUID) =
        using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    """
                    SELECT * from svaralternativ
                    WHERE sporsmal = :sporsmalId
                    """.trimIndent(),
                    mapOf(
                        "sporsmalId" to spørsmålId.toString(),
                    ),
                ).map { svaralternativRad ->
                    Svaralternativ(
                        id = UUID.fromString(svaralternativRad.string("id")),
                        eksternId = svaralternativRad.string("ekstern_id"),
                        tekst = svaralternativRad.string("tekst"),
                    )
                }.asList,
            )
        }

    fun oppdaterSurvey(survey: SpørreundersøkelseDto) {
        using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    """
                    UPDATE survey SET status = :status, endret = :endret
                    WHERE ekstern_id = :eksternId
                    AND opphav = :opphav
                    AND type = :type 
                    """.trimIndent(),
                    mapOf(
                        "status" to survey.status.toString(),
                        "endret" to survey.endret?.toJavaLocalDateTime(),
                        "eksternId" to survey.id,
                        "opphav" to survey.opphav,
                        "type" to survey.type,
                    ),
                ).asUpdate,
            )
        }
    }

    fun slettSurvey(survey: SpørreundersøkelseDto) {
        using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    """
                    DELETE FROM survey
                    WHERE ekstern_id = :eksternId
                    AND opphav = :opphav
                    AND type = :type
                    """.trimIndent(),
                    mapOf(
                        "eksternId" to survey.id,
                        "opphav" to survey.opphav,
                        "type" to survey.type,
                    ),
                ).asUpdate,
            )
        }
    }

    fun lagreSurvey(survey: SpørreundersøkelseDto) {
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
        survey: SpørreundersøkelseDto,
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
            "opphav" to survey.opphav,
            "type" to survey.type,
            "status" to survey.status.name,
            "opprettet" to survey.opprettet.toJavaLocalDateTime(),
            "endret" to survey.endret?.toJavaLocalDateTime(),
            "gyldigTil" to survey.gyldigTil.toJavaLocalDateTime(),
        ),
    ).asUpdate
}
