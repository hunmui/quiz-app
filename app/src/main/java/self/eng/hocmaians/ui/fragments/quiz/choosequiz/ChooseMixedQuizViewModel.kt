package self.eng.hocmaians.ui.fragments.quiz.choosequiz

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import self.eng.hocmaians.repositories.IRepository
import self.eng.hocmaians.util.Constants.ZERO_QUESTIONS
import self.eng.hocmaians.util.Event
import self.eng.hocmaians.util.Resource
import javax.inject.Inject

@HiltViewModel
class ChooseMixedQuizViewModel @Inject constructor(
    repository: IRepository
) : ViewModel() {

    // variables for Choose Mixed Quiz Fragment
    var totalQuestionsInDb: Long = ZERO_QUESTIONS.toLong()
    var chosenQuantity: Int? = null

    // live data from db for Choose Mixed Quiz Fragment to observe
    val allQuestions: LiveData<Long> = repository.countAllQuestions()

    // state of start test for Choose Mixed Quiz Fragment to observe
    private var _startTestStatus = MutableLiveData<Event<Resource<String>>>()
    val startTestStatus: LiveData<Event<Resource<String>>> = _startTestStatus

    /**
     * When user choose question quantity to do test, update chosenQuantity.
     *
     * @param quantity user chosen quantity
     */
    fun onChooseQuantity(quantity: Int) {
        chosenQuantity = quantity
    }

    /**
     * When user click Start test button. Check if all conditions to start test are met or not, then
     * post value to the UI to observe
     */
    fun onStartTest() {
        chosenQuantity?.let {
            if (it > totalQuestionsInDb) {
                _startTestStatus.postValue(
                    Event(
                        Resource.error(
                            msg = "You picked more questions than we offer. Please " +
                                    "choose a smaller quantity!",
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
        } ?: _startTestStatus.postValue(
            Event(
                Resource.error(msg = "You have to choose quantity first", data = null)
            )
        )
    }
}