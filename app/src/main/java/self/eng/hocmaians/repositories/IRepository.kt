package self.eng.hocmaians.repositories

import androidx.lifecycle.LiveData
import self.eng.hocmaians.data.entities.*
import self.eng.hocmaians.data.entities.realtions.AnswerAndQuestion

interface IRepository {

    /* ------------------- Courses ------------------- */
    fun getAllCourses(): LiveData<List<Course>>
    fun getAllCourseNames(): LiveData<List<String>>
    fun getCourseNameBasedOnTopicId(topicId: Int): LiveData<String>
    suspend fun updateCoursePriority(courseId: Int, newCoursePriority: Long)

    /* ------------------- Topics ------------------- */
    fun getTopicsBasedOnCourse(courseId: Int): LiveData<List<Topic>>
    fun getTopicsNamesBasedOnCourse(courseId: Int): LiveData<List<String>>
    fun getTopicNameBasedOnTopicId(topicId: Int): LiveData<String>
    suspend fun insertTopic(topic: Topic)
    suspend fun updateTopicPriority(topicId: Int, newTopicPriority: Long)
    suspend fun updateTopicName(topicId: Int, topicName: String)
    suspend fun deleteTopic(topic: Topic)

    /* ------------------- Questions ------------------- */
    fun getQuestionsBasedOnTopic(topicId: Int): LiveData<List<Question>>
    fun getRandomQuestions(quantity: Int): LiveData<List<Question>>
    fun getUserAddedQuestionsByTopic(topicId: Int): LiveData<List<Question>>
    fun getAllBookmarks(): LiveData<List<Question>>
    fun getBookmarksBasedOnTopicId(topicId: Int): LiveData<List<Question>>
    fun countQuestionsBasedOnTopic(topicId: Int): LiveData<Int>
    fun countAllQuestions(): LiveData<Long>
    suspend fun insertQuestion(question: Question)
    suspend fun deleteQuestion(question: Question)
    suspend fun deleteQuestionsByTopic(topicId: Int)
    suspend fun updateQuestion(question: Question)
    suspend fun updateQuestionBookmark(questionId: Long, bookmark: Int)

    /* ------------------- User answers ------------------- */
    suspend fun insertUserAnswer(userAnswer: UserAnswer)
    fun getAllUserAnswers(): LiveData<List<UserAnswer>>
    fun getUserAnswersNoSorting(timestamp: Long): LiveData<List<AnswerAndQuestion>>
    fun getUserAnswersOrderByQuestionId(timestamp: Long): LiveData<List<AnswerAndQuestion>>
    suspend fun deleteUserAnswerBasedOnQuestion(questionId: Long)
    suspend fun deleteUserAnswersByTimeStamp(timestamp: Long)
    suspend fun deleteUserAnswersByTopic(topicId: Int)

    /* ------------------- User scores ------------------- */
    suspend fun insertUserScore(score: Score)
    fun getUserScore(timestamp: Long): LiveData<Score>
    fun getUserScoresByTopic(topicId: Int): LiveData<List<Score>>
    fun getAllUserScores(): LiveData<List<Score>>
    fun getUserScoresByCourse(courseId: Int): LiveData<List<Score>>
    fun getUserScoresByMixedQuiz(): LiveData<List<Score>>
    suspend fun deleteScoreByTimeStamp(timestamp: Long)
    suspend fun deleteScoresByTopic(topicId: Int)
}