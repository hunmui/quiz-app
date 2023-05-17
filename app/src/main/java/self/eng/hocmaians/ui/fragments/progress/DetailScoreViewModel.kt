package self.eng.hocmaians.ui.fragments.progress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import self.eng.hocmaians.data.entities.Course
import self.eng.hocmaians.data.entities.Score
import self.eng.hocmaians.data.entities.Topic
import self.eng.hocmaians.repositories.IRepository
import self.eng.hocmaians.util.Constants.COURSE_BASED_RESULTS
import self.eng.hocmaians.util.Constants.MIXED_QUIZ_RESULTS
import self.eng.hocmaians.util.Constants.OVERALL_RESULTS
import self.eng.hocmaians.util.Constants.TOPIC_BASED_RESULTS
import self.eng.hocmaians.util.Event
import self.eng.hocmaians.util.Resource
import java.text.DecimalFormat
import javax.inject.Inject

@HiltViewModel
class DetailScoreViewModel @Inject constructor(
    private val repository: IRepository
) : ViewModel() {

    // các biến để ProgressFragment quan sát
    var showFirstLayout = true
    var showSecondLayout = false
    var showThirdLayout = false
    var showFilterByTopicSpinner = false

    var getGraphDataBy: Int = OVERALL_RESULTS
    var chosenCourse: Course? = null
    var chosenTopic: Topic? = null

    // dữ liệu trực tiếp từ db cho ProgressFragment để quan sát
    val courses: LiveData<List<Course>> = repository.getAllCourses()
    lateinit var topicsByCourse: LiveData<List<Topic>>
    lateinit var scores: LiveData<List<Score>>

    var isScoresInitialized = false

    // lọc trạng thái tiến trình cho ProgressFragment để quan sát
    private var _filter = MutableLiveData<Event<Resource<String>>>()
    val filter: LiveData<Event<Resource<String>>> = _filter

    /**
     * Người dùng muốn lọc tiến trình tổng thể
     */
    fun filterByOverall() {
        getAllUserScores()
        getGraphDataBy = OVERALL_RESULTS
        isScoresInitialized = true
    }

    fun filterByMixedQuiz() {
        getUserScoresByMixedQuiz()
        getGraphDataBy = MIXED_QUIZ_RESULTS
        isScoresInitialized = true
    }

    /**
     * Người dùng chọn một khóa học để lọc tiến độ
     */
    fun onChooseCourse(course: Course) {
        chosenCourse = course

        if (showFilterByTopicSpinner) {
            getTopicsByCourse(courseId = course.id)
        }
    }

    /**
     * Người dùng nhấp vào nút Tiến trình lọc
     */
    fun onFilterProgress() {
        if (chosenCourse == null) {
            _filter.postValue(
                Event(
                    Resource.error(
                        msg = "No course has been chosen",
                        data = null
                    )
                )
            )
            return
        }
        if (chosenTopic == null && showFilterByTopicSpinner) {
            _filter.postValue(
                Event(
                    Resource.error(
                        msg = "No topic has been chosen",
                        data = null
                    )
                )
            )
            return
        }

        _filter.postValue(
            Event(
                Resource.success("Allow to filter progress")
            )
        )

        getGraphDataBy = when (showFilterByTopicSpinner) {
            true -> {
                getUserScoresByTopic(topicId = chosenTopic!!.id)
                isScoresInitialized = true
                TOPIC_BASED_RESULTS
            }
            false -> {
                getUserScoresByCourse(courseId = chosenCourse!!.id)
                isScoresInitialized = true
                COURSE_BASED_RESULTS
            }
        }
    }

    fun calculateAvgScore(scores: List<Score>): String {
        var scoreSum = 0.0

        scores.forEach { score ->
            scoreSum += (score.totalCorrect.toDouble() / score.totalQuestions) * 10.0
        }

        val decimalFormat = DecimalFormat("0.0")
        return decimalFormat.format(scoreSum / scores.size).replace(",", ".")
    }

    /* ----------------------------- DB related methods ----------------------------- */
    private fun getTopicsByCourse(courseId: Int) {
        topicsByCourse = repository.getTopicsBasedOnCourse(courseId = courseId)
    }

    private fun getAllUserScores() {
        scores = repository.getAllUserScores()
    }

    private fun getUserScoresByCourse(courseId: Int) {
        scores = repository.getUserScoresByCourse(courseId = courseId)
    }

    private fun getUserScoresByTopic(topicId: Int) {
        scores = repository.getUserScoresByTopic(topicId = topicId)
    }

    private fun getUserScoresByMixedQuiz() {
        scores = repository.getUserScoresByMixedQuiz()
    }
}