package self.eng.hocmaians.util

object Constants {

    /* ---------------------------- All course names---------------------------- */
    const val MIXED_QUIZ = "Mixed Quiz"
    const val OVERALL = "Overall"

    // contains all course names, except for Mixed Quiz
    val COURSE_NAMES: List<String> = listOf(
        "Grammar", "Vocabulary", "Pronunciation", "Practice Test", MIXED_QUIZ
    )
    const val GRAMMAR_COURSE_ID = 1
    const val VOCABULARY_COURSE_ID = 2
    const val PRONUNCIATION_COURSE_ID = 3
    const val PRACTICE_TEST_COURSE_ID = 4

    /* ----------- Course, Topic, Question Entity related ----------- */
    const val DEFAULT_PRIORITY: Long = 0L
    const val MIXED_COURSE_NAME = "Mixed"
    const val MIXED_TOPIC_ID = 9999
    const val MIXED_TOPIC_NAME = "Mixed"
    const val QUESTION_BOOKMARKED = 1
    const val QUESTION_NOT_BOOKMARKED = 0

    /* ----------- Database related ----------- */
    const val DB_NAME = "quiz_database.db"
    const val TEST_DB_PATH = "database/partial_database.db"

    /* ----------- Date time related ----------- */
    const val DATE_FORMAT_PATTERN = "dd/MM/yyyy"
    const val TIME_FORMAT_PATTERN = "hh:mm a"
    const val DEFAULT_MILLISECOND: Long = 0L

    /* ----------- Tab Layout + ViewPager2 related ----------- */
    const val HOME_TAB_LAYOUT_NUMBER = 2
    const val GUIDE_LAYOUT_FRAGMENT_NUMBERS = 5
    const val PROGRESS_HOME_TAB_LAYOUT_NUMBER = 2

    /* ----------- DoOrderedQuizFragment ----------- */
    const val ZERO_QUESTIONS = 0    // zero to indicate that it is from "Choose Quiz By Topic"

    /* ----------- DoMixedQuizFragment ----------- */
    val QUANTITY_LIST: List<Int> = listOf(10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60)

    /* ----------- Quiz Fragment ----------- */
    const val PRACTICE_TEST_TIMER_IN_MILLIS: Long = 1_800_000L
    const val ONE_MINUTE_IN_MILLIS: Long = 60_000L
    const val TIME_OUT = 0L
    const val FORMAT_COUNT_DOWN_TIMER = "%02d:%02d"
    const val KEY_CURRENT_ORIENTATION = "Orientation"
    const val NOT_ANSWER_YET = -1
    const val UN_INITIALIZE_POSITION = -1
    const val KEY_CURRENT_POSITION = "CurrentPosition"
    const val KEY_CURRENT_QUESTION = "CurrentQuestion"

    /* ----------- Graph Fragment ----------- */
    const val ANIMATE_X_DURATION = 2000
    const val ANIMATE_Y_DURATION = 2000
    const val MAX_Y_VALUE: Float = 13f
    const val MIN_Y_VALUE: Float = -3f
    const val MAX_SCORE: Float = 10f
    const val MIN_SCORE: Float = 0f
    const val LIMIT_LINE_TEXT_SIZE: Float = 10f

    /* ----------- ReviewAnswersFragment ----------- */
    const val QUIZ_FRAGMENT = 2
    const val GRAPH_FRAGMENT = 4
    const val INVALID_SCORE = -1.0

    /* ----------- Manage Courses Fragment ----------- */
    const val ACTION_EDIT_COURSE = "ACTION_EDIT_COURSE"
    const val ACTION_ADD_COURSE = "ACTION_ADD_COURSE"

    /* ----------- Manage Topics Fragment ----------- */
    const val ACTION_EDIT_TOPIC = "ACTION_EDIT_TOPIC"
    const val ACTION_ADD_TOPIC = "ACTION_ADD_TOPIC"

    /* ----------- Manage Questions Fragment ----------- */
    const val ACTION_EDIT_QUESTION = "ACTION_EDIT_QUESTION"
    const val ACTION_ADD_QUESTION = "ACTION_ADD_QUESTION"

    /* ----------- Add/Edit ----------- */
    const val ADMIN_ADDED = 0
    const val USER_ADDED = 1
    const val MAX_COURSE_NAME_LENGTH = 50
    const val MAX_TOPIC_NAME_LENGTH = 100

    /* -------------- Conditions to filter results in Detail Score Fragment ---------------- */
    const val OVERALL_RESULTS = 1
    const val COURSE_BASED_RESULTS = 2
    const val TOPIC_BASED_RESULTS = 3
    const val MIXED_QUIZ_RESULTS = 4

    /* ----------------------------- Radar chart constants----------------------------- */
    const val RADAR_CHART_X_ANIMATION_TIME = 1400
    const val RADAR_CHART_Y_ANIMATION_TIME = 1400
    const val OVER_MAXIMUM_SCORE_OF_100 = 101f

    /* ----------------------------- Pie chart constants----------------------------- */
    const val PIE_CHART_Y_ANIMATION_TIME = 1400
    const val  FILTER_BY_OVERALL = 0
    const val FILTER_BY_GRAMMAR = 1
    const val FILTER_BY_VOCABULARY = 2
    const val FILTER_BY_PRONUNCIATION = 3
    const val FILTER_BY_PRACTICE_TEST = 4

    /* ----------------------------- Test DAO constants----------------------------- */
    const val TEST_DB = "test_db"
}