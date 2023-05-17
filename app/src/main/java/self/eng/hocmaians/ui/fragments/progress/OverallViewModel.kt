package self.eng.hocmaians.ui.fragments.progress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import self.eng.hocmaians.data.entities.Course
import self.eng.hocmaians.data.entities.Score
import self.eng.hocmaians.data.entities.Topic
import self.eng.hocmaians.repositories.IRepository
import self.eng.hocmaians.ui.fragments.progress.model.AvgScoreAndLabel
import self.eng.hocmaians.util.Constants.FILTER_BY_OVERALL
import self.eng.hocmaians.util.Constants.MIXED_QUIZ
import self.eng.hocmaians.util.Constants.OVER_MAXIMUM_SCORE_OF_100
import javax.inject.Inject

@HiltViewModel
class OverallViewModel @Inject constructor(
    private val repository: IRepository
) : ViewModel() {

    val courses: LiveData<List<Course>> = repository.getAllCourses()

    // dữ liệu trực tiếp cho WholeFragment để quan sát để tải dữ liệu cho biểu đồ radar của nó
    private val radarChartAvgScores: MutableList<AvgScoreAndLabel> = mutableListOf()
    private var _radarChartData = MutableLiveData<List<AvgScoreAndLabel>>()
    val radarChartData: LiveData<List<AvgScoreAndLabel>> = _radarChartData

    // course that user need to improve on (radar chart)
    private var _courseNameNeedToImprove = MutableLiveData("")
    val courseNameNeedToImprove: LiveData<String> = _courseNameNeedToImprove

    // dữ liệu trực tiếp cho WholeFragment để quan sát để tải dữ liệu cho biểu đồ hình tròn của nó
    private val pieChartAvgScores: MutableList<AvgScoreAndLabel> = mutableListOf()
    private var _pieChartData = MutableLiveData<List<AvgScoreAndLabel>>()
    val pieChartData: LiveData<List<AvgScoreAndLabel>> = _pieChartData

    // đối với biểu đồ hình tròn, phải đảm bảo đạt đủ số lượng dữ liệu trước khi đặt dữ liệu cho biểu đồ hình tròn
    var ensureDataQuantity: Int = 0

    // giữ vị trí được chọn cho cửa sổ bật lên MaterialAlertDialog
    var chosenDialogIndex = FILTER_BY_OVERALL

    /**
     * Tính điểm trung bình trong mỗi khóa học. Điểm trung bình được trình bày dưới dạng phần trăm
     * (9,3 sẽ là 93%). Sau đó, hãy để _avgScores đăng giá trị mới nhất.
     *
     * @param scores danh sách điểm trong khóa học tương ứng
     * @param labelName tên khóa học để tải nhãn biểu đồ radar
     */
    fun calAvgScoreInPercentage(scores: List<Score>, labelName: String) {
        var scoreSum = 0f

        scores.forEach { score ->
            scoreSum += (score.totalCorrect.toFloat() / score.totalQuestions) * 10f
        }

        // điểm trung bình trong mỗi khóa học theo tỷ lệ phần trăm
        val avgScore = (scoreSum / scores.size) * 10f
        radarChartAvgScores.add(AvgScoreAndLabel(avgScore = avgScore, labelName = labelName))

        _radarChartData.postValue(radarChartAvgScores)
    }

    /**
     * Lấy tên môn học có điểm trung bình thấp nhất (trừ Trắc nghiệm hỗn hợp)
     */
    fun getCourseThatHasTheLowestAvgScore() {
        var courseNameToFind = ""

        // vì điểm trung bình ở đây tính theo phần trăm nên điểm tối đa của 10 bây giờ là 100
        var lowestAvgScore = OVER_MAXIMUM_SCORE_OF_100

        radarChartAvgScores.forEach {
            if (it.avgScore < lowestAvgScore && it.labelName != MIXED_QUIZ) {
                lowestAvgScore = it.avgScore
                courseNameToFind = it.labelName
            }
        }

        _courseNameNeedToImprove.postValue(courseNameToFind)
    }

    /**
     * Tính điểm trung bình
     *
     * @param scores danh sách điểm
     * @param labelName chức năng này tính toán điểm trung bình cho nhãn nào
     */
    fun calculateAverageScore(scores: List<Score>, labelName: String) {
        var scoreSum = 0f

        scores.forEach { score ->
            scoreSum += (score.totalCorrect.toFloat() / score.totalQuestions) * 10f
        }

        val avgScore = scoreSum / scores.size
        pieChartAvgScores.add(AvgScoreAndLabel(avgScore = avgScore, labelName = labelName))

        _pieChartData.postValue(pieChartAvgScores)
    }

    /**
     * Xóa tất cả dữ liệu biểu đồ hình tròn trước đó
     */
    fun clearPreviousScoreData() {
        pieChartAvgScores.clear()
    }

    /* -------------------------------------- DB related -------------------------------------- */
    fun getTopicsByCourse(courseId: Int): LiveData<List<Topic>> =
        repository.getTopicsBasedOnCourse(courseId = courseId)

    fun getUserScoresByTopic(topicId: Int): LiveData<List<Score>> =
        repository.getUserScoresByTopic(topicId = topicId)

    // TODO: test
    fun getUserScoresByCourse(courseId: Int): LiveData<List<Score>> =
        repository.getUserScoresByCourse(courseId = courseId)

    // TODO: test
    fun getUserScoresByMixedQuiz(): LiveData<List<Score>> =
        repository.getUserScoresByMixedQuiz()
}