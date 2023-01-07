package self.eng.hocmaians.ui.fragments.quiz.choosequiz

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import self.eng.hocmaians.MainCoroutineRule
import self.eng.hocmaians.getOrAwaitValueTest
import self.eng.hocmaians.repositories.FakeRepository
import self.eng.hocmaians.util.Status

@ExperimentalCoroutinesApi
class ChooseMixedQuizViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: ChooseMixedQuizViewModel

    @Before
    fun setup() {
        viewModel = ChooseMixedQuizViewModel(FakeRepository())
    }

    @Test
    fun `start mixed quiz with null question quantity, returns error`() {
        viewModel.chosenQuantity = null
        viewModel.onStartTest()

        val value = viewModel.startTestStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `start mixed quiz with chosen quantity greater than total questions in db, returns error`() {
        viewModel.totalQuestionsInDb = 59
        viewModel.onChooseQuantity(quantity = 60)
        viewModel.onStartTest()

        val value = viewModel.startTestStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `start mixed quiz with all valid requirements, returns success`() {
        viewModel.totalQuestionsInDb = 50
        viewModel.onChooseQuantity(quantity = 50)
        viewModel.onStartTest()

        val value = viewModel.startTestStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }
}