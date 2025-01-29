package no.nav.pia.survey.domene

import ia.felles.integrasjoner.kafkameldinger.spørreundersøkelse.SpørreundersøkelseStatus
import kotlinx.serialization.json.Json
import no.nav.pia.survey.db.SurveyRepository
import no.nav.pia.survey.kafka.dto.SpørreundersøkelseDto
import java.util.UUID

class SurveyService(
    private val surveyRepository: SurveyRepository,
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun hentAntallDeltakere(id: UUID) = surveyRepository.hentAntallDeltakere(id) ?: 0

    fun hentSurvey(id: UUID) = surveyRepository.hentSurvey(id)

    fun hentSurvey(
        opphav: String,
        type: String,
        eksternId: String,
    ) = surveyRepository.hentSurvey(eksternId = eksternId, opphav = opphav, type = type)

    fun håndterKafkaMelding(melding: String) {
        val spørreundersøkelseDto = json.decodeFromString<SpørreundersøkelseDto>(melding)
        when (spørreundersøkelseDto.status) {
            SpørreundersøkelseStatus.SLETTET -> {
                surveyRepository.slettSurvey(spørreundersøkelseDto)
            }
            else -> {
                surveyRepository.hentSurvey(spørreundersøkelseDto.id, spørreundersøkelseDto.opphav, spørreundersøkelseDto.type)?.let {
                    surveyRepository.oppdaterSurvey(spørreundersøkelseDto)
                } ?: surveyRepository.lagreSurvey(spørreundersøkelseDto)
            }
        }
    }

    fun hentDeltaker(
        sesjonId: UUID,
        surveyId: UUID,
    ) = surveyRepository.hentDeltaker(sesjonId, surveyId)

    fun bliMed(surveyId: UUID) =
        hentSurvey(surveyId)?.let {
            val sesjonId = UUID.randomUUID()
            surveyRepository.bliMed(surveyId = surveyId, sesjonId = sesjonId)
            sesjonId
        }
}
