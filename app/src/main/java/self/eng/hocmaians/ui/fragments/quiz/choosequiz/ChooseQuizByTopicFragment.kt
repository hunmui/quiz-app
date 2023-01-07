package self.eng.hocmaians.ui.fragments.quiz.choosequiz

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import self.eng.hocmaians.R
import self.eng.hocmaians.data.entities.Course
import self.eng.hocmaians.data.entities.Topic
import self.eng.hocmaians.databinding.FragmentChooseQuizByTopicBinding
import self.eng.hocmaians.util.CommonMethods
import self.eng.hocmaians.util.Constants.ZERO_QUESTIONS
import self.eng.hocmaians.util.Status

@AndroidEntryPoint
class ChooseQuizByTopicFragment : Fragment(R.layout.fragment_choose_quiz_by_topic) {

    // view binding
    private var _binding: FragmentChooseQuizByTopicBinding? = null
    private val binding get() = _binding!!

    // view model
    private val viewModel: ChooseQuizByTopicViewModel by viewModels()

    // toast holder
    private var toastHolder: Toast? = null

    // observable variables
    private var noCourses = false
    private var noTopics = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChooseQuizByTopicBinding.bind(view)

        subscribeToObserver()

        viewModel.courses.observe(viewLifecycleOwner) { courses ->
            if (courses.isEmpty()) {
                noCourses = true
                binding.spinnerChooseCourse.adapter = null
                setTotalQuestionsToZero()
            } else {
                loadCoursesToSpinner(courses = courses)
            }
        }

        setHelpButtons()

        binding.btnStartOrderedQuiz.setOnClickListener {
            startQuiz()
        }
    }

    /**
     * Load all course from the Courses table in the database.
     * Let the spinner display all courses, then load all topics which are corresponding to
     * the chosen course.
     *
     * @param courses a list of all courses available
     */
    private fun loadCoursesToSpinner(courses: List<Course>) {

        val courseAdapter: ArrayAdapter<Course> = ArrayAdapter(
            requireContext(),
            R.layout.spinner_display_text,
            courses
        )
        courseAdapter.setDropDownViewResource(R.layout.each_spinner_text_view)

        binding.spinnerChooseCourse.apply {
            adapter = courseAdapter

            // load the chosen course in case of screen rotation
            viewModel.chosenCourse?.let {
                this.setSelection(courseAdapter.getPosition(it))
            }

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val chosenCourse = parent?.selectedItem as Course
                    viewModel.onChooseCourse(course = chosenCourse)
                    viewModel.topicsByCourse.observe(viewLifecycleOwner) { topics ->
                        if (topics.isEmpty()) {
                            binding.spinnerChooseTopic.adapter = null
                            noTopics = true
                            setTotalQuestionsToZero()
                        } else {
                            loadTopicsToSpinner(topics = topics)
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
    }

    /**
     * Loads all topics from the Topic table in the database, based on the chosen Course.
     * Let the spinner display all the selected topics. Then show total number of questions that are
     * available in the selected topic.
     *
     * @param topics a list of all topics based on the chosen course
     */
    private fun loadTopicsToSpinner(topics: List<Topic>) {

        val topicAdapter: ArrayAdapter<Topic> = ArrayAdapter(
            requireContext(),
            R.layout.spinner_display_text,
            topics
        )
        topicAdapter.setDropDownViewResource(R.layout.each_spinner_text_view)

        binding.spinnerChooseTopic.apply {
            adapter = topicAdapter

            // load the chosen topic in case of screen rotation
            viewModel.chosenTopic.let {
                this.setSelection(topicAdapter.getPosition(it))
            }

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val chosenTopic = parent?.selectedItem as Topic
                    viewModel.onChooseTopic(chosenTopic)
                    viewModel.totalQuestionsByTopic.observe(viewLifecycleOwner) {
                        viewModel.totalQuestionsInTopic = it
                        binding.tvShowTotalQuestions.text = it.toString()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
    }

    /**
     * Start the quiz, pass 4 required arguments: courseName, topicName, topicId, and question
     * quantity
     */
    private fun startQuiz() {
        when {
            noCourses -> {
                toastHolder = Toast.makeText(
                    requireContext(),
                    R.string.no_courses_to_start_test,
                    Toast.LENGTH_LONG
                )
                toastHolder?.show()
            }
            noTopics -> {
                toastHolder = Toast.makeText(
                    requireContext(),
                    R.string.no_topics_to_start_test,
                    Toast.LENGTH_LONG
                )
                toastHolder?.show()
            }
            else -> {
                viewModel.onStartTest()
            }
        }
    }

    /**
     * Observe the status of starting the test.
     */
    private fun subscribeToObserver() {
        viewModel.startTestStatus.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        // changing fragment, pass required arguments
                        val action = HomeFragmentDirections.actionHomeFragmentToQuizFragment(
                            courseName = viewModel.chosenCourse!!.name,
                            topicId = viewModel.chosenTopic!!.id,
                            topicName = viewModel.chosenTopic!!.name,
                            quizAmount = ZERO_QUESTIONS
                        )
                        findNavController().navigate(action)
                    }
                    Status.ERROR -> {
                        toastHolder = Toast.makeText(
                            requireContext(),
                            result.message ?: getString(R.string.unknown_error_occurred),
                            Toast.LENGTH_LONG
                        )
                        toastHolder?.show()
                    }
                    Status.LOADING -> {
                        /* NO-OP */
                    }
                }
            }
        }
    }

    /**
     * Set 2 helper buttons: choose course and choose topic
     */
    private fun setHelpButtons() {
        binding.apply {
            ivChooseCourseHelp.setOnClickListener {
                CommonMethods.showHelpDialog(
                    context = requireContext(),
                    title = getString(R.string.pick_course_help_title),
                    message = getString(R.string.pick_choose_course_help)
                )
            }

            ivChooseTopicHelp.setOnClickListener {
                CommonMethods.showHelpDialog(
                    context = requireContext(),
                    title = getString(R.string.pick_topic_help_title),
                    message = getString(R.string.pick_choose_topic_help)
                )
            }
        }
    }

    /**
     * If there are no courses or no topics in the database then set total questions to 0
     */
    private fun setTotalQuestionsToZero() {
        viewModel.totalQuestionsInTopic = ZERO_QUESTIONS
        binding.tvShowTotalQuestions.text = ZERO_QUESTIONS.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        toastHolder?.cancel()
    }
}