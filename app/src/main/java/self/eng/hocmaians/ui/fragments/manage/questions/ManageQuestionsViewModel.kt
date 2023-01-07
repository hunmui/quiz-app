package self.eng.hocmaians.ui.fragments.manage.questions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import self.eng.hocmaians.data.entities.Question
import self.eng.hocmaians.repositories.IRepository
import self.eng.hocmaians.util.Constants.ACTION_ADD_QUESTION
import self.eng.hocmaians.util.Constants.ACTION_EDIT_QUESTION
import self.eng.hocmaians.util.Constants.QUESTION_NOT_BOOKMARKED
import self.eng.hocmaians.util.Constants.USER_ADDED
import self.eng.hocmaians.util.Event
import self.eng.hocmaians.util.Resource
import javax.inject.Inject

@HiltViewModel
class ManageQuestionsViewModel @Inject constructor(
    private val repository: IRepository
) : ViewModel() {

    /* -------------------------- Manage questions fragment -------------------------- */
    fun getQuestionsByTopic(topicId: Int): LiveData<List<Question>> =
        repository.getQuestionsBasedOnTopic(topicId = topicId)

    fun getUserAddedQuestionsByTopic(topicId: Int): LiveData<List<Question>> =
        repository.getUserAddedQuestionsByTopic(topicId = topicId)

//    fun deleteQuestion(question: Question) = viewModelScope.launch {
//        repository.deleteQuestion(question = question)
//    }

    private fun insertQuestionIntoDb(question: Question) = viewModelScope.launch {
        repository.insertQuestion(question = question)
    }

    /* -------------------------- Add, Edit questions fragment -------------------------- */
    // insert, update question state for Fragment to observe
    private val _insertQuestionStatus = MutableLiveData<Event<Resource<Question>>>()
    val insertQuestionStatus: LiveData<Event<Resource<Question>>> = _insertQuestionStatus

    private val _updateQuestionStatus = MutableLiveData<Event<Resource<Question>>>()
    val updateQuestionStatus: LiveData<Event<Resource<Question>>> = _updateQuestionStatus

    private val _deleteQuestionStatus = MutableLiveData<Event<Resource<String>>>()
    val deleteQuestionStatus: LiveData<Event<Resource<String>>> = _deleteQuestionStatus

    /**
     * Post value when there's an error when adding OR updating a question
     *
     * @param action is the action Add question, or Edit question
     * @param errorMessage the error message to post
     */
    private fun insertUpdateQuestionError(action: String, errorMessage: String) {
        if (action == ACTION_ADD_QUESTION) {
            _insertQuestionStatus.postValue(
                Event(
                    Resource.error(
                        msg = errorMessage,
                        data = null
                    )
                )
            )
        } else {
            _updateQuestionStatus.postValue(
                Event(
                    Resource.error(
                        msg = errorMessage,
                        data = null
                    )
                )
            )
        }
    }

    /**
     * Insert question
     */
    fun insertQuestion(
        quiz: String,
        option1: String,
        option2: String,
        option3: String,
        option4: String,
        correctAnswer: String,
        explanation: String?,
        topicId: Int
    ) {
        if (quiz.isBlank()) {
            insertUpdateQuestionError(
                action = ACTION_ADD_QUESTION,
                errorMessage = "Question must not be blank"
            )
            return
        }
        if (option1.isBlank()) {
            insertUpdateQuestionError(
                action = ACTION_ADD_QUESTION,
                errorMessage = "First option must not be blank"
            )
            return
        }
        if (option2.isBlank()) {
            insertUpdateQuestionError(
                action = ACTION_ADD_QUESTION,
                errorMessage = "Second option must not be blank"
            )
            return
        }
        if (option3.isBlank()) {
            insertUpdateQuestionError(
                action = ACTION_ADD_QUESTION,
                errorMessage = "Third option must not be blank"
            )
            return
        }
        if (option4.isBlank()) {
            insertUpdateQuestionError(
                action = ACTION_ADD_QUESTION,
                errorMessage = "Fourth option must not be blank"
            )
            return
        }
        if (correctAnswer.isBlank()) {
            insertUpdateQuestionError(
                action = ACTION_ADD_QUESTION,
                errorMessage = "Correct answer must bot be blank"
            )
            return
        }
        if (correctAnswer.trim().lowercase() !in listOfValidAnswer) {
            insertUpdateQuestionError(
                action = ACTION_ADD_QUESTION,
                errorMessage = "Invalid correct answer. Correct answer is only A-D in uppercase " +
                        "or a-d in lowercase"
            )
            return
        }

//        // admin add
//        val question = Question(
//            question = quiz.trim(),
//            option1 = option1.trim(),
//            option2 = option2.trim(),
//            option3 = option3.trim(),
//            option4 = option4.trim(),
//            answerNr = convertAnswerLetterToInt(correctAnswer.trim().lowercase()),
//            explanation = explanation?.trim(),
//            isBookmark = QUESTION_NOT_BOOKMARKED,
//            // TODO: change to userAdded after filling all data
//            isUserAdded = ADMIN_ADDED,
//            topicId = topicId
//        )

        // production code (user add)
        val question = Question(
            question = quiz.trim(),
            option1 = option1.trim(),
            option2 = option2.trim(),
            option3 = option3.trim(),
            option4 = option4.trim(),
            answerNr = convertAnswerLetterToInt(correctAnswer.trim().lowercase()),
            explanation = explanation?.trim(),
            isBookmark = QUESTION_NOT_BOOKMARKED,
            isUserAdded = USER_ADDED,
            topicId = topicId
        )
        insertQuestionIntoDb(question)
        _insertQuestionStatus.postValue(Event(Resource.success(question)))
    }

    fun editQuestion(
        question: Question,
        quiz: String,
        option1: String,
        option2: String,
        option3: String,
        option4: String,
        correctAnswer: String,
        explanation: String?,
    ) {
        if (quiz.isBlank()) {
            insertUpdateQuestionError(
                action = ACTION_EDIT_QUESTION,
                errorMessage = "Question must not be blank"
            )
            return
        }
        if (option1.isBlank()) {
            insertUpdateQuestionError(
                action = ACTION_EDIT_QUESTION,
                errorMessage = "First option must not be blank"
            )
            return
        }
        if (option2.isBlank()) {
            insertUpdateQuestionError(
                action = ACTION_EDIT_QUESTION,
                errorMessage = "Second option must not be blank"
            )
            return
        }
        if (option3.isBlank()) {
            insertUpdateQuestionError(
                action = ACTION_EDIT_QUESTION,
                errorMessage = "Third option must not be blank"
            )
            return
        }
        if (option4.isBlank()) {
            insertUpdateQuestionError(
                action = ACTION_EDIT_QUESTION,
                errorMessage = "Fourth option must not be blank"
            )
            return
        }
        if (correctAnswer.isBlank()) {
            insertUpdateQuestionError(
                action = ACTION_EDIT_QUESTION,
                errorMessage = "Correct answer must bot be blank"
            )
            return
        }
        if (correctAnswer.trim().lowercase() !in listOfValidAnswer) {
            insertUpdateQuestionError(
                action = ACTION_EDIT_QUESTION,
                errorMessage = "Invalid correct answer. Correct answer is only A-D in uppercase " +
                        "or a-d in lowercase"
            )
            return
        }

        question.question = quiz.trim()
        question.option1 = option1.trim()
        question.option2 = option2.trim()
        question.option3 = option3.trim()
        question.option4 = option4.trim()
        question.answerNr = convertAnswerLetterToInt(correctAnswer.trim().lowercase())
        question.explanation = explanation?.trim()

        updateQuestion(question)
        _updateQuestionStatus.postValue(Event(Resource.success(null)))
    }

    fun deleteQuestionAndAnswers(question: Question) {
        viewModelScope.launch {
            val job = viewModelScope.launch {
                repository.deleteQuestion(question = question)
                repository.deleteUserAnswerBasedOnQuestion(questionId = question.id)
            }

            job.join()

            if (job.isCancelled) {
                _deleteQuestionStatus.postValue(
                    Event(
                        Resource.error(
                            msg = "There is some error while deleting the question. Please try again",
                            data = null
                        )
                    )
                )
            } else {
                _deleteQuestionStatus.postValue(
                    Event(
                        Resource.success("Delete question successfully")
                    )
                )
            }
        }
    }

    private fun updateQuestion(question: Question) = viewModelScope.launch {
        repository.updateQuestion(question = question)
    }

    val listOfValidAnswer: List<String> = listOf("a", "b", "c", "d")

    private fun convertAnswerLetterToInt(answerLetter: String): Int {
        return when (answerLetter) {
            "a" -> 1
            "b" -> 2
            "c" -> 3
            "d" -> 4
            else -> -1
        }
    }
}