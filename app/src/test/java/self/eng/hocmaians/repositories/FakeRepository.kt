package self.eng.hocmaians.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import self.eng.hocmaians.data.entities.*
import self.eng.hocmaians.data.entities.realtions.AnswerAndQuestion
import self.eng.hocmaians.util.Constants.MIXED_TOPIC_ID
import self.eng.hocmaians.util.Constants.QUESTION_BOOKMARKED
import self.eng.hocmaians.util.Constants.USER_ADDED

class FakeRepository : IRepository {

    // lists - representing tables in db
    private val courses: MutableList<Course> = mutableListOf()
    private val topics: MutableList<Topic> = mutableListOf()
    private val questions: MutableList<Question> = mutableListOf()
    private val userAnswers: MutableList<UserAnswer> = mutableListOf()
    private val scores: MutableList<Score> = mutableListOf()

    // observable
    private val observableCourses: MutableLiveData<List<Course>> = MutableLiveData(courses)
    private val observableCourseName: MutableLiveData<String> = MutableLiveData()
    private val observableCourseNames: MutableLiveData<List<String>> = MutableLiveData()

    private val observableTopics: MutableLiveData<List<Topic>> = MutableLiveData()
    private val observableTopicName: MutableLiveData<String> = MutableLiveData()
    private val observableTopicNames: MutableLiveData<List<String>> = MutableLiveData()

    private val observableQuestions: MutableLiveData<List<Question>> = MutableLiveData()
    private val observableTotalQuestionsByTopic: MutableLiveData<Int> = MutableLiveData()
    private val observableTotalQuestions: MutableLiveData<Long> = MutableLiveData()

    private val observableUserAnswers: MutableLiveData<List<UserAnswer>> = MutableLiveData()
    private val observableQNA: MutableLiveData<List<AnswerAndQuestion>> = MutableLiveData()

    private val observableScores: MutableLiveData<List<Score>> = MutableLiveData()
    private val observableScore: MutableLiveData<Score> = MutableLiveData()

    /* ------------------------------- Courses ------------------------------- */
    override fun getAllCourses(): LiveData<List<Course>> = observableCourses

    override fun getAllCourseNames(): LiveData<List<String>> {
        val courseNames: MutableList<String> = mutableListOf()

        courses.forEach { course ->
            courseNames.add(course.name)
        }

        observableCourseNames.postValue(courseNames)
        return observableCourseNames
    }

    override fun getCourseNameBasedOnTopicId(topicId: Int): LiveData<String> {
        val findingTopic = topics.first { topic ->
            topic.id == topicId
        }

        val findingCourse = courses.first { course ->
            course.id == findingTopic.courseId
        }

        observableCourseName.postValue(findingCourse.name)
        return observableCourseName
    }

    override suspend fun updateCoursePriority(courseId: Int, newCoursePriority: Long) {
        courses.forEach { course ->
            if (course.id == courseId) {
                course.priority = newCoursePriority
                return@forEach
            }
        }
        observableCourses.postValue(courses)
    }

//    private fun refreshCoursesLiveData() {
//        observableCourses.postValue(courses)
//        observableCourseNames.postValue(courseNames())
//    }
//
//    private fun courseNames(): List<String> {
//        val courseNames: MutableList<String> = mutableListOf()
//
//        courses.forEach { course ->
//            courseNames.add(course.name)
//        }
//
//        return courseNames
//    }

    /* ------------------------------- Topics ------------------------------- */

    override fun getTopicsBasedOnCourse(courseId: Int): LiveData<List<Topic>> {
        val topicList: MutableList<Topic> = mutableListOf()

        topics.forEach { topic ->
            if (topic.courseId == courseId) {
                topicList.add(topic)
            }
        }

        observableTopics.postValue(topicList)
        return observableTopics
    }

    override fun getTopicsNamesBasedOnCourse(courseId: Int): LiveData<List<String>> {
        val topicNames: MutableList<String> = mutableListOf()

        topics.forEach { topic ->
            if (topic.courseId == courseId) {
                topicNames.add(topic.name)
            }
        }

        observableTopicNames.postValue(topicNames)
        return observableTopicNames
    }

    override fun getTopicNameBasedOnTopicId(topicId: Int): LiveData<String> {
        val findingTopic = topics.first { topic ->
            topic.id == topicId
        }

        observableTopicName.postValue(findingTopic.name)
        return observableTopicName
    }

    override suspend fun insertTopic(topic: Topic) {
        topics.add(topic)
        observableTopics.postValue(topics)
    }

    override suspend fun deleteTopic(topic: Topic) {
        topics.remove(topic)
        observableTopics.postValue(topics)
    }

    override suspend fun updateTopicPriority(topicId: Int, newTopicPriority: Long) {
        topics.forEach { topic ->
            if (topic.id == topicId) {
                topic.priority = newTopicPriority
                return@forEach
            }
        }
        observableTopics.postValue(topics)
    }

    override suspend fun updateTopicName(topicId: Int, topicName: String) {
        topics.forEach { topic ->
            if (topic.id == topicId) {
                topic.name = topicName
                return@forEach
            }
        }
        observableTopics.postValue(topics)
    }

    /* ------------------------------- Questions ------------------------------- */
    override fun getQuestionsBasedOnTopic(topicId: Int): LiveData<List<Question>> {
        val questionList: MutableList<Question> = mutableListOf()

        questions.forEach { question ->
            if (question.topicId == topicId) {
                questionList.add(question)
            }
        }

        observableQuestions.postValue(questions)
        return observableQuestions
    }

    override fun getRandomQuestions(quantity: Int): LiveData<List<Question>> {
        val randomQuestionList: List<Question> = questions.shuffled().subList(0, quantity)

        observableQuestions.postValue(randomQuestionList)
        return observableQuestions
    }

    override fun getAllBookmarks(): LiveData<List<Question>> {
        val bookmarks: MutableList<Question> = mutableListOf()

        questions.forEach { question ->
            if (question.isBookmark == QUESTION_BOOKMARKED) {
                bookmarks.add(question)
            }
        }

        observableQuestions.postValue(bookmarks)
        return observableQuestions
    }

    override fun getBookmarksBasedOnTopicId(topicId: Int): LiveData<List<Question>> {
        val bookmarks: MutableList<Question> = mutableListOf()

        questions.forEach { question ->
            if (question.topicId == topicId && question.isBookmark == QUESTION_BOOKMARKED) {
                bookmarks.add(question)
            }
        }

        observableQuestions.postValue(bookmarks)
        return observableQuestions
    }

    override fun countQuestionsBasedOnTopic(topicId: Int): LiveData<Int> =
        observableTotalQuestionsByTopic

    override fun countAllQuestions(): LiveData<Long> = observableTotalQuestions

    override suspend fun insertQuestion(question: Question) {
        questions.add(question)
        refreshQuestionsLiveData(question.topicId)
    }

    override suspend fun deleteQuestion(question: Question) {
        questions.remove(question)
        refreshQuestionsLiveData(question.topicId)
    }

    override suspend fun deleteQuestionsByTopic(topicId: Int) {
        questions.removeIf { question ->
            question.topicId == topicId
        }
        refreshQuestionsLiveData(topicId = topicId)
    }

    override suspend fun updateQuestion(question: Question) {
        val findingQuestion = questions.first { expectingQuestion ->
            expectingQuestion.id == question.id
        }

        questions.remove(findingQuestion)
        questions.add(question)

        observableQuestions.postValue(questions)
    }

    override suspend fun updateQuestionBookmark(questionId: Long, bookmark: Int) {
        questions.forEach { question ->
            if (question.id == questionId) {
                question.isBookmark = bookmark
                return@forEach
            }
        }
        observableQuestions.postValue(questions)
    }

    private fun refreshQuestionsLiveData(topicId: Int) {
        observableQuestions.postValue(questions)
        observableTotalQuestions.postValue(questions.size.toLong())
        observableTotalQuestionsByTopic.postValue(totalQuestionByTopic(topicId))
    }

    private fun totalQuestionByTopic(topicId: Int): Int = questions.count { question ->
        question.topicId == topicId
    }

    override fun getUserAddedQuestionsByTopic(topicId: Int): LiveData<List<Question>> {
        val userAddedQuestions: MutableList<Question> = mutableListOf()

        questions.forEach { question ->
            if (question.isUserAdded == USER_ADDED) {
                userAddedQuestions.add(question)
            }
        }

        observableQuestions.postValue(userAddedQuestions)
        return observableQuestions
    }

    /* ----------------------- User answers ----------------------- */
    override suspend fun insertUserAnswer(userAnswer: UserAnswer) {
        userAnswers.add(userAnswer)
        observableUserAnswers.postValue(userAnswers)
    }

    override fun getUserAnswersNoSorting(
        timestamp: Long
    ): LiveData<List<AnswerAndQuestion>> {
        val qnaList: MutableList<AnswerAndQuestion> = mutableListOf()

        val sortedUserAnswers = userAnswers.sortedBy { answer ->
            answer.uaId
        }

        sortedUserAnswers.forEach { answer ->
            if (answer.timestamp == timestamp) {
                val findingQuestion = questions.first { question ->
                    question.id == answer.questionId
                }
                qnaList.add(AnswerAndQuestion(userAnswer = answer, question = findingQuestion))
            }
        }

        observableQNA.postValue(qnaList)
        return observableQNA
    }

    override fun getUserAnswersOrderByQuestionId(
        timestamp: Long
    ): LiveData<List<AnswerAndQuestion>> {
        val qnaList: MutableList<AnswerAndQuestion> = mutableListOf()
        val allAnswersByTimeStamp: MutableList<UserAnswer> = mutableListOf()

        userAnswers.forEach { answer ->
            if (answer.timestamp == timestamp) {
                allAnswersByTimeStamp.add(answer)
            }
        }

        // sort by question id Ascending
        allAnswersByTimeStamp.sortBy { answer ->
            answer.questionId
        }

        allAnswersByTimeStamp.forEach { answer ->
            val findingQuestion = questions.first { question ->
                question.id == answer.questionId
            }
            qnaList.add(AnswerAndQuestion(userAnswer = answer, question = findingQuestion))
        }

        observableQNA.postValue(qnaList)
        return observableQNA
    }

    override suspend fun deleteUserAnswerBasedOnQuestion(questionId: Long) {
        userAnswers.removeIf { answer ->
            answer.questionId == questionId
        }
        observableUserAnswers.postValue(userAnswers)
    }

    override suspend fun deleteUserAnswersByTimeStamp(timestamp: Long) {
        userAnswers.removeIf { answer ->
            answer.timestamp == timestamp
        }
        observableUserAnswers.postValue(userAnswers)
    }

    override suspend fun deleteUserAnswersByTopic(topicId: Int) {
        val questionIdsToDelete: MutableList<Long> = mutableListOf()

        questions.forEach { question ->
            if (question.topicId == topicId) {
                questionIdsToDelete.add(question.id)
            }
        }

        userAnswers.removeIf {
            it.questionId in questionIdsToDelete
        }
        observableUserAnswers.postValue(userAnswers)
    }

    override fun getAllUserAnswers(): LiveData<List<UserAnswer>> = observableUserAnswers

    /* --------------------------------- Score --------------------------------- */
    override suspend fun insertUserScore(score: Score) {
        scores.add(score)
        observableScores.postValue(scores)
    }

    override fun getUserScore(timestamp: Long): LiveData<Score> {
        val findingScore = scores.first { score ->
            score.timestamp == timestamp
        }

        observableScore.postValue(findingScore)
        return observableScore
    }

    override fun getUserScoresByTopic(topicId: Int): LiveData<List<Score>> {
        val scoresByTopic: MutableList<Score> = mutableListOf()

        scores.forEach { score ->
            if (score.topicId == topicId) {
                scoresByTopic.add(score)
            }
        }

        observableScores.postValue(scoresByTopic)
        return observableScores
    }

    override fun getAllUserScores(): LiveData<List<Score>> {
        observableScores.postValue(scores)
        return observableScores
    }

    override fun getUserScoresByCourse(courseId: Int): LiveData<List<Score>> {
        val topicIdList: MutableList<Int> = mutableListOf()
        val scoresByCourse: MutableList<Score> = mutableListOf()

        topics.forEach { topic ->
            if (topic.courseId == courseId) {
                topicIdList.add(topic.id)
            }
        }

        scores.forEach { score ->
            if (score.topicId in topicIdList) {
                scoresByCourse.add(score)
            }
        }

        observableScores.postValue(scoresByCourse)
        return observableScores
    }

    override fun getUserScoresByMixedQuiz(): LiveData<List<Score>> {
        val scoresByMixedQuiz: MutableList<Score> = mutableListOf()

        scores.forEach { score ->
            if (score.topicId == MIXED_TOPIC_ID) {
                scoresByMixedQuiz.add(score)
            }
        }

        observableScores.postValue(scoresByMixedQuiz)
        return observableScores
    }

    override suspend fun deleteScoreByTimeStamp(timestamp: Long) {
        val findingScore = scores.first { score ->
            score.timestamp == timestamp
        }

        scores.remove(findingScore)
        observableScores.postValue(scores)
    }

    override suspend fun deleteScoresByTopic(topicId: Int) {
        scores.removeIf {
            it.topicId == topicId
        }
        observableScores.postValue(scores)
    }
}