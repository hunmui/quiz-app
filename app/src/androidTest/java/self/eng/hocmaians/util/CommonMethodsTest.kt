package self.eng.hocmaians.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import self.eng.hocmaians.util.Constants.NOT_ANSWER_YET

class CommonMethodsTest {

    @Test
    fun dateTimeWithNonNegativeMillisecond_returnsNormalDate() {
        val dateTimeString: String = CommonMethods.millisToDateTime(
            milliseconds = 1654645645232L
        )
        assertThat(dateTimeString).isEqualTo("07/06/2022 11:47 PM")
    }

    @Test
    fun dateTimeWithNegativeMillisecond_returns1970() {
        val dateTimeString: String = CommonMethods.millisToDateTime(
            milliseconds = -1654645645232L
        )
        assertThat(dateTimeString).isEqualTo("01/01/1970 12:00 AM")
    }

    @Test
    fun totalCorrectMoreThanTotalQuestions_returnsNegative1() {
        val scoreInString = CommonMethods.userScoreInString(
            totalCorrect = 19,
            totalQuestions = 18
        )
        assertThat(scoreInString).isEqualTo("-1.0")
    }

    @Test
    fun totalQuestionsIsNotPositive_returnsNegative1() {
        val scoreInString = CommonMethods.userScoreInString(
            totalCorrect = 19,
            totalQuestions = -5
        )
        assertThat(scoreInString).isEqualTo("-1.0")
    }

    @Test
    fun totalCorrectIsNegative_returnsNegative1() {
        val scoreInString = CommonMethods.userScoreInString(
            totalCorrect = -2,
            totalQuestions = 8
        )
        assertThat(scoreInString).isEqualTo("-1.0")
    }

    @Test
    fun totalCorrectIsZero_returnsZero() {
        val scoreInString = CommonMethods.userScoreInString(
            totalCorrect = 0,
            totalQuestions = 21
        )
        assertThat(scoreInString).isEqualTo("0.0")
    }

    @Test
    fun totalCorrectSameAsTotalQuestions_returnsTen() {
        val scoreInString = CommonMethods.userScoreInString(
            totalCorrect = 34,
            totalQuestions = 34
        )
        assertThat(scoreInString).isEqualTo("10.0")
    }

    @Test
    fun totalCorrectLessThanTotalQuestions_returnsCorrectScore() {
        val scoreInString = CommonMethods.userScoreInString(
            totalCorrect = 8,
            totalQuestions = 17
        )
        assertThat(scoreInString).isEqualTo("4.7")
    }

    @Test
    fun validPosition_returnsCorrespondingAnswer() {
        val answer = CommonMethods.convertIndexToText(4)
        assertThat(answer).isEqualTo("D")
    }

    @Test
    fun invalidPosition_returnsInvalidAnswer() {
        val answer = CommonMethods.convertIndexToText(8)
        assertThat(answer).isEqualTo("Invalid answer")
    }

    @Test
    fun notAnsweredPosition_returnsNotAnswer() {
        val answer = CommonMethods.convertIndexToText(NOT_ANSWER_YET)
        assertThat(answer).isEqualTo("Not answered yet")
    }
}