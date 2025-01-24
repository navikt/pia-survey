package no.nav.pia.survey.domene

import ia.felles.integrasjoner.kafkameldinger.spørreundersøkelse.SpørreundersøkelseStatus
import kotlinx.serialization.json.Json
import no.nav.pia.survey.db.SurveyRepository
import no.nav.pia.survey.dto.SurveyDto

class SurveyService(
    val surveyRepository: SurveyRepository,
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

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
