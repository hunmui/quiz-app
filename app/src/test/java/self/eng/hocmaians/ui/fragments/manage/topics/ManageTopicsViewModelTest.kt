package self.eng.hocmaians.ui.fragments.manage.topics

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import self.eng.hocmaians.MainCoroutineRule
import self.eng.hocmaians.data.entities.Topic
import self.eng.hocmaians.getOrAwaitValueTest
import self.eng.hocmaians.repositories.FakeRepository
import self.eng.hocmaians.util.Constants
import self.eng.hocmaians.util.Status

@ExperimentalCoroutinesApi
class ManageTopicsViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: ManageTopicsViewModel

    @Before
    fun setup() {
        viewModel = ManageTopicsViewModel(FakeRepository())
    }

    @Test
    fun `insert topic with blank name, returns error`() {
        viewModel.insertTopic(
            topicName = "   ",
            existingTopicNames = listOf(),
            courseId = 9
        )

        val value = viewModel.insertTopicStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert topic with duplicate name, returns error`() {
        val existingTopicNames: List<String> = listOf("topic1", "topic2")
        viewModel.insertTopic(
            topicName = "Topic1   ",
            existingTopicNames = existingTopicNames,
            courseId = 10
        )

        val value = viewModel.insertTopicStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert topic with too long name, returns error`() {
        val string = buildString {
            for (i in 1..Constants.MAX_TOPIC_NAME_LENGTH + 1) {
                append(1)
            }
        }

        viewModel.insertTopic(topicName = string, existingTopicNames = listOf(), courseId = 1)
        val value = viewModel.insertTopicStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert topic with valid name, returns success`() {
        viewModel.insertTopic(
            topicName = "topic name",
            existingTopicNames = listOf(),
            courseId = 10
        )
        val value = viewModel.insertTopicStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun `when user delete a topic, returns success`() {
        val fakeTopic = Topic(id = 1, name = "a", priority = 0, isUserAdded = 0, courseId = 1)
        viewModel.deleteTopic(topic = fakeTopic)

        val value = viewModel.deleteTopicStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }
}