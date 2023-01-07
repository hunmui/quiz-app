package self.eng.hocmaians.ui.fragments.quiz.result

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import self.eng.hocmaians.data.entities.Score
import self.eng.hocmaians.repositories.IRepository
import javax.inject.Inject

@HiltViewModel
class GraphViewModel @Inject constructor(
    private val repository: IRepository
) : ViewModel() {

    fun getUserScoresByTopic(topicId: Int): LiveData<List<Score>> =
        repository.getUserScoresByTopic(topicId = topicId)

    fun getCourseNameByTopicId(topicId: Int): LiveData<String> =
        repository.getCourseNameBasedOnTopicId(topicId = topicId)

    fun getTopicNameByItsId(topicId: Int): LiveData<String> =
        repository.getTopicNameBasedOnTopicId(topicId = topicId)
}