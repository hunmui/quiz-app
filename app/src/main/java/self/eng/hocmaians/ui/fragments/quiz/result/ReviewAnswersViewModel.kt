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

    // live data from db for ReviewAnswersFragment to observe
    lateinit var userAnswers: LiveData<List<AnswerAndQuestion>>

    // update question bookmark state for ReviewAnswersFragment to observe
    private var _updateBookmark = MutableLiveData<Event<Resource<String>>>()
    val updateBookmark: LiveData<Event<Resource<String>>> = _updateBookmark

    /**
     * get question and answer list
     *
     * @param action get the list based on action from fragment
     * @param timestamp the timestamp to get the list
     */
    fun getQuestionAndAnswerList(action: Int, timestamp: Long) {
        userAnswers = when (action) {
            QUIZ_FRAGMENT -> getUserAnswersNoSorting(timestamp = timestamp)
            else -> getUserAnswersOrderByQuestionId(timestamp = timestamp)
        }
    }

    /**
     * When user click on bookmark icon. Update the question bookmark.
     *
     * @param isBookmarked is the question bookmarked or not
     * @param questionsPosition update bookmark on which question
     */
    fun onUpdateBookmark(isBookmarked: Boolean, questionsPosition: Int) {
        // get the question that is bookmarked
        val question: Question = questionAnswerList[questionsPosition].question

        // update isBookmark field
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
     * get user answers sort by user answer id
     *
     * @param timestamp timestamp to get the list of user answer
     */
    private fun getUserAnswersNoSorting(timestamp: Long): LiveData<List<AnswerAndQuestion>> =
        repository.getUserAnswersNoSorting(timestamp = timestamp)

    /**
     * get user answers sort by question id
     *
     * @param timestamp timestamp to get the list of user answer
     */
    private fun getUserAnswersOrderByQuestionId(
        timestamp: Long
    ): LiveData<List<AnswerAndQuestion>> =
        repository.getUserAnswersOrderByQuestionId(timestamp = timestamp)

    /**
     * get user score based on timestamp
     *
     * @param timestamp timestamp to get user score
     */
    fun getUserScore(timestamp: Long): LiveData<Score> =
        repository.getUserScore(timestamp = timestamp)

    /**
     * update question bookmark
     *
     * @param questionId which question to be updated
     * @param bookmark is question bookmarked or not
     */
    private fun updateQuestionBookmark(questionId: Long, bookmark: Int) =
        viewModelScope.launch {
            repository.updateQuestionBookmark(questionId = questionId, bookmark = bookmark)
        }
}