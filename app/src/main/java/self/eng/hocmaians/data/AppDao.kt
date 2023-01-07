package self.eng.hocmaians.data

import androidx.lifecycle.LiveData
import androidx.room.*
import self.eng.hocmaians.data.entities.*
import self.eng.hocmaians.data.entities.realtions.AnswerAndQuestion
import self.eng.hocmaians.util.Constants.MIXED_TOPIC_ID
import self.eng.hocmaians.util.Constants.QUESTION_BOOKMARKED
import self.eng.hocmaians.util.Constants.USER_ADDED

@Dao
interface AppDao {

    /* ------------ Course related queries ------------ */

    @Query("SELECT * FROM courses ORDER BY priority DESC, id ASC")
    fun getAllCourses(): LiveData<List<Course>>

    @Query("SELECT name FROM courses")
    fun getAllCourseNames(): LiveData<List<String>>

    @Query("SELECT name FROM courses WHERE courses.id = (SELECT course_id FROM topics WHERE topics.id = :topicId)")
    fun getCourseNameBasedOnTopicId(topicId: Int): LiveData<String>

    @Query("UPDATE courses SET priority = :newCoursePriority WHERE id = :courseId")
    suspend fun updateCoursePriority(courseId: Int, newCoursePriority: Long)

    @Query("SELECT * FROM courses WHERE id = :courseId")
    fun getCourseById(courseId: Int): LiveData<Course>

    // for testing DAO
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCourses(vararg course: Course)

    /* ------------ Topic related queries ------------ */

    @Query("SELECT * FROM topics WHERE course_id = :courseId ORDER BY priority DESC, id ASC")
    fun getTopicsBasedOnCourse(courseId: Int): LiveData<List<Topic>>

    @Query("SELECT name FROM topics WHERE course_id = :courseId")
    fun getTopicsNamesBasedOnCourse(courseId: Int): LiveData<List<String>>

    @Query("SELECT name FROM topics WHERE id = :topicId")
    fun getTopicNameBasedOnTopicId(topicId: Int): LiveData<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: Topic)

    // for testing DAO
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(vararg topic: Topic)

    @Query("UPDATE topics SET priority = :newTopicPriority WHERE id = :topicId")
    suspend fun updateTopicPriority(topicId: Int, newTopicPriority: Long)

    @Query("UPDATE topics SET name = :topicName WHERE id = :topicId")
    suspend fun updateTopicName(topicId: Int, topicName: String)

    // for testing DAO
    @Query("SELECT * FROM topics WHERE id = :topicId")
    fun getTopicById(topicId: Int): LiveData<Topic>

    @Delete
    suspend fun deleteTopic(topic: Topic)

    /* ------------ Question related queries ------------ */

    @Query("SELECT * FROM questions WHERE topic_id = :topicId ORDER BY id ASC")
    fun getQuestionsBasedOnTopic(topicId: Int): LiveData<List<Question>>

    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT :quantity")
    fun getRandomQuestions(quantity: Int): LiveData<List<Question>>

    @Query("SELECT * FROM questions WHERE topic_id = :topicId AND is_user_added = :isUserAdded ORDER BY id ASC")
    fun getUserAddedQuestionsByTopic(
        topicId: Int,
        isUserAdded: Int = USER_ADDED
    ): LiveData<List<Question>>

    @Query("SELECT * FROM questions WHERE is_bookmark = :isBookmark ORDER BY id ASC")
    fun getAllBookmarks(isBookmark: Int = QUESTION_BOOKMARKED): LiveData<List<Question>>

    @Query("SELECT * FROM questions WHERE is_bookmark = :isBookmark AND topic_id = :topicId ORDER BY id ASC")
    fun getBookmarksBasedOnTopicId(
        isBookmark: Int = QUESTION_BOOKMARKED,
        topicId: Int
    ): LiveData<List<Question>>

    @Query("SELECT COUNT(*) FROM questions WHERE topic_id = :topicId")
    fun countQuestionsBasedOnTopic(topicId: Int): LiveData<Int>

    @Query("SELECT COUNT(*) FROM questions")
    fun countAllQuestions(): LiveData<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question)

    // for testing DAO
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(vararg question: Question)

    @Update
    suspend fun updateQuestion(question: Question)

    @Delete
    suspend fun deleteQuestion(question: Question)

    @Query("DELETE FROM questions WHERE topic_id = :topicId")
    suspend fun deleteQuestionsByTopic(topicId: Int)

    @Query("UPDATE questions SET is_bookmark = :bookmark WHERE id = :questionId")
    suspend fun updateQuestionBookmark(questionId: Long, bookmark: Int)

    // for testing DAO
    @Query("SELECT * FROM questions WHERE id = :questionId")
    fun getQuestionById(questionId: Long): LiveData<Question>

    /* ------------ User Answer related queries ------------ */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAnswer(userAnswer: UserAnswer)

    // for testing DAO
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAnswers(vararg userAnswer: UserAnswer)

    @Transaction
    @Query("SELECT * FROM user_answers AS ua INNER JOIN questions ON ua.question_id = questions.id WHERE ua.timestamp = :timestamp ORDER BY ua.ua_id ASC")
    fun getUserAnswersNoSorting(timestamp: Long): LiveData<List<AnswerAndQuestion>>

    @Transaction
    @Query("SELECT * FROM user_answers AS ua INNER JOIN questions ON ua.question_id = questions.id WHERE ua.timestamp = :timestamp ORDER BY ua.question_id ASC")
    fun getUserAnswersOrderByQuestionId(timestamp: Long): LiveData<List<AnswerAndQuestion>>

    @Query("DELETE FROM user_answers WHERE question_id =:questionId")
    suspend fun deleteUserAnswerBasedOnQuestion(questionId: Long)

    @Query("DELETE FROM user_answers WHERE timestamp = :timestamp")
    suspend fun deleteUserAnswersByTimeStamp(timestamp: Long)

    @Query("DELETE FROM user_answers WHERE question_id IN (SELECT q.id FROM questions AS q WHERE q.topic_id = :topicId)")
    suspend fun deleteUserAnswersByTopic(topicId: Int)

    @Query("SELECT * FROM user_answers WHERE timestamp = :timestamp")
    fun getUserAnswersByTimeStamp(timestamp: Long): LiveData<List<UserAnswer>>

    // testing purpose
    @Query("SELECT * FROM user_answers")
    fun getAllUserAnswers(): LiveData<List<UserAnswer>>

    /* ------------ Score related queries ------------ */

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserScore(score: Score)

    // testing purpose
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserScores(vararg userScore: Score)

    @Transaction
    @Query("SELECT * FROM scores WHERE timestamp = :timestamp")
    fun getUserScore(timestamp: Long): LiveData<Score>

    @Query("SELECT * FROM scores WHERE topic_id = :topicId ORDER BY timestamp ASC")
    fun getUserScoresByTopic(topicId: Int): LiveData<List<Score>>

    @Query("SELECT * FROM scores ORDER BY timestamp ASC")
    fun getAllUserScores(): LiveData<List<Score>>

    @Query("SELECT s.* FROM scores AS s WHERE s.topic_id IN (SELECT t.id FROM topics AS t WHERE t.course_id = :courseId) ORDER BY s.timestamp ASC")
    fun getUserScoresByCourse(courseId: Int): LiveData<List<Score>>

    @Query("SELECT * FROM scores WHERE topic_id = :topicId ORDER BY timestamp ASC")
    fun getUserScoresByMixedQuiz(topicId: Int = MIXED_TOPIC_ID): LiveData<List<Score>>

    @Query("DELETE FROM scores WHERE timestamp = :timestamp")
    suspend fun deleteScoreByTimeStamp(timestamp: Long)

    // test
    @Query("DELETE FROM scores WHERE topic_id = :topicId")
    suspend fun deleteScoresByTopic(topicId: Int)
}