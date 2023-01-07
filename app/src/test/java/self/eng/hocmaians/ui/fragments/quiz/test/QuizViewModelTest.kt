package self.eng.hocmaians.ui.fragments.quiz.test

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import self.eng.hocmaians.MainCoroutineRule
import self.eng.hocmaians.data.entities.Question
import self.eng.hocmaians.getOrAwaitValueTest
import self.eng.hocmaians.repositories.FakeRepository
import self.eng.hocmaians.util.Constants.NOT_ANSWER_YET
import self.eng.hocmaians.util.Status

@ExperimentalCoroutinesApi
class QuizViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: QuizViewModel

    @Before
    fun setup() {
        viewModel = QuizViewModel(FakeRepository())
    }

    @Test
    fun `is initialize process correct, returns true`() {
        val correctlyInitializedAnswers = listOf(
            NOT_ANSWER_YET, NOT_ANSWER_YET, NOT_ANSWER_YET
        )

        val questions: List<Question> = listOf(
            Question(id = 1, question = "ab", option1 = "a", option2 = "b", option3 = "c",
                option4 = "d", answerNr = 2, explanation = null,
                isUserAdded = 0, isBookmark = 0, topicId = 2
            ),
            Question(id = 2, question = "ac", option1 = "a", option2 = "b", option3 = "c",
                option4 = "d", answerNr = 1, explanation = null,
                isUserAdded = 0, isBookmark = 0, topicId = 4
            ),
            Question(id = 3, question = "rb", option1 = "a", option2 = "b", option3 = "c",
                option4 = "d", answerNr = 4, explanation = null,
                isUserAdded = 0, isBookmark = 0, topicId = 6
            )
        )

        viewModel.initialize(questions = questions)

        val answers: List<Int> = viewModel.answers.getOrAwaitValueTest()

        assertThat(viewModel.questionList).isEqualTo(questions)
        assertThat(viewModel.doneQuantity).isEqualTo(0)
        assertThat(answers.size).isEqualTo(questions.size)
        assertThat(answers).isEqualTo(correctlyInitializedAnswers)
    }

    @Test
    fun `when user answer a newly created question, save it into answer list and move to next question, return success`() {
        // since currentQuestionPos is private in viewModel, so its default value is 0

        val questions: List<Question> = listOf(
            Question(id = 1, question = "ab", option1 = "a", option2 = "b", option3 = "c",
                option4 = "d", answerNr = 2, explanation = null,
                isUserAdded = 0, isBookmark = 0, topicId = 3
            ),
            Question(id = 2, question = "ac", option1 = "a", option2 = "b", option3 = "c",
                option4 = "d", answerNr = 1, explanation = null,
                isUserAdded = 0, isBookmark = 1, topicId = 4
            )
        )

        viewModel.initialize(questions = questions)

        val questionPos = 0
        val answerPosition = 2

        val pageNumber = viewModel.onAnswerQuestion(
            currentPosition = questionPos,
            answerPosition = answerPosition
        )

        val answers: List<Int> = viewModel.answers.getOrAwaitValueTest()

        // update answer list?
        assertThat(answers[questionPos]).isEqualTo(answerPosition)
        // update the done quantity?
        assertThat(viewModel.doneQuantity).isEqualTo(1)
        // move to next question?
        assertThat(pageNumber).isEqualTo(questionPos + 1)
    }

    @Test
    fun `when user update an answer, save it to answer list and remain the viewpager page, return success`() {
        // since currentQuestionPos is private in viewModel, so its default value is 0

        val questions: List<Question> = listOf(
            Question(id = 1, question = "ab", option1 = "a", option2 = "b", option3 = "c",
                option4 = "drt", answerNr = 2, explanation = null,
                isUserAdded = 0, isBookmark = 0, topicId = 2
            ),
            Question(id = 2, question = "ac", option1 = "a", option2 = "b", option3 = "c",
                option4 = "e", answerNr = 1, explanation = null,
                isUserAdded = 0, isBookmark = 0, topicId = 4
            )
        )

        viewModel.initialize(questions = questions)

        val questionPos = 0
        val answerPosition = 2
        val updatedAnswer = 4

        viewModel.onAnswerQuestion(currentPosition = questionPos, answerPosition = answerPosition)
        val pageNumber = viewModel.onAnswerQuestion(
            currentPosition = questionPos,
            answerPosition = updatedAnswer
        )

        val answers: List<Int> = viewModel.answers.getOrAwaitValueTest()

        // update answer yet?
        assertThat(answers[questionPos]).isEqualTo(updatedAnswer)
        // same page?
        assertThat(pageNumber).isEqualTo(questionPos)
    }

    @Test
    fun `when user submit, returns success`() {
        val topicId = 9

        val questions: List<Question> = listOf(
            Question(id = 9, question = "ab", option1 = "a", option2 = "b", option3 = "c",
                option4 = "d", answerNr = 2, explanation = null,
                isUserAdded = 0, isBookmark = 0, topicId = topicId
            ),
            Question(id = 10, question = "ac", option1 = "a", option2 = "b", option3 = "c",
                option4 = "d", answerNr = 1, explanation = null,
                isUserAdded = 0, isBookmark = 0, topicId = topicId
            )
        )

        viewModel.initialize(questions = questions)
        viewModel.onAnswerQuestion(currentPosition = 0, answerPosition = 2)
        viewModel.onAnswerQuestion(currentPosition = 1, answerPosition = 1)

        viewModel.onSubmitTest(topicId = topicId)

        val value = viewModel.save.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }
}