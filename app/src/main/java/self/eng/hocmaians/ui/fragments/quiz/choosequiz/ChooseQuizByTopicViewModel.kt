package self.eng.hocmaians.ui.fragments.quiz.choosequiz

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import self.eng.hocmaians.data.entities.Course
import self.eng.hocmaians.data.entities.Topic
import self.eng.hocmaians.repositories.IRepository
import self.eng.hocmaians.util.Constants.ZERO_QUESTIONS
import self.eng.hocmaians.util.Event
import self.eng.hocmaians.util.Resource
import javax.inject.Inject

/**
 * View Model, handles all logic for ChooseQuizByTopicFragment
 */
@HiltViewModel
class ChooseQuizByTopicViewModel @Inject constructor(
    private val repository: IRepository
) : ViewModel() {

    // variable for Choose Quiz By Topic
    var chosenCourse: Course? = null
    var chosenTopic: Topic? = null
    var totalQuestionsInTopic: Int = ZERO_QUESTIONS

    // Live data for Choose Quiz By Topic to observe
    val courses: LiveData<List<Course>> = repository.getAllCourses()
    lateinit var topicsByCourse: LiveData<List<Topic>>
    lateinit var totalQuestionsByTopic: LiveData<Int>

    // state of start test for Choose Quiz By Topic to observe
    private val _startTestStatus = MutableLiveData<Event<Resource<String>>>()
    val startTestStatus: LiveData<Event<Resource<String>>> = _startTestStatus

    /**
     * When user choose a course from spinner. Update chosen course, then get all topics belong to
     * that course
     *
     * @param course chosen course
     */
    fun onChooseCourse(course: Course) {
        chosenCourse = course
        getTopicsBasedOnCourse(course.id)
    }

    /**
     * When user choose a topic from spinner. Update chosen topic, then count all questions in that
     * topic
     *
     * @param topic chosen topic
     */
    fun onChooseTopic(topic: Topic) {
        chosenTopic = topic
        totalQuestionsByTopic = countQuestionsBasedOnTopic(topic.id)
    }

    /**
     * When user click Start test button. Check if all conditions are met in order to start test.
     * If not, post error message. Else, post success message.
     */
    fun onStartTest() {
        if (chosenCourse == null) {
            _startTestStatus.postValue(
                Event(
                    Resource.error(msg = "No course has been chosen", data = null)
                )
            )
            return
        }
        if (chosenTopic == null) {
            _startTestStatus.postValue(
                Event(
                    Resource.error(msg = "No topic has been chosen", data = null)
                )
            )
            return
        }
        if (totalQuestionsInTopic == ZERO_QUESTIONS) {
            _startTestStatus.postValue(
                Event(
                    Resource.error(
                        msg = "Since there is 0 questions, you cannot do the test!",
                        data = null
                    )
                )
            )
            return
        }

        _startTestStatus.postValue(
            Event(
                Resource.success("Success")
            )
        )

        increaseCoursePriority()
        increaseTopicPriority()
    }

    /* ----------------------------- DB related methods ----------------------------- */

    /**
     * Get topics based on the chosen course.
     *
     * @param courseId chosen course id
     */
    private fun getTopicsBasedOnCourse(courseId: Int) {
        topicsByCourse = repository.getTopicsBasedOnCourse(courseId = courseId)
    }

    /**
     * Count all questions in the chosen topic
     *
     * @param topicId chosen topic id
     */
    private fun countQuestionsBasedOnTopic(topicId: Int): LiveData<Int> =
        repository.countQuestionsBasedOnTopic(topicId = topicId)

    /**
     * Increase the chosen course priority by 1
     */
    private fun increaseCoursePriority() {
        // increase chosen course priority, then update that course
        chosenCourse?.let {
            viewModelScope.launch {
                repository.updateCoursePriority(
                    courseId = it.id,
                    newCoursePriority = ++it.priority
                )
            }
        }
    }

    /**
     * Increase the chosen topic priority by 1
     */
    private fun increaseTopicPriority() {
        // increase selected topic priority, then update that topic
        chosenTopic?.let {
            viewModelScope.launch {
                repository.updateTopicPriority(
                    topicId = it.id,
                    newTopicPriority = ++it.priority
                )
            }
        }
    }
}