package self.eng.hocmaians.ui.fragments.quiz.choosequiz

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import self.eng.hocmaians.MainCoroutineRule
import self.eng.hocmaians.data.entities.Course
import self.eng.hocmaians.data.entities.Topic
import self.eng.hocmaians.getOrAwaitValueTest
import self.eng.hocmaians.repositories.FakeRepository
import self.eng.hocmaians.util.Constants.DEFAULT_PRIORITY
import self.eng.hocmaians.util.Constants.ZERO_QUESTIONS
import self.eng.hocmaians.util.Status

@ExperimentalCoroutinesApi

class ChooseQuizByTopicViewModelTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: ChooseQuizByTopicViewModel

    @Before
    fun setup() {
        viewModel = ChooseQuizByTopicViewModel(FakeRepository())
    }

    @Test
    fun `start test by topic with null course, returns error`() {
        viewModel.onStartTest()

        val value = viewModel.startTestStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `start test by topic with null topic, returns error`() {
        viewModel.chosenCourse = Course(id = 1, name = "r1", priority = 0)
        viewModel.chosenTopic = null

        viewModel.onStartTest()

        val value = viewModel.startTestStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `start test by topic with no questions, returns error`() {
        viewModel.chosenCourse = Course(id = 1, name = "r1", priority = 0)
        viewModel.chosenTopic = Topic(
            id = 1, name = "e", isUserAdded = 0, priority = 0, courseId = 1
        )
        viewModel.totalQuestionsInTopic = ZERO_QUESTIONS

        viewModel.onStartTest()

        val value = viewModel.startTestStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `start test by topic with valid requirements, returns success`() {
        val courseId = 1
        val course = Course(id = courseId, name = "Grammar", priority = DEFAULT_PRIORITY)
        val topic = Topic(id = 5, name = "T1", isUserAdded = 0, priority = 1, courseId = courseId)

        viewModel.chosenCourse = course
        viewModel.chosenTopic = topic
        viewModel.totalQuestionsInTopic = 29

        viewModel.onStartTest()

        val value = viewModel.startTestStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }
}