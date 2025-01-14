package no.nav.pia.survey.kafka

import no.nav.pia.survey.helper.TestContainerHelper
import no.nav.pia.survey.helper.TestContainerHelper.Companion.shouldContainLog
import kotlin.test.Test

class KafkaKonsumentTest {
    @Test
    fun `skal kunne konsumere meldinger fra kafka`() {
        TestContainerHelper.kafkaContainer.sendMeldingPåKafka()
        TestContainerHelper.piaSurveyContainer shouldContainLog "Mottok kafkamelding med nøkkel: nøkkel".toRegex()
    }
}
