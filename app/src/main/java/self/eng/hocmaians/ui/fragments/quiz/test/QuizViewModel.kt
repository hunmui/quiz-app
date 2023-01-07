package self.eng.hocmaians.ui.fragments.quiz.test

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import self.eng.hocmaians.data.entities.Question
import self.eng.hocmaians.data.entities.Score
import self.eng.hocmaians.data.entities.UserAnswer
import self.eng.hocmaians.repositories.IRepository
import self.eng.hocmaians.util.Constants.NOT_ANSWER_YET
import self.eng.hocmaians.util.Constants.ZERO_QUESTIONS
import self.eng.hocmaians.util.Event
import self.eng.hocmaians.util.Resource
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: IRepository
) : ViewModel() {

    // variables for Quiz Fragment
    lateinit var questionList: List<Question>
    var doneQuantity = 0
    private var currentQuestionPos = 0
    var timestamp: Long = 0

    // live data from db for Quiz Fragment to observe
    lateinit var questions: LiveData<List<Question>>

    // list of answers for Quiz Fragment to observe
    private val answerList: MutableList<Int> = mutableListOf()
    private var _answers = MutableLiveData<List<Int>>()
    var answers: LiveData<List<Int>> = _answers

    // saving answers and score state for Quiz Fragment to observe
    private var _save = MutableLiveData<Event<Resource<String>>>()
    val save: LiveData<Event<Resource<String>>> = _save

    /**
     * Which type of questions to get (by topic or mixed question)
     *
     * @param questionQuantity how many questions to get if coming from Choose Mixed Quiz Fragment
     * @param topicId get questions from the chosen topic
     */
    fun getQuestions(questionQuantity: Int, topicId: Int) {
        questions = if (questionQuantity == ZERO_QUESTIONS) {
            repository.getQuestionsBasedOnTopic(topicId = topicId)
        } else {
            repository.getRandomQuestions(quantity = questionQuantity)
        }
    }

    /**
     * Initialize all variables for Quiz Fragment (questionList, doneQuantity, currentQuestionPos)
     *
     * @param questions list of questions that Quiz Fragment observed
     */
    fun initialize(questions: List<Question>) {
        questionList = questions
        doneQuantity = 0
        currentQuestionPos = 0

        answerList.clear()

        for (i in questionList.indices) {
            answerList.add(NOT_ANSWER_YET)
        }

        _answers.postValue(answerList)
    }

    /**
     * When user answer a question.
     *
     * @param currentPosition current question (viewpager) position
     * @param answerPosition user answer position (1, 2, 3, or 4)
     * @return viewPager next page (could be next page or just stay at the current page)
     */
    fun onAnswerQuestion(
        currentPosition: Int,
        answerPosition: Int
    ): Int {
        currentQuestionPos = currentPosition

        return when {
            answerList[currentQuestionPos] == NOT_ANSWER_YET -> {
                // save the answer
                answerList[currentQuestionPos] = answerPosition
                _answers.postValue(answerList)

                // update the done quantity
                doneQuantity++

                // move to next page of ViewPager2
                currentQuestionPos + 1
            }
            answerList[currentQuestionPos] != answerPosition -> {
                // already answered, then update the answer
                answerList[currentQuestionPos] = answerPosition

                _answers.postValue(answerList)
                currentQuestionPos
            }
            else -> {
                currentQuestionPos
            }
        }
    }

    /**
     * When user submit test. Save user answers, and score. If there is any error, post the result
     * via _save
     *
     * @param topicId topic id
     */
    fun onSubmitTest(topicId: Int) {
        timestamp = System.currentTimeMillis()
        var totalCorrect = 0

        viewModelScope.launch {

            // insert user answers
            for (i in answerList.indices) {
                val insertAnswerJob: Job = insertUserAnswer(
                    UserAnswer(
                        answerNumber = answerList[i],
                        questionId = questionList[i].id,
                        timestamp = timestamp
                    )
                )
                if (answerList[i] == questionList[i].answerNr)
                    totalCorrect++
                insertAnswerJob.join()

                // insert answer fail
                if (insertAnswerJob.isCancelled) {
                    _save.postValue(
                        Event(
                            Resource.error(
                                msg = "Error when trying to save answers. Please submit again!",
                                data = null
                            )
                        )
                    )
                    deleteAnswersByTimestamp(timestamp = timestamp)
                    return@launch
                }
            }

            val insertScoreJob: Job = insertUserScore(
                Score(
                    timestamp = timestamp,
                    topicId = topicId,
                    totalCorrect = totalCorrect,
                    totalQuestions = questionList.size
                )
            )

            insertScoreJob.join()

            // insert score fail
            if (insertScoreJob.isCancelled) {
                _save.postValue(
                    Event(
                        Resource.error(
                            msg = "Error when trying to save score. Please submit again!",
                            data = null
                        )
                    )
                )
                deleteScoreAndAnswersByTimestamp(timestamp = timestamp)
                return@launch
            }

            _save.postValue(
                Event(
                    Resource.success("Success")
                )
            )
        }
    }

    private fun deleteAnswersByTimestamp(timestamp: Long) = viewModelScope.launch {
        repository.deleteUserAnswersByTimeStamp(timestamp = timestamp)
    }

    private fun deleteScoreAndAnswersByTimestamp(timestamp: Long) = viewModelScope.launch {
        repository.deleteScoreByTimeStamp(timestamp = timestamp)
        repository.deleteUserAnswersByTimeStamp(timestamp = timestamp)
    }

    private fun insertUserAnswer(userAnswer: UserAnswer) = viewModelScope.launch {
        repository.insertUserAnswer(userAnswer = userAnswer)
    }

    private fun insertUserScore(score: Score) = viewModelScope.launch {
        repository.insertUserScore(score = score)
    }
}