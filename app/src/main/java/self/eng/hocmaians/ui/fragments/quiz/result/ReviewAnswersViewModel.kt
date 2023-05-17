package self.eng.hocmaians.ui.fragments.quiz.result

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import self.eng.hocmaians.data.entities.Question
import self.eng.hocmaians.data.entities.Score
import self.eng.hocmaians.data.entities.realtions.AnswerAndQuestion
import self.eng.hocmaians.repositories.IRepository
import self.eng.hocmaians.util.Constants.QUESTION_BOOKMARKED
import self.eng.hocmaians.util.Constants.QUESTION_NOT_BOOKMARKED
import self.eng.hocmaians.util.Constants.QUIZ_FRAGMENT
import self.eng.hocmaians.util.Event
import self.eng.hocmaians.util.Resource
import javax.inject.Inject

@HiltViewModel
class ReviewAnswersViewModel @Inject constructor(
    private val repository: IRepository
) : ViewModel() {

    // variable for ReviewAnswersFragment
    lateinit var questionAnswerList: List<AnswerAndQuestion>

    // dữ liệu trực tiếp từ db để ReviewAnswersFragment quan sát
    lateinit var userAnswers: LiveData<List<AnswerAndQuestion>>

    // cập nhật trạng thái đánh dấu câu hỏi để ReviewAnswersFragment quan sát
    private var _updateBookmark = MutableLiveData<Event<Resource<String>>>()
    val updateBookmark: LiveData<Event<Resource<String>>> = _updateBookmark

    /**
     * nhận danh sách câu hỏi và câu trả lời
     *
     * @param action lấy danh sách dựa trên hành động từ đoạn
     * @param timestamp dấu thời gian để lấy danh sách
     */
    fun getQuestionAndAnswerList(action: Int, timestamp: Long) {
        userAnswers = when (action) {
            QUIZ_FRAGMENT -> getUserAnswersNoSorting(timestamp = timestamp)
            else -> getUserAnswersOrderByQuestionId(timestamp = timestamp)
        }
    }

    /**
     * Khi người dùng nhấp vào biểu tượng dấu trang. Cập nhật dấu trang câu hỏi.
     *
     * @param isBookmarked là câu hỏi có được đánh dấu hay không
     * @param questionĐánh dấu cập nhật vị trí cho câu hỏi nào
     */
    fun onUpdateBookmark(isBookmarked: Boolean, questionsPosition: Int) {
        // lấy câu hỏi đã đánh dấu
        val question: Question = questionAnswerList[questionsPosition].question

        // cập nhật trường isBookmark
        val bookmark: Int = if (isBookmarked) {
            QUESTION_BOOKMARKED
        } else {
            QUESTION_NOT_BOOKMARKED
        }

        viewModelScope.launch {
            val updateJob: Job = updateQuestionBookmark(
                questionId = question.id,
                bookmark = bookmark
            )

            updateJob.join()

            if (updateJob.isCancelled) {
                _updateBookmark.postValue(
                    Event(
                        Resource.error(
                            msg = "Fail to update bookmark. Please try again",
                            data = null
                        )
                    )
                )
                return@launch
            }

            _updateBookmark.postValue(
                Event(
                    Resource.success(data = "Update question's bookmark successfully")
                )
            )
        }
    }

    /**
     * nhận câu trả lời của người dùng sắp xếp theo id câu trả lời của người dùng
     *
     * @param timestamp timestamp để lấy danh sách câu trả lời của người dùng
     */
    private fun getUserAnswersNoSorting(timestamp: Long): LiveData<List<AnswerAndQuestion>> =
        repository.getUserAnswersNoSorting(timestamp = timestamp)

    /**
     * nhận câu trả lời của người dùng sắp xếp theo id câu hỏi
     *
     * @param timestamp timestamp để lấy danh sách câu trả lời của người dùng
     */
    private fun getUserAnswersOrderByQuestionId(
        timestamp: Long
    ): LiveData<List<AnswerAndQuestion>> =
        repository.getUserAnswersOrderByQuestionId(timestamp = timestamp)

    /**
     * nhận điểm người dùng dựa trên dấu thời gian
     *
     * @param timestamp để lấy điểm của người dùng
     */
    fun getUserScore(timestamp: Long): LiveData<Score> =
        repository.getUserScore(timestamp = timestamp)

    /**
     * cập nhật dấu trang câu hỏi
     *
     * @param questionId câu hỏi nào sẽ được cập nhật
     * @param bookmark có được đánh dấu câu hỏi hay không
     */
    private fun updateQuestionBookmark(questionId: Long, bookmark: Int) =
        viewModelScope.launch {
            repository.updateQuestionBookmark(questionId = questionId, bookmark = bookmark)
        }
}