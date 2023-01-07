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

    // live data for OverallFragment to observe to load data for its radar chart
    private val radarChartAvgScores: MutableList<AvgScoreAndLabel> = mutableListOf()
    private var _radarChartData = MutableLiveData<List<AvgScoreAndLabel>>()
    val radarChartData: LiveData<List<AvgScoreAndLabel>> = _radarChartData

    // course that user need to improve on (radar chart)
    private var _courseNameNeedToImprove = MutableLiveData("")
    val courseNameNeedToImprove: LiveData<String> = _courseNameNeedToImprove

    // live data for OverallFragment to observe to load data for its pie chart
    private val pieChartAvgScores: MutableList<AvgScoreAndLabel> = mutableListOf()
    private var _pieChartData = MutableLiveData<List<AvgScoreAndLabel>>()
    val pieChartData: LiveData<List<AvgScoreAndLabel>> = _pieChartData

    // for pie chart, has to ensure the data quantity is reached before set the data for pie chart
    var ensureDataQuantity: Int = 0

    // holder chosen position for pop up MaterialAlertDialog
    var chosenDialogIndex = FILTER_BY_OVERALL

    /**
     * Calculate average score in each course. The average score is presented in percentage form
     * (9.3 will be 93%). Then let _avgScores post the latest value.
     *
     * @param scores a list of score in the corresponding course
     * @param labelName course name to load the radar chart label
     */
    fun calAvgScoreInPercentage(scores: List<Score>, labelName: String) {
        var scoreSum = 0f

        scores.forEach { score ->
            scoreSum += (score.totalCorrect.toFloat() / score.totalQuestions) * 10f
        }

        // avg score in each course in percentage
        val avgScore = (scoreSum / scores.size) * 10f
        radarChartAvgScores.add(AvgScoreAndLabel(avgScore = avgScore, labelName = labelName))

        _radarChartData.postValue(radarChartAvgScores)
    }

    /**
     * Get the course name that has the lowest average score (except for Mixed Quiz)
     */
    fun getCourseThatHasTheLowestAvgScore() {
        var courseNameToFind = ""

        // because average score here is in percentage so max score of 10 is now 100
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
     * Calculate average score
     *
     * @param scores a list of scores
     * @param labelName which label is this function calculate avg score for
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
     * Clear all previous pie chart data
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