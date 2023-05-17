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

    // dữ liệu trực tiếp từ db cho Quiz Fragment để quan sát
    lateinit var questions: LiveData<List<Question>>

    // danh sách các câu trả lời cho Quiz Fragment để quan sát
    private val answerList: MutableList<Int> = mutableListOf()
    private var _answers = MutableLiveData<List<Int>>()
    var answers: LiveData<List<Int>> = _answers

    // lưu câu trả lời và trạng thái điểm cho Quiz Fragment để quan sát
    private var _save = MutableLiveData<Event<Resource<String>>>()
    val save: LiveData<Event<Resource<String>>> = _save

    /**
     * Loại câu hỏi nào sẽ nhận được (theo chủ đề hoặc câu hỏi hỗn hợp)
     *
     * @param questionQuantity lượng bao nhiêu câu hỏi nhận được nếu đến từ Choose Mixed Quiz Fragment
     * @param topicId nhận câu hỏi từ chủ đề đã chọn
     */
    fun getQuestions(questionQuantity: Int, topicId: Int) {
        questions = if (questionQuantity == ZERO_QUESTIONS) {
            repository.getQuestionsBasedOnTopic(topicId = topicId)
        } else {
            repository.getRandomQuestions(quantity = questionQuantity)
        }
    }

    /**
     * Khởi tạo tất cả các biến cho Quiz Fragment ( questionList, doneQuantity, currentQuestionPos)
     *
     * @param questions danh sách các câu hỏi mà Quiz Fragment quan sát được
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
     * Khi người dùng trả lời một câu hỏi.
     *
     * @param currentPosition vị trí hiện tại của câu hỏi (viewpager)
     * @param answerPosition vị trí câu trả lời của người dùng (1, 2, 3 hoặc 4)
     * @return viewPager trang tiếp theo (có thể là trang tiếp theo hoặc chỉ ở trang hiện tại)
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

                // cập nhật số lượng đã hoàn thành
                doneQuantity++

                // chuyển sang trang tiếp theo của ViewPager2
                currentQuestionPos + 1
            }
            answerList[currentQuestionPos] != answerPosition -> {
                // đã trả lời, sau đó cập nhật câu trả lời
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
     * Khi người dùng gửi bài kiểm tra. Lưu câu trả lời của người dùng và ghi điểm. Nếu có bất kỳ lỗi nào, hãy đăng kết quả
     * thông qua _save
     *
     * @param topicId id chủ đề
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