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

    // biến cho Chọn câu hỏi theo chủ đề
    var chosenCourse: Course? = null
    var chosenTopic: Topic? = null
    var totalQuestionsInTopic: Int = ZERO_QUESTIONS

    // Dữ liệu trực tiếp cho Chọn câu hỏi theo chủ đề để quan sát
    val courses: LiveData<List<Course>> = repository.getAllCourses()
    lateinit var topicsByCourse: LiveData<List<Topic>>
    lateinit var totalQuestionsByTopic: LiveData<Int>

    // trạng thái bắt đầu kiểm tra Chọn câu hỏi theo chủ đề để quan sát
    private val _startTestStatus = MutableLiveData<Event<Resource<String>>>()
    val startTestStatus: LiveData<Event<Resource<String>>> = _startTestStatus

    /**
     * Khi người dùng chọn một khóa học từ spinner. Cập nhật khóa học đã chọn, sau đó nhận tất cả các chủ đề thuộc về
     * khóa học đó
     *
     * @param course khóa học đã chọn
     */
    fun onChooseCourse(course: Course) {
        chosenCourse = course
        getTopicsBasedOnCourse(course.id)
    }

    /**
     * Khi người dùng chọn một chủ đề từ spinner. Cập nhật chủ đề đã chọn, sau đó đếm tất cả các câu hỏi trong đó
     * đề tài
     *
     * @param topic  chủ đề đã chọn
     */
    fun onChooseTopic(topic: Topic) {
        chosenTopic = topic
        totalQuestionsByTopic = countQuestionsBasedOnTopic(topic.id)
    }

    /**
     * Khi người dùng nhấp vào nút Bắt đầu kiểm tra. Kiểm tra nếu tất cả các điều kiện được đáp ứng để bắt đầu thử nghiệm.
     * Nếu không, gửi thông báo lỗi. Khác, đăng thông báo thành công.
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
     * Nhận các chủ đề dựa trên khóa học đã chọn.
     *
     * @param courseId đã chọn id khóa học
     */
    private fun getTopicsBasedOnCourse(courseId: Int) {
        topicsByCourse = repository.getTopicsBasedOnCourse(courseId = courseId)
    }

    /**
     * Đếm tất cả các câu hỏi trong chủ đề đã chọn
     *
     * @param topicId id chủ đề đã chọn
     */
    private fun countQuestionsBasedOnTopic(topicId: Int): LiveData<Int> =
        repository.countQuestionsBasedOnTopic(topicId = topicId)

    /**
     * Tăng mức độ ưu tiên của khóa học đã chọn lên 1
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
     * Tăng mức độ ưu tiên của chủ đề đã chọn lên 1
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