package self.eng.hocmaians.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import self.eng.hocmaians.data.entities.*
import self.eng.hocmaians.data.entities.realtions.AnswerAndQuestion
import self.eng.hocmaians.getOrAwaitValue
import self.eng.hocmaians.util.Constants.ADMIN_ADDED
import self.eng.hocmaians.util.Constants.MIXED_TOPIC_ID
import self.eng.hocmaians.util.Constants.QUESTION_BOOKMARKED
import self.eng.hocmaians.util.Constants.QUESTION_NOT_BOOKMARKED
import self.eng.hocmaians.util.Constants.TEST_DB
import self.eng.hocmaians.util.Constants.USER_ADDED
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
@SmallTest
@HiltAndroidTest
class AppDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    @Named(TEST_DB)
    lateinit var database: AppDatabase
    private lateinit var dao: AppDao

    @Before
    fun setup() {
        hiltRule.inject()
        dao = database.dao
    }

    @After
    fun teardown() {
        database.close()
    }

    /* ------------------------------ Test Course related queries ------------------------------ */
    @Test
    fun getAllCourses() = runBlockingTest {
        val course1 = Course(id = 1, name = "course 1", priority = 9L)
        val course2 = Course(id = 2, name = "course 2", priority = 0L)

        dao.insertCourses(course1, course2)

        val courses: List<Course> = dao.getAllCourses().getOrAwaitValue()

        assertThat(courses).containsExactly(course2, course1)
    }

    @Test
    fun getAllCourseNames() = runBlockingTest {
        val courseName1 = "course number 1"
        val courseName2 = "course 2"

        val course1 = Course(name = courseName1, priority = 5L)
        val course2 = Course(name = courseName2, priority = 9L)

        dao.insertCourses(course1, course2)

        val courseNames: List<String> = dao.getAllCourseNames().getOrAwaitValue()

        assertThat(courseNames).containsExactly(courseName1, courseName2)
    }

    @Test
    fun getCourseNameBasedOnTopicId() = runBlockingTest {
        val expectedCourseId = 1
        val expectedCourseName = "second course"
        val course1 = Course(
            id = expectedCourseId,
            name = expectedCourseName,
            priority = 3L,
        )
        val course2 = Course(id = 3, name = "first course", priority = 10L)
        val topic = Topic(
            id = 9, name = "topic 1", priority = 3L,
            isUserAdded = 1, courseId = expectedCourseId
        )

        dao.insertCourses(course1, course2)
        dao.insertTopic(topic = topic)

        val courseName: String =
            dao.getCourseNameBasedOnTopicId(topicId = topic.id).getOrAwaitValue()

        assertThat(courseName).isEqualTo(expectedCourseName)
    }

    @Test
    fun updateCoursePriority() = runBlockingTest {
        val newCoursePriority = 4L
        val course = Course(id = 2, name = "second course", priority = 3L)

        dao.insertCourses(course)
        dao.updateCoursePriority(courseId = course.id, newCoursePriority = newCoursePriority)

        val chosenCourse: Course = dao.getCourseById(courseId = course.id).getOrAwaitValue()

        assertThat(chosenCourse.priority).isEqualTo(newCoursePriority)
    }

    @Test
    fun getCourseById() = runBlockingTest {
        val course1 = Course(id = 1, name = "first course", priority = 9L)
        val expectedCourse = Course(id = 2, name = "some course", priority = 12L)
        val course3 = Course(id = 3, name = "third course", priority = 100L)

        dao.insertCourses(course1, expectedCourse, course3)

        val chosenCourse: Course = dao.getCourseById(courseId = expectedCourse.id).getOrAwaitValue()

        assertThat(chosenCourse).isEqualTo(expectedCourse)
    }

    /* ------------------------------ Test Topic related queries ------------------------------ */
    @Test
    fun getTopicsBasedOnCourse() = runBlockingTest {
        val chosenCourseId = 8
        val course = Course(id = chosenCourseId, name = "course", priority = 8L)
        val topic1 = Topic(
            id = 1, name = "topic 1", priority = 3L,
            isUserAdded = 0, courseId = chosenCourseId
        )
        val topic2 = Topic(
            id = 2, name = "topic 2", priority = 8L,
            isUserAdded = 0, courseId = chosenCourseId
        )

        dao.insertCourses(course)
        dao.insertTopics(topic1, topic2)

        val topics: List<Topic> =
            dao.getTopicsBasedOnCourse(courseId = chosenCourseId).getOrAwaitValue()

        assertThat(topics).containsExactly(topic2, topic1)
    }

    @Test
    fun getTopicsNamesBasedOnCourse() = runBlockingTest {
        val chosenCourseId = 19
        val course = Course(id = chosenCourseId, name = "course 1", priority = 8L)
        val topic1 = Topic(
            id = 7, name = "first topic", priority = 9L,
            isUserAdded = 1, courseId = chosenCourseId
        )
        val topic2 = Topic(
            id = 2, name = "second topic", priority = 19L,
            isUserAdded = 0, courseId = chosenCourseId
        )
        val topic3 =
            Topic(id = 1, name = "some topic", priority = 19L, isUserAdded = 0, courseId = 4)

        dao.insertCourses(course)
        dao.insertTopics(topic1, topic2, topic3)

        val topicNames: List<String> =
            dao.getTopicsNamesBasedOnCourse(courseId = chosenCourseId).getOrAwaitValue()

        assertThat(topicNames).containsExactly(topic1.name, topic2.name)
    }

    @Test
    fun getTopicNameBasedOnTopicId() = runBlockingTest {
        val expectedTopicName = "expected topic"
        val topic2 =
            Topic(id = 2, name = expectedTopicName, priority = 19L, isUserAdded = 0, courseId = 1)

        dao.insertTopic(topic = topic2)

        val topicName: String =
            dao.getTopicNameBasedOnTopicId(topicId = topic2.id).getOrAwaitValue()

        assertThat(topicName).isEqualTo(expectedTopicName)
    }

    @Test
    fun isInsertTopicReplaceable() = runBlockingTest {
        val topic1 = Topic(id = 4, name = "topic 1", priority = 3L, isUserAdded = 1, courseId = 2)
        val expectedTopic =
            Topic(id = 4, name = "topic 1", priority = 9L, isUserAdded = 0, courseId = 2)

        dao.insertTopic(topic = topic1)
        dao.insertTopic(topic = expectedTopic)

        val topics: List<Topic> = dao.getTopicsBasedOnCourse(courseId = 2).getOrAwaitValue()

        assertThat(topics).containsExactly(expectedTopic)
    }

    @Test
    fun updateTopicPriority() = runBlockingTest {
        val newTopicPriority = 20L
        val topic = Topic(id = 2, name = "some", priority = 19L, isUserAdded = 0, courseId = 1)

        dao.insertTopic(topic)
        dao.updateTopicPriority(topicId = topic.id, newTopicPriority = newTopicPriority)

        val chosenTopic: Topic = dao.getTopicById(topic.id).getOrAwaitValue()

        assertThat(chosenTopic.priority).isEqualTo(newTopicPriority)
    }

    @Test
    fun updateTopicName() = runBlockingTest {
        val newTopicName = "this is a new topic name"
        val topic = Topic(id = 19, name = "topic", priority = 17L, isUserAdded = 1, courseId = 9)

        dao.insertTopic(topic)
        dao.updateTopicName(topicId = topic.id, topicName = newTopicName)

        val chosenTopic: Topic = dao.getTopicById(topic.id).getOrAwaitValue()

        assertThat(chosenTopic.name).isEqualTo(newTopicName)
    }

    @Test
    fun getTopicById() = runBlockingTest {
        val topic1 = Topic(id = 1, name = "t", priority = 1L, isUserAdded = 1, courseId = 7)
        val topic2 = Topic(id = 2, name = "to", priority = 11L, isUserAdded = 0, courseId = 9)
        val topic3 = Topic(id = 3, name = "top", priority = 14L, isUserAdded = 1, courseId = 3)

        dao.insertTopics(topic1, topic2, topic3)

        val expectedTopic: Topic = dao.getTopicById(topic1.id).getOrAwaitValue()

        assertThat(expectedTopic).isEqualTo(topic1)
    }

    @Test
    fun deleteTopic() = runBlockingTest {
        val topicId = 1
        val topic = Topic(id = topicId, name = "t", priority = 1L, isUserAdded = 1, courseId = 7)

        dao.insertTopic(topic = topic)
        dao.deleteTopic(topic = topic)

        val topics: List<Topic> = dao.getTopicsBasedOnCourse(
            courseId = 7
        ).getOrAwaitValue()

        assertThat(topics).isEmpty()
    }

    /* ----------------------------- Test Question related queries ----------------------------- */
    @Test
    fun getQuestionsBasedOnTopic() = runBlockingTest {
        val chosenTopicId = 1
        val topic = Topic(
            id = chosenTopicId, name = "topic1",
            priority = 1L, isUserAdded = 0, courseId = 1
        )

        val ques1 = Question(
            id = 1, question = "question1",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 1, explanation = null, isBookmark = 0,
            isUserAdded = 0, topicId = chosenTopicId
        )
        val ques2 = Question(
            id = 2, question = "question2",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 2, explanation = null, isBookmark = 1,
            isUserAdded = 0, topicId = chosenTopicId
        )

        dao.insertTopic(topic = topic)
        dao.insertQuestions(ques1, ques2)

        val questions: List<Question> =
            dao.getQuestionsBasedOnTopic(topicId = topic.id).getOrAwaitValue()

        assertThat(questions).containsExactly(ques1, ques2)
    }

    @Test
    fun getRandomQuestions() = runBlockingTest {
        val ques1 = Question(
            id = 1, question = "question1",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 1, explanation = null, isBookmark = 0,
            isUserAdded = 0, topicId = 3
        )
        val ques2 = Question(
            id = 2, question = "question2",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 2, explanation = null, isBookmark = 1,
            isUserAdded = 0, topicId = 2
        )
        val ques3 = Question(
            id = 3, question = "question3",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 2, explanation = null, isBookmark = 1,
            isUserAdded = 0, topicId = 15
        )

        dao.insertQuestions(ques1, ques2, ques3)

        val questions: List<Question> = dao.getRandomQuestions(
            quantity = 2
        ).getOrAwaitValue()

        assertThat(questions).containsAnyIn(listOf(ques1, ques2, ques3))
        assertThat(questions.size).isEqualTo(2)
    }

    @Test
    fun getUserAddedQuestionsByTopic() = runBlockingTest {
        val topicId = 5
        val q1 = Question(
            id = 1, question = "question1",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 1, explanation = null, isBookmark = 1,
            isUserAdded = USER_ADDED, topicId = topicId
        )
        val q2 = Question(
            id = 5, question = "question5",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 3, explanation = null, isBookmark = 1,
            isUserAdded = ADMIN_ADDED, topicId = topicId
        )
        val q3 = Question(
            id = 100, question = "question100",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 2, explanation = null, isBookmark = 0,
            isUserAdded = USER_ADDED, topicId = topicId
        )

        dao.insertQuestions(q1, q2, q3)

        val questions: List<Question> = dao.getUserAddedQuestionsByTopic(
            topicId = topicId
        ).getOrAwaitValue()

        assertThat(questions).containsExactly(q1, q3)
    }

    @Test
    fun getAllBookmarks() = runBlockingTest {
        val ques1 = Question(
            id = 1, question = "question1",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 1, explanation = null, isBookmark = QUESTION_BOOKMARKED,
            isUserAdded = 0, topicId = 1
        )
        val ques2 = Question(
            id = 2, question = "question2",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 2, explanation = null, isBookmark = QUESTION_NOT_BOOKMARKED,
            isUserAdded = 1, topicId = 9
        )
        val ques3 = Question(
            id = 3, question = "question3",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 4, explanation = null, isBookmark = QUESTION_BOOKMARKED,
            isUserAdded = 0, topicId = 2
        )

        dao.insertQuestions(ques1, ques2, ques3)

        val bookmarks: List<Question> = dao.getAllBookmarks().getOrAwaitValue()

        assertThat(bookmarks).containsExactly(ques1, ques3)
    }

    @Test
    fun getBookmarksBasedOnTopicId() = runBlockingTest {
        val topicId = 2
        val topic = Topic(id = topicId, name = "t1", priority = 1L, isUserAdded = 0, courseId = 1)
        val ques1 = Question(
            id = 1, question = "question1",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 4, explanation = null, isBookmark = QUESTION_BOOKMARKED,
            isUserAdded = 0, topicId = topicId
        )
        val ques2 = Question(
            id = 2, question = "question2",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 3, explanation = null, isBookmark = QUESTION_BOOKMARKED,
            isUserAdded = 1, topicId = topicId
        )
        val ques3 = Question(
            id = 3, question = "question3",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 1, explanation = null, isBookmark = QUESTION_NOT_BOOKMARKED,
            isUserAdded = 1, topicId = topicId
        )

        dao.insertTopic(topic)
        dao.insertQuestions(ques1, ques2, ques3)

        val bookmarks: List<Question> =
            dao.getBookmarksBasedOnTopicId(topicId = topicId).getOrAwaitValue()

        assertThat(bookmarks).containsExactly(ques1, ques2)
    }

    @Test
    fun countQuestionsBasedOnTopic() = runBlockingTest {
        val expectedTotalQuestions = 3
        val topicId = 3
        val ques1 = Question(
            id = 7, question = "question7",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 2, explanation = null, isBookmark = 1,
            isUserAdded = 1, topicId = topicId
        )
        val ques2 = Question(
            id = 9, question = "question",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 1, explanation = null, isBookmark = 1,
            isUserAdded = 0, topicId = topicId
        )
        val ques3 = Question(
            id = 2, question = "question this",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 2, explanation = null, isBookmark = 0,
            isUserAdded = 0, topicId = topicId
        )
        val ques4 = Question(
            id = 19, question = "some question",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 2, explanation = null, isBookmark = 0,
            isUserAdded = 0, topicId = 9
        )

        dao.insertQuestions(ques1, ques2, ques3, ques4)

        val totalQuestions: Int =
            dao.countQuestionsBasedOnTopic(topicId = topicId).getOrAwaitValue()

        assertThat(totalQuestions).isEqualTo(expectedTotalQuestions)
    }

    @Test
    fun countAllQuestions() = runBlockingTest {
        val expectedTotalQuestions = 4
        val ques1 = Question(
            id = 1, question = "question1",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 2, explanation = null, isBookmark = 1,
            isUserAdded = 1, topicId = 2
        )
        val ques2 = Question(
            id = 2, question = "question hard",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 1, explanation = null, isBookmark = 1,
            isUserAdded = 0, topicId = 1
        )
        val ques3 = Question(
            id = 4, question = "question normal",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 2, explanation = null, isBookmark = 0,
            isUserAdded = 0, topicId = 7
        )
        val ques4 = Question(
            id = 9, question = "some q",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 2, explanation = null, isBookmark = 0,
            isUserAdded = 0, topicId = 9
        )

        dao.insertQuestions(ques1, ques2, ques3, ques4)

        val totalQuestions: Long = dao.countAllQuestions().getOrAwaitValue()

        assertThat(totalQuestions).isEqualTo(expectedTotalQuestions)
    }

    @Test
    fun isInsertQuestionReplaceable() = runBlockingTest {
        val topicId = 9
        val ques1 = Question(
            id = 1, question = "question1",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 2, explanation = null, isBookmark = 1,
            isUserAdded = 1, topicId = topicId
        )
        val anotherQuestion = Question(
            id = 1, question = "9 is my lucky number",
            option1 = "b", option2 = "a", option3 = "d", option4 = "c",
            answerNr = 1, explanation = "some", isBookmark = 0,
            isUserAdded = 0, topicId = topicId
        )

        dao.insertQuestion(ques1)
        dao.insertQuestion(anotherQuestion)

        val questions: List<Question> =
            dao.getQuestionsBasedOnTopic(topicId = topicId).getOrAwaitValue()

        assertThat(questions).containsExactly(anotherQuestion)
    }

    @Test
    fun updateQuestion() = runBlockingTest {
        val updatedQuestion = "This is a simple question"
        val updatedOption1 = "This is the correct answer"
        val updatedAnswerNr = 1

        val ques1 = Question(
            id = 8, question = "question1",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 3, explanation = null, isBookmark = 1,
            isUserAdded = 1, topicId = 4
        )

        dao.insertQuestion(ques1)

        ques1.question = updatedQuestion
        ques1.option1 = updatedOption1
        ques1.answerNr = updatedAnswerNr

        dao.updateQuestion(ques1)

        val question: Question = dao.getQuestionById(questionId = ques1.id).getOrAwaitValue()

        assertThat(question.question).isEqualTo(updatedQuestion)
        assertThat(question.option1).isEqualTo(updatedOption1)
        assertThat(question.answerNr).isEqualTo(updatedAnswerNr)
    }

    @Test
    fun deleteQuestion() = runBlockingTest {
        val topicId = 4
        val ques = Question(
            id = 1, question = "1", option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 1, explanation = "ad", isBookmark = 1, isUserAdded = 1, topicId = topicId
        )

        dao.insertQuestion(ques)
        dao.deleteQuestion(ques)

        val questions: List<Question> =
            dao.getQuestionsBasedOnTopic(topicId = topicId).getOrAwaitValue()

        assertThat(questions).doesNotContain(ques)
    }

    @Test
    fun deleteQuestionsByTopic() = runBlockingTest {
        val topicId = 4
        val q1 = Question(
            id = 1, question = "1", option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 1, explanation = "ad", isBookmark = 1, isUserAdded = 1, topicId = topicId
        )
        val q2 = Question(
            id = 9, question = "1", option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 1, explanation = "ad", isBookmark = 1, isUserAdded = 1, topicId = topicId
        )
        dao.insertQuestions(q1, q2)
        dao.deleteQuestionsByTopic(topicId = topicId)

        val questions: List<Question> = dao.getQuestionsBasedOnTopic(
            topicId = topicId
        ).getOrAwaitValue()

        assertThat(questions).isEmpty()
    }

    @Test
    fun updateQuestionBookmark() = runBlockingTest {
        val ques1 = Question(
            id = 1, question = "question",
            option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 1, explanation = null, isBookmark = QUESTION_NOT_BOOKMARKED,
            isUserAdded = 0, topicId = 4
        )

        dao.insertQuestion(ques1)
        dao.updateQuestionBookmark(questionId = ques1.id, bookmark = QUESTION_BOOKMARKED)

        val question: Question = dao.getQuestionById(questionId = ques1.id).getOrAwaitValue()

        assertThat(question.isBookmark).isEqualTo(QUESTION_BOOKMARKED)
    }

    @Test
    fun getQuestionById() = runBlockingTest {
        val chosenQuestionId = 19L
        val ques1 = Question(
            id = 1, question = "q1", option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 2, explanation = null, isBookmark = 0, isUserAdded = 0, topicId = 6
        )
        val ques2 = Question(
            id = chosenQuestionId, question = "q6", option1 = "a", option2 = "b", option3 = "c",
            option4 = "d", answerNr = 1, explanation = null,
            isBookmark = 1, isUserAdded = 1, topicId = 17
        )
        val ques3 = Question(
            id = 3, question = "q3", option1 = "a", option2 = "b", option3 = "c", option4 = "d",
            answerNr = 3, explanation = "Some", isBookmark = 0, isUserAdded = 0, topicId = 6
        )

        dao.insertQuestions(ques1, ques2, ques3)

        val question: Question =
            dao.getQuestionById(questionId = chosenQuestionId).getOrAwaitValue()

        assertThat(question).isEqualTo(ques2)
    }

    /* --------------------------- Test User answer related queries --------------------------- */
    @Test
    fun isInsertUserAnswerReplaceable() = runBlockingTest {
        val time = 13421412421L
        val topicId = 1
        val questionId = 4L
        val userAnswerId = 9L

        val question = Question(
            id = questionId, question = "A question", option1 = "a", option2 = "b", option3 = "c",
            option4 = "d", answerNr = 2, explanation = null, isBookmark = 0, isUserAdded = 0,
            topicId = topicId
        )

        val usrAns = UserAnswer(
            uaId = userAnswerId, answerNumber = 3,
            questionId = questionId, timestamp = time
        )
        val anotherUsrAns = UserAnswer(
            uaId = userAnswerId, answerNumber = 2,
            questionId = questionId, timestamp = time
        )

        val answerAndQuestion = AnswerAndQuestion(userAnswer = anotherUsrAns, question = question)

        dao.insertQuestion(question)
        dao.insertUserAnswer(usrAns)
        dao.insertUserAnswer(anotherUsrAns)

        val userAnswers: List<AnswerAndQuestion> =
            dao.getUserAnswersNoSorting(timestamp = time).getOrAwaitValue()

        assertThat(userAnswers).contains(answerAndQuestion)
    }

    @Test
    fun getUserAnswersNoSorting() = runBlockingTest {
        val topicId = 9
        val time = 101241L
        val questionId = 2L

        val q1 = Question(
            id = questionId, question = "Name", option1 = "a", option2 = "b", option3 = "c",
            option4 = "d", answerNr = 2, explanation = null, isBookmark = 0, isUserAdded = 0,
            topicId = topicId
        )

        val ua1 = UserAnswer(
            uaId = 10L, answerNumber = 1,
            questionId = questionId, timestamp = time
        )
        val ua2 = UserAnswer(
            uaId = 17L, answerNumber = 2,
            questionId = questionId, timestamp = time
        )
        val ua3 = UserAnswer(
            uaId = 219L, answerNumber = 4,
            questionId = questionId, timestamp = time
        )

        val qna1 = AnswerAndQuestion(userAnswer = ua1, question = q1)
        val qna2 = AnswerAndQuestion(userAnswer = ua2, question = q1)
        val qna3 = AnswerAndQuestion(userAnswer = ua3, question = q1)
        val qnaList: List<AnswerAndQuestion> = listOf(qna1, qna2, qna3)

        dao.insertQuestion(q1)
        dao.insertUserAnswers(ua1, ua2, ua3)

        val answerAndQuestionList: List<AnswerAndQuestion> = dao.getUserAnswersNoSorting(
            timestamp = time
        ).getOrAwaitValue()

        assertThat(answerAndQuestionList).containsExactlyElementsIn(qnaList).inOrder()
    }

    @Test
    fun getUserAnswersOrderByQuestionId() = runBlockingTest {
        val topicId = 9
        val time = 101241L

        val q1 = Question(
            id = 2L, question = "Name", option1 = "a", option2 = "b", option3 = "c",
            option4 = "d", answerNr = 2, explanation = null, isBookmark = 0, isUserAdded = 0,
            topicId = topicId
        )
        val q2 = Question(
            id = 3L, question = "Name", option1 = "a", option2 = "b", option3 = "c",
            option4 = "d", answerNr = 3, explanation = null, isBookmark = 1, isUserAdded = 0,
            topicId = topicId
        )
        val q3 = Question(
            id = 4L, question = "Name", option1 = "a", option2 = "b", option3 = "c",
            option4 = "d", answerNr = 1, explanation = null, isBookmark = 0, isUserAdded = 0,
            topicId = topicId
        )

        val ua1 = UserAnswer(
            uaId = 219L, answerNumber = 1,
            questionId = 2L, timestamp = time
        )
        val ua2 = UserAnswer(
            uaId = 1L, answerNumber = 2,
            questionId = 3L, timestamp = time
        )
        val ua3 = UserAnswer(
            uaId = 100L, answerNumber = 4,
            questionId = 4L, timestamp = time
        )

        val qna1 = AnswerAndQuestion(userAnswer = ua1, question = q1)
        val qna2 = AnswerAndQuestion(userAnswer = ua2, question = q2)
        val qna3 = AnswerAndQuestion(userAnswer = ua3, question = q3)
        val qnaList: List<AnswerAndQuestion> = listOf(qna1, qna2, qna3)

        dao.insertQuestions(q1, q2, q3)
        dao.insertUserAnswers(ua1, ua2, ua3)

        val answerAndQuestionList: List<AnswerAndQuestion> = dao.getUserAnswersOrderByQuestionId(
            timestamp = time
        ).getOrAwaitValue()

        assertThat(answerAndQuestionList).containsExactlyElementsIn(qnaList).inOrder()
    }

    @Test
    fun deleteUserAnswerBasedOnQuestion() = runBlockingTest {
        val questionId = 7L
        val topicId = 1
        val time = 1121L

        val question = Question(
            id = questionId, question = "A question", option1 = "a", option2 = "b", option3 = "c",
            option4 = "d", answerNr = 1, explanation = "ex", isBookmark = 1, isUserAdded = 0,
            topicId = topicId
        )

        val usrAns1 = UserAnswer(
            uaId = 9L, answerNumber = 3,
            questionId = questionId, timestamp = time
        )

        val answerAndQuestion = AnswerAndQuestion(userAnswer = usrAns1, question = question)

        dao.insertQuestion(question)
        dao.insertUserAnswer(usrAns1)
        dao.deleteUserAnswerBasedOnQuestion(questionId = questionId)

        val userAnswers: List<AnswerAndQuestion> =
            dao.getUserAnswersNoSorting(timestamp = time).getOrAwaitValue()

        assertThat(userAnswers).doesNotContain(answerAndQuestion)
    }

    @Test
    fun deleteUserAnswersByTimeStamp() = runBlockingTest {
        val timeStampToDelete = 115223L

        val usrAns1 = UserAnswer(
            uaId = 9L, answerNumber = 3,
            questionId = 56L, timestamp = timeStampToDelete
        )
        val usrAns2 = UserAnswer(
            uaId = 10L, answerNumber = 2,
            questionId = 57L, timestamp = timeStampToDelete
        )
        val usrAns3 = UserAnswer(
            uaId = 100L, answerNumber = 1,
            questionId = 100L, timestamp = timeStampToDelete
        )

        dao.insertUserAnswers(usrAns1, usrAns2, usrAns3)
        dao.deleteUserAnswersByTimeStamp(timestamp = timeStampToDelete)

        val userAnswers: List<UserAnswer> = dao.getUserAnswersByTimeStamp(
            timestamp = timeStampToDelete
        ).getOrAwaitValue()

        assertThat(userAnswers).isEmpty()
    }

    @Test
    fun deleteUserAnswersByTopic() = runBlockingTest {
        val topicId = 7
        val questionId = 89L

        val q = Question(
            id = questionId, question = "A question", option1 = "a", option2 = "b", option3 = "c",
            option4 = "d", answerNr = 1, explanation = "ex", isBookmark = 1, isUserAdded = 0,
            topicId = topicId
        )

        val ua1 = UserAnswer(
            uaId = 100L, answerNumber = 1,
            questionId = questionId, timestamp = 4894124L
        )
        val ua2 = UserAnswer(
            uaId = 700L, answerNumber = 3,
            questionId = questionId, timestamp = 4141904412L
        )

        dao.insertQuestion(q)
        dao.insertUserAnswers(ua1, ua2)
        dao.deleteUserAnswersByTopic(topicId = topicId)

        val qnaList: List<UserAnswer> = dao.getAllUserAnswers().getOrAwaitValue()

        assertThat(qnaList).isEmpty()
    }

    @Test
    fun getUserAnswersByTimeStamp() = runBlockingTest {
        val timeStamp = 124141L
        val ua1 = UserAnswer(
            uaId = 219L, answerNumber = 1,
            questionId = 2L, timestamp = timeStamp
        )
        val ua2 = UserAnswer(
            uaId = 1L, answerNumber = 2,
            questionId = 3L, timestamp = timeStamp
        )
        val ua3 = UserAnswer(
            uaId = 100L, answerNumber = 4,
            questionId = 4L, timestamp = timeStamp
        )

        dao.insertUserAnswers(ua1, ua2, ua3)

        val userAnswers: List<UserAnswer> = dao.getUserAnswersByTimeStamp(
            timestamp = timeStamp
        ).getOrAwaitValue()

        assertThat(userAnswers).containsExactly(ua1, ua2, ua3)
    }

    /* --------------------------- Test Score related queries --------------------------- */
    @Test
    fun isInsertUserScoreReplaceable() = runBlockingTest {
        val topicId = 1
        val score1 =
            Score(timestamp = 12L, topicId = topicId, totalCorrect = 5, totalQuestions = 12)
        val score2 =
            Score(timestamp = 12L, topicId = topicId, totalCorrect = 6, totalQuestions = 14)

        dao.insertUserScores(score1, score2)

        val scores: List<Score> = dao.getUserScoresByTopic(topicId = topicId).getOrAwaitValue()

        assertThat(scores).containsExactly(score2)
    }

    @Test
    fun getUserScore() = runBlockingTest {
        val time = 12L
        val score1 = Score(timestamp = time, topicId = 2, totalCorrect = 8, totalQuestions = 12)
        val score2 = Score(timestamp = 100L, topicId = 2, totalCorrect = 9, totalQuestions = 119)

        dao.insertUserScores(score1, score2)

        val score: Score = dao.getUserScore(timestamp = time).getOrAwaitValue()

        assertThat(score).isEqualTo(score1)
    }

    @Test
    fun getUserScoresByTopic() = runBlockingTest {
        val topicId = 3
        val score1 =
            Score(timestamp = 12L, topicId = topicId, totalCorrect = 15, totalQuestions = 24)
        val score2 =
            Score(timestamp = 100L, topicId = topicId, totalCorrect = 19, totalQuestions = 45)
        val score3 = Score(timestamp = 102L, topicId = 17, totalCorrect = 69, totalQuestions = 69)

        dao.insertUserScores(score1, score2, score3)

        val scores: List<Score> = dao.getUserScoresByTopic(topicId = topicId).getOrAwaitValue()

        assertThat(scores).containsExactly(score1, score2)
    }

    @Test
    fun getAllUserScores() = runBlockingTest {
        val score1 = Score(timestamp = 12L, topicId = 1, totalCorrect = 15, totalQuestions = 24)
        val score2 = Score(timestamp = 100L, topicId = 8, totalCorrect = 19, totalQuestions = 45)
        val score3 = Score(timestamp = 102L, topicId = 17, totalCorrect = 69, totalQuestions = 69)

        dao.insertUserScores(score1, score2, score3)

        val scores: List<Score> = dao.getAllUserScores().getOrAwaitValue()

        assertThat(scores).containsExactly(score1, score2, score3)
    }

    @Test
    fun getUserScoresByCourse() = runBlockingTest {
        val courseId = 2
        val topicId1 = 5
        val topicId2 = 9

        val topic1 = Topic(
            id = topicId1, name = "a", priority = 1, isUserAdded = 0, courseId = courseId
        )
        val topic2 = Topic(
            id = topicId2, name = "b", priority = 0, isUserAdded = 0, courseId = courseId
        )

        val score1 =
            Score(timestamp = 12L, topicId = topicId1, totalCorrect = 15, totalQuestions = 24)
        val score2 =
            Score(timestamp = 100L, topicId = topicId2, totalCorrect = 19, totalQuestions = 45)
        val score3 =
            Score(timestamp = 102L, topicId = topicId1, totalCorrect = 69, totalQuestions = 69)
        val score4 = Score(timestamp = 110L, topicId = 100, totalCorrect = 67, totalQuestions = 90)

        dao.insertTopics(topic1, topic2)
        dao.insertUserScores(score1, score2, score3, score4)

        val scores: List<Score> = dao.getUserScoresByCourse(
            courseId = courseId
        ).getOrAwaitValue()

        assertThat(scores).containsExactly(score1, score2, score3)
    }

    @Test
    fun getUserScoresByMixedQuiz() = runBlockingTest {
        val topicId = MIXED_TOPIC_ID
        val s1 = Score(
            timestamp = 901L, topicId = topicId, totalCorrect = 19, totalQuestions = 20
        )
        val s2 = Score(
            timestamp = 1241L, topicId = topicId, totalCorrect = 20, totalQuestions = 35
        )

        dao.insertUserScores(s1, s2)

        val scores: List<Score> = dao.getUserScoresByMixedQuiz().getOrAwaitValue()
        assertThat(scores).containsExactly(s1, s2)
    }

    @Test
    fun deleteScoreByTimeStamp() = runBlockingTest {
        val timeStampToDelete = 1243251L

        val s1 = Score(
            timestamp = timeStampToDelete, topicId = 5, totalCorrect = 19, totalQuestions = 20
        )
        dao.insertUserScore(s1)
        dao.deleteScoreByTimeStamp(timestamp = timeStampToDelete)

        val score: Score = dao.getUserScore(
            timestamp = timeStampToDelete
        ).getOrAwaitValue()

        assertThat(score).isNull()
    }

    @Test
    fun deleteScoresByTopic() = runBlockingTest {
        val topicId = 5
        val s1 = Score(
            timestamp = 901L, topicId = topicId, totalCorrect = 19, totalQuestions = 20
        )
        val s2 = Score(
            timestamp = 1241L, topicId = topicId, totalCorrect = 20, totalQuestions = 35
        )

        dao.insertUserScores(s1, s2)
        dao.deleteScoresByTopic(topicId = topicId)

        val scores: List<Score> = dao.getUserScoresByTopic(
            topicId = topicId
        ).getOrAwaitValue()
        assertThat(scores).isEmpty()
    }
}