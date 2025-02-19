package no.nav.pia.survey.api.vert

import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.client.request.bearerAuth
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import no.nav.pia.survey.domene.Tema.Companion.Status.AVSLUTTET
import no.nav.pia.survey.domene.Tema.Companion.Status.IKKE_STARTET
import no.nav.pia.survey.domene.Tema.Companion.Status.STARTET
import no.nav.pia.survey.helper.TestContainerHelper.Companion.authContainer
import no.nav.pia.survey.helper.TestContainerHelper.Companion.kafkaContainer
import no.nav.pia.survey.helper.TestContainerHelper.Companion.piaSurveyContainer
import no.nav.pia.survey.helper.TestContainerHelper.Companion.postgresContainer
import no.nav.pia.survey.helper.bliMed
import no.nav.pia.survey.helper.hentAntallDeltakere
import no.nav.pia.survey.helper.hentSurveySomVert
import no.nav.pia.survey.helper.oppdaterTemaStatus
import no.nav.pia.survey.helper.performGet
import java.util.UUID
import kotlin.test.Test

class VertApiTest {
    @Test
    fun `tema skal som default være IKKE_STARTET`() {
        val behovsvurdering = kafkaContainer.nySurvey()
        runBlocking {
            val survey = behovsvurdering.hentSurveySomVert()
            survey.temaer.forAll {
                it.status shouldBe IKKE_STARTET
            }
        }
    }

    @Test
    fun `skal kunne starte og avslutte temaer`() {
        val behovsvurdering = kafkaContainer.nySurvey()
        runBlocking {
            val survey = behovsvurdering.hentSurveySomVert()
            survey.temaer.forEach {
                survey.oppdaterTemaStatus(it, STARTET).status shouldBe STARTET
            }
            survey.temaer.forEach {
                survey.oppdaterTemaStatus(it, AVSLUTTET).status shouldBe AVSLUTTET
            }
        }
    }

    @Test
    fun `vert skal kunne hente antall deltakere som har blitt med`() {
        val behovsvurdering = kafkaContainer.nySurvey()
        runBlocking {
            val survey = behovsvurdering.hentSurveySomVert()
            survey.hentAntallDeltakere() shouldBe 0

            val antallDeltakere = 5
            for (deltaker in (1..5)) {
                survey.bliMed()
            }
            survey.hentAntallDeltakere() shouldBe antallDeltakere
        }
    }

    @Test
    fun `skal ikke kunne hente ut vertsider uten gyldig token`() {
        runBlocking {
            val response = piaSurveyContainer.performGet("$VERT_BASEPATH/survey/${UUID.randomUUID()}")
            response.status shouldBe HttpStatusCode.Unauthorized
        }
    }

    @Test
    fun `skal få feilmelding hvis man forsøker å hente en survey som ikke eksisterer`() {
        runBlocking {
            val response = piaSurveyContainer.performGet("$VERT_BASEPATH/survey/${UUID.randomUUID()}") {
                bearerAuth(authContainer.issueToken().serialize())
            }
            response.status shouldBe HttpStatusCode.NotFound
        }
    }

    @Test
    fun `vert kan hente ut en survey`() {
        val behovsvurdering = kafkaContainer.nySurvey()

        runBlocking {
            val survey =
                hentSurveySomVert(behovsvurdering.opphav, behovsvurdering.type, behovsvurdering.id)
            survey.id shouldNotBe behovsvurdering.id
            postgresContainer.hentEnkelKolonne<String>(
                sql = "select ekstern_id from survey where id = '${survey.id}'",
            ) shouldBe behovsvurdering.id
        }
    }
}
