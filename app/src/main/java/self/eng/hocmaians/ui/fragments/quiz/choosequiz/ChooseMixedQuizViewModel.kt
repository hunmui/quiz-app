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

    // các biến cho Choose Mixed Quiz Fragment
    var totalQuestionsInDb: Long = ZERO_QUESTIONS.toLong()
    var chosenQuantity: Int? = null

    // dữ liệu trực tiếp từ db cho Choose Mixed Quiz Fragment để quan sát
    val allQuestions: LiveData<Long> = repository.countAllQuestions()

    // trạng thái bắt đầu bài kiểm tra Chọn Đoạn bài kiểm tra hỗn hợp để quan sát
    private var _startTestStatus = MutableLiveData<Event<Resource<String>>>()
    val startTestStatus: LiveData<Event<Resource<String>>> = _startTestStatus

    /**
     * Khi người dùng chọn số lượng câu hỏi để kiểm tra, hãy cập nhật Số lượng đã chọn.
     *
     * @param quantity người dùng đã chọn số lượng
     */
    fun onChooseQuantity(quantity: Int) {
        chosenQuantity = quantity
    }

    /**
     * Khi người dùng nhấp vào nút Bắt đầu kiểm tra. Kiểm tra xem tất cả các điều kiện để bắt đầu thử nghiệm có được đáp ứng hay không, sau đó
     * đăng giá trị lên giao diện người dùng để quan sát
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