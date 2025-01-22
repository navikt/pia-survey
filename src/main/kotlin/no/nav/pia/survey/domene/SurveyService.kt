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
        val survey = json.decodeFromString<SurveyDto>(melding)
        when (survey.status) {
            SpørreundersøkelseStatus.SLETTET -> {
                surveyRepository.slettSurvey(survey)
            }
            SpørreundersøkelseStatus.OPPRETTET -> {
                surveyRepository.lagreSurvey(survey)
            }
            else -> {
                // TODO - oppdater
            }
        }
    }
}
