package no.nav.pia.survey.db

import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.serialization.json.Json
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.pia.survey.dto.SurveyDto
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
                tx.run(
                    queryOf(
                        """
                        INSERT INTO survey
                        (id, ekstern_id, opphav, type, status, opprettet, endret, gyldig_til)
                        VALUES (:id, :eksternId, :opphav, :type, :status, :opprettet, :endret, :gyldigTil)
                        """.trimIndent(),
                        mapOf(
                            "id" to UUID.randomUUID().toString(),
                            "eksternId" to survey.id,
                            "opphav" to "pia",
                            "type" to survey.type,
                            "status" to survey.status.name,
                            "opprettet" to survey.opprettet.toJavaLocalDateTime(),
                            "endret" to survey.endret?.toJavaLocalDateTime(),
                            "gyldigTil" to survey.gyldigTil.toJavaLocalDateTime(),
                        ),
                    ).asUpdate,
                )
            }
        }
    }
}
