package self.eng.hocmaians.ui.fragments.manage.topics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import self.eng.hocmaians.data.entities.Topic
import self.eng.hocmaians.repositories.IRepository
import self.eng.hocmaians.util.Constants.ACTION_ADD_TOPIC
import self.eng.hocmaians.util.Constants.ACTION_EDIT_TOPIC
import self.eng.hocmaians.util.Constants.DEFAULT_PRIORITY
import self.eng.hocmaians.util.Constants.MAX_TOPIC_NAME_LENGTH
import self.eng.hocmaians.util.Constants.USER_ADDED
import self.eng.hocmaians.util.Event
import self.eng.hocmaians.util.Resource
import javax.inject.Inject

@HiltViewModel
class ManageTopicsViewModel @Inject constructor(
    private val repository: IRepository
) : ViewModel() {

    // methods get live data from db for manage topics related fragments to observe
    fun getTopicsByCourse(courseId: Int): LiveData<List<Topic>> =
        repository.getTopicsBasedOnCourse(courseId = courseId)

    fun getTopicsNamesByCourse(courseId: Int): LiveData<List<String>> =
        repository.getTopicsNamesBasedOnCourse(courseId = courseId)

    // insert, update, delete topic for manage topics related fragments to observe
    private val _insertTopicStatus = MutableLiveData<Event<Resource<Topic>>>()
    val insertTopicStatus: LiveData<Event<Resource<Topic>>> = _insertTopicStatus

    private val _updateTopicStatus = MutableLiveData<Event<Resource<Topic>>>()
    val updateTopicStatus: LiveData<Event<Resource<Topic>>> = _updateTopicStatus

    private val _deleteTopicStatus = MutableLiveData<Event<Resource<String>>>()
    val deleteTopicStatus: LiveData<Event<Resource<String>>> = _deleteTopicStatus

    /**
     * Post value when there's an error when adding OR updating a topic
     *
     * @param action is the action Add topic, or Edit topic
     * @param errorMessage the error message to post
     */
    private fun insertUpdateTopicError(action: String, errorMessage: String) {
        if (action == ACTION_ADD_TOPIC) {
            _insertTopicStatus.postValue(
                Event(
                    Resource.error(
                        msg = errorMessage,
                        data = null
                    )
                )
            )
        } else {
            _updateTopicStatus.postValue(
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
     * Insert topic
     *
     * @param topicName topic name
     * @param existingTopicNames a list of already existed topic names in the db
     * @param courseId which course does this topic belong to
     */
    fun insertTopic(topicName: String, existingTopicNames: List<String>, courseId: Int) {
        if (topicName.isBlank()) {
            insertUpdateTopicError(
                action = ACTION_ADD_TOPIC,
                errorMessage = "Topic name must not be blank"
            )
            return
        }
        if (topicName.trim().length > MAX_TOPIC_NAME_LENGTH) {
            val errorMessage = "Topic name is too long, maximum is: " +
                    "$MAX_TOPIC_NAME_LENGTH characters"
            insertUpdateTopicError(
                action = ACTION_ADD_TOPIC,
                errorMessage = errorMessage
            )
            return
        }
        if (topicName.lowercase().trim() in existingTopicNames) {
            insertUpdateTopicError(
                action = ACTION_ADD_TOPIC,
                errorMessage = "Topic\'s name is duplicated, please choose another topic name!"
            )
            return
        }

//        // admin add
//        val topic = Topic(
//            name = topicName.trim().lowercase(),
//            priority = DEFAULT_PRIORITY,
//            isUserAdded = ADMIN_ADDED,
//            courseId = courseId
//        )

        // production code
        val topic = Topic(
            name = topicName.trim().lowercase(),
            priority = DEFAULT_PRIORITY,
            isUserAdded = USER_ADDED,
            courseId = courseId
        )
        insertTopicIntoDb(topic = topic)
        _insertTopicStatus.postValue(Event(Resource.success(data = topic)))
    }

    /**
     * Update topic
     *
     * @param topicId which topic to be updated
     * @param topicName topic name to update
     * @param existingTopicNames a list of already existed topic names to avoid duplicate
     */
    fun updateTopic(topicId: Int, topicName: String, existingTopicNames: List<String>) {
        if (topicName.isBlank()) {
            insertUpdateTopicError(
                action = ACTION_EDIT_TOPIC,
                errorMessage = "Topic name must not be blank"
            )
            return
        }
        if (topicName.trim().length > MAX_TOPIC_NAME_LENGTH) {
            val errorMessage = "Topic name is too long, maximum is: " +
                    "$MAX_TOPIC_NAME_LENGTH characters"
            insertUpdateTopicError(
                action = ACTION_EDIT_TOPIC,
                errorMessage = errorMessage
            )
            return
        }
        if (topicName.lowercase().trim() in existingTopicNames) {
            insertUpdateTopicError(
                action = ACTION_EDIT_TOPIC,
                errorMessage = "Topic\' name is duplicated, please choose another topic name!"
            )
            return
        }

        updateTopicName(
            topicId = topicId,
            topicName = topicName.trim().lowercase()
        )
        _updateTopicStatus.postValue(Event(Resource.success(data = null)))
    }

    /**
     * Delete topic and its associated stuff (questions, answers and scores). The post the result
     * so that AddEditTopicFragment can observe
     *
     * @param topic the topic to delete
     */
    fun deleteTopic(topic: Topic) {
        viewModelScope.launch {
            val job: Job = viewModelScope.launch {
                repository.deleteTopic(topic = topic)
                repository.deleteQuestionsByTopic(topicId = topic.id)
                repository.deleteUserAnswersByTopic(topicId = topic.id)
                repository.deleteScoresByTopic(topicId = topic.id)
            }

            job.join()

            _deleteTopicStatus.postValue(
                Event(
                    Resource.success("Delete topic successfully")
                )
            )
        }
    }

    private fun insertTopicIntoDb(topic: Topic) = viewModelScope.launch {
        repository.insertTopic(topic = topic)
    }

    private fun updateTopicName(topicId: Int, topicName: String) = viewModelScope.launch {
        repository.updateTopicName(
            topicId = topicId,
            topicName = topicName
        )
    }
}