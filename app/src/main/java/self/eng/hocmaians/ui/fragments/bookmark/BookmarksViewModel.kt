package self.eng.hocmaians.ui.fragments.bookmark

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import self.eng.hocmaians.data.entities.Course
import self.eng.hocmaians.data.entities.Question
import self.eng.hocmaians.data.entities.Topic
import self.eng.hocmaians.repositories.IRepository
import self.eng.hocmaians.util.Event
import self.eng.hocmaians.util.Resource
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val repository: IRepository
) : ViewModel() {

    // variable for Bookmarks fragment
    var isFilterSectionOpen = false
    var chosenCourse: Course? = null
    var chosenTopic: Topic? = null

    // dữ liệu trực tiếp từ db cho BookmarksFragment để quan sát
    val courses: LiveData<List<Course>> = repository.getAllCourses()
    lateinit var topicsByCourse: LiveData<List<Topic>>
    lateinit var bookmarks: LiveData<List<Question>>

    // bắt đầu lọc trạng thái cho BookmarksFragment để quan sát
    private var _filter = MutableLiveData<Event<Resource<String>>>()
    val filter: LiveData<Event<Resource<String>>> = _filter

    /**
     * Nhận tất cả dấu trang từ cơ sở dữ liệu
     */
    fun getAllBookmarks() {
        bookmarks = repository.getAllBookmarks()
    }

    /**
     * Khi người dùng chọn một khóa học từ spinner. Cập nhật khóa học đã chọn, sau đó nhận tất cả các chủ đề thuộc về
     * khóa học đó
     *
     * @param course đã chọn khóa học
     */
    fun onChooseCourse(course: Course) {
        chosenCourse = course
        getTopicsBasedOnCourse(course.id)
    }

    /**
     * Khi người dùng chọn một chủ đề từ spinner. Cập nhật chủ đề đã chọn, sau đó đếm tất cả các câu hỏi trong đó
     * đề tài
     *
     * @param topic đã chọn chủ đề
     */
    fun onChooseTopic(topic: Topic) {
        chosenTopic = topic
    }

    fun onFilterBookmarks() {
        if (chosenCourse == null) {
            _filter.postValue(
                Event(
                    Resource.error(
                        msg = "You have not chosen any course yet",
                        data = null
                    )
                )
            )
            return
        }
        if (chosenTopic == null) {
            _filter.postValue(
                Event(
                    Resource.error(
                        msg = "You have not chosen any topic yet",
                        data = null
                    )
                )
            )
            return
        }

        _filter.postValue(
            Event(
                Resource.success(
                    data = "You can filter bookmarks"
                )
            )
        )
        bookmarks = getBookmarksByTopic(topicId = chosenTopic!!.id)
    }

    /* ---------------------------------- DB related methods ---------------------------------- */
    private fun getTopicsBasedOnCourse(courseId: Int) {
        topicsByCourse = repository.getTopicsBasedOnCourse(courseId = courseId)
    }

    private fun getBookmarksByTopic(topicId: Int): LiveData<List<Question>> =
        repository.getBookmarksBasedOnTopicId(topicId = topicId)

    fun updateQuestionBookmark(questionId: Long, bookmark: Int) =
        viewModelScope.launch {
            repository.updateQuestionBookmark(questionId = questionId, bookmark = bookmark)
        }
}