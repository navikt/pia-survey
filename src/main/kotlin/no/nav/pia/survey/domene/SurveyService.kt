package no.nav.pia.survey.domene

import ia.felles.integrasjoner.kafkameldinger.spørreundersøkelse.SpørreundersøkelseStatus
import kotlinx.serialization.json.Json
import no.nav.pia.survey.db.SurveyRepository
import no.nav.pia.survey.kafka.dto.SurveyDto
import java.util.UUID

class SurveyService(
    val surveyRepository: SurveyRepository,
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun hentSurvey(id: UUID) = surveyRepository.hentSurvey(id)

    fun hentSurvey(
        opphav: String,
        type: String,
        eksternId: String,
    ) = surveyRepository.hentSurvey(eksternId = eksternId, opphav = opphav, type = type)

    fun håndterKafkaMelding(melding: String) {
        val surveyDto = json.decodeFromString<SurveyDto>(melding)
        when (surveyDto.status) {
            SpørreundersøkelseStatus.SLETTET -> {
                surveyRepository.slettSurvey(surveyDto)
            }
            else -> {
                surveyRepository.hentSurvey(surveyDto.id, surveyDto.opphav, surveyDto.type)?.let {
                    surveyRepository.oppdaterSurvey(surveyDto)
                } ?: surveyRepository.lagreSurvey(surveyDto)
            }
        }
    }
}
