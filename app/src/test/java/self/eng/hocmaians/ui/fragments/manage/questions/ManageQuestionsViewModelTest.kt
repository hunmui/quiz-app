package self.eng.hocmaians.ui.fragments.manage.questions

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
import self.eng.hocmaians.util.Status

@ExperimentalCoroutinesApi
class ManageQuestionsViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: ManageQuestionsViewModel

    @Before
    fun setup() {
        viewModel = ManageQuestionsViewModel(FakeRepository())
    }

    @Test
    fun `insert question with blank question, returns error`() {
        viewModel.insertQuestion(
            quiz = "    ",
            option1 = "osm",
            option2 = "o",
            option3 = "a",
            option4 = "v",
            correctAnswer = "A",
            explanation = null,
            topicId = 1
        )

        val value = viewModel.insertQuestionStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert question with blank first option, returns error`() {
        viewModel.insertQuestion(
            quiz = "some",
            option1 = " ",
            option2 = "o",
            option3 = "a",
            option4 = "v",
            correctAnswer = "A",
            explanation = null,
            topicId = 1
        )

        val value = viewModel.insertQuestionStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert question with blank second option, returns error`() {
        viewModel.insertQuestion(
            quiz = "some",
            option1 = "a",
            option2 = "",
            option3 = "a",
            option4 = "v",
            correctAnswer = "A",
            explanation = null,
            topicId = 1
        )

        val value = viewModel.insertQuestionStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert question with blank third option, returns error`() {
        viewModel.insertQuestion(
            quiz = "some",
            option1 = "",
            option2 = "o",
            option3 = "    ",
            option4 = "v",
            correctAnswer = "A",
            explanation = null,
            topicId = 1
        )

        val value = viewModel.insertQuestionStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert question with blank fourth option, returns error`() {
        viewModel.insertQuestion(
            quiz = "some",
            option1 = "z",
            option2 = "o",
            option3 = "a",
            option4 = "   ",
            correctAnswer = "A",
            explanation = null,
            topicId = 1
        )

        val value = viewModel.insertQuestionStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert question with blank correct answer, returns error`() {
        viewModel.insertQuestion(
            quiz = "some",
            option1 = "a",
            option2 = "o",
            option3 = "a",
            option4 = "v",
            correctAnswer = "   ",
            explanation = null,
            topicId = 1
        )

        val value = viewModel.insertQuestionStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert question with invalid correct answer, returns error`() {
        viewModel.insertQuestion(
            quiz = "quiz",
            option1 = "a",
            option2 = "b",
            option3 = "c",
            option4 = "d",
            correctAnswer = "T",
            explanation = null,
            topicId = 1
        )

        val value = viewModel.insertQuestionStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert question with valid fields, returns success`() {
        viewModel.insertQuestion(
            quiz = "quiz",
            option1 = "a",
            option2 = "b",
            option3 = "c",
            option4 = "d",
            correctAnswer = "A",
            explanation = null,
            topicId = 1
        )

        val value = viewModel.insertQuestionStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun `when user delete a question, returns success`() {
        val fakeQuestion = Question(
            id = 9L, question = "sth", option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 2, explanation = null, isBookmark = 0, isUserAdded = 0, topicId = 2
        )

        viewModel.deleteQuestionAndAnswers(question = fakeQuestion)

        val value = viewModel.deleteQuestionStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }
}