package self.eng.hocmaians.repositories

import androidx.lifecycle.LiveData
import self.eng.hocmaians.data.AppDao
import self.eng.hocmaians.data.entities.*
import self.eng.hocmaians.data.entities.realtions.AnswerAndQuestion
import javax.inject.Inject

class AppRepository @Inject constructor(
    private val appDao: AppDao
) : IRepository {
    /* ------------------- Courses ------------------- */
    override fun getAllCourses(): LiveData<List<Course>> = appDao.getAllCourses()

    override fun getAllCourseNames(): LiveData<List<String>> = appDao.getAllCourseNames()

    override fun getCourseNameBasedOnTopicId(topicId: Int): LiveData<String> =
        appDao.getCourseNameBasedOnTopicId(topicId = topicId)

    override suspend fun updateCoursePriority(courseId: Int, newCoursePriority: Long) =
        appDao.updateCoursePriority(courseId = courseId, newCoursePriority = newCoursePriority)

    /* ------------------- Topics ------------------- */
    override fun getTopicsBasedOnCourse(courseId: Int): LiveData<List<Topic>> =
        appDao.getTopicsBasedOnCourse(courseId = courseId)

    override fun getTopicsNamesBasedOnCourse(courseId: Int): LiveData<List<String>> =
        appDao.getTopicsNamesBasedOnCourse(courseId = courseId)

    override fun getTopicNameBasedOnTopicId(topicId: Int): LiveData<String> =
        appDao.getTopicNameBasedOnTopicId(topicId = topicId)

    override suspend fun insertTopic(topic: Topic) = appDao.insertTopic(topic = topic)

    override suspend fun updateTopicPriority(topicId: Int, newTopicPriority: Long) =
        appDao.updateTopicPriority(topicId = topicId, newTopicPriority = newTopicPriority)

    override suspend fun updateTopicName(topicId: Int, topicName: String) =
        appDao.updateTopicName(topicId = topicId, topicName = topicName)

    override suspend fun deleteTopic(topic: Topic) = appDao.deleteTopic(topic = topic)

    /* ------------------- Questions ------------------- */
    override fun getQuestionsBasedOnTopic(topicId: Int): LiveData<List<Question>> =
        appDao.getQuestionsBasedOnTopic(topicId = topicId)

    override fun getRandomQuestions(quantity: Int): LiveData<List<Question>> =
        appDao.getRandomQuestions(quantity = quantity)

    override fun getUserAddedQuestionsByTopic(topicId: Int): LiveData<List<Question>> =
        appDao.getUserAddedQuestionsByTopic(topicId = topicId)

    override fun getAllBookmarks(): LiveData<List<Question>> = appDao.getAllBookmarks()

    override fun getBookmarksBasedOnTopicId(topicId: Int): LiveData<List<Question>> =
        appDao.getBookmarksBasedOnTopicId(topicId = topicId)

    override fun countQuestionsBasedOnTopic(topicId: Int): LiveData<Int> =
        appDao.countQuestionsBasedOnTopic(topicId = topicId)

    override fun countAllQuestions(): LiveData<Long> = appDao.countAllQuestions()

    override suspend fun insertQuestion(question: Question) =
        appDao.insertQuestion(question = question)

    override suspend fun deleteQuestion(question: Question) =
        appDao.deleteQuestion(question = question)

    override suspend fun deleteQuestionsByTopic(topicId: Int) =
        appDao.deleteQuestionsByTopic(topicId = topicId)

    override suspend fun updateQuestion(question: Question) =
        appDao.updateQuestion(question = question)

    override suspend fun updateQuestionBookmark(questionId: Long, bookmark: Int) =
        appDao.updateQuestionBookmark(questionId = questionId, bookmark = bookmark)

    /* ------------------- User answers ------------------- */
    override suspend fun insertUserAnswer(userAnswer: UserAnswer) =
        appDao.insertUserAnswer(userAnswer = userAnswer)

    override fun getUserAnswersNoSorting(
        timestamp: Long
    ): LiveData<List<AnswerAndQuestion>> =
        appDao.getUserAnswersNoSorting(timestamp = timestamp)

    override fun getUserAnswersOrderByQuestionId(
        timestamp: Long
    ): LiveData<List<AnswerAndQuestion>> =
        appDao.getUserAnswersOrderByQuestionId(timestamp = timestamp)

    override suspend fun deleteUserAnswerBasedOnQuestion(questionId: Long) =
        appDao.deleteUserAnswerBasedOnQuestion(questionId = questionId)

    override suspend fun deleteUserAnswersByTimeStamp(timestamp: Long) =
        appDao.deleteUserAnswersByTimeStamp(timestamp = timestamp)

    override suspend fun deleteUserAnswersByTopic(topicId: Int) =
        appDao.deleteUserAnswersByTopic(topicId = topicId)

    override fun getAllUserAnswers(): LiveData<List<UserAnswer>> = appDao.getAllUserAnswers()

    /* ------------------- User scores ------------------- */
    override suspend fun insertUserScore(score: Score) = appDao.insertUserScore(score = score)

    override fun getUserScore(timestamp: Long): LiveData<Score> =
        appDao.getUserScore(timestamp = timestamp)

    override fun getUserScoresByTopic(topicId: Int): LiveData<List<Score>> =
        appDao.getUserScoresByTopic(topicId = topicId)

    override fun getAllUserScores(): LiveData<List<Score>> = appDao.getAllUserScores()

    override fun getUserScoresByCourse(courseId: Int): LiveData<List<Score>> =
        appDao.getUserScoresByCourse(courseId = courseId)

    override fun getUserScoresByMixedQuiz(): LiveData<List<Score>> =
        appDao.getUserScoresByMixedQuiz()

    override suspend fun deleteScoreByTimeStamp(timestamp: Long) =
        appDao.deleteScoreByTimeStamp(timestamp = timestamp)

    override suspend fun deleteScoresByTopic(topicId: Int) =
        appDao.deleteScoresByTopic(topicId = topicId)
}