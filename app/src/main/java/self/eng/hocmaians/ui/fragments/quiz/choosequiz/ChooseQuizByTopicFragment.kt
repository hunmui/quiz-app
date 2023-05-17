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
     * Tải tất cả khóa học từ bảng Khóa học trong cơ sở dữ liệu.
     * Để spinner hiển thị tất cả các khóa học, sau đó tải tất cả các chủ đề tương ứng với
     * khóa học đã chọn.
     *
     * @param courses danh sách tất cả các khóa học có sẵn
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

            // tải khóa học đã chọn trong trường hợp xoay màn hình
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
     * Tải tất cả các chủ đề từ bảng Chủ đề trong cơ sở dữ liệu, dựa trên Khóa học đã chọn.
     * Để spinner hiển thị tất cả các chủ đề đã chọn. Sau đó hiển thị tổng số câu hỏi đó là
     * có sẵn trong chủ đề đã chọn.
     *
     * @param topics danh sách tất cả các chủ đề dựa trên khóa học đã chọn
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

            // tải chủ đề đã chọn trong trường hợp xoay màn hình
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
     * Bắt đầu bài kiểm tra, vượt qua 4 đối số bắt buộc: Tên khóa học, Tên chủ đề, Id chủ đề và câu hỏi
     * Số lượng
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
     * Quan sát trạng thái bắt đầu kiểm tra.
     */
    private fun subscribeToObserver() {
        viewModel.startTestStatus.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        // thay đổi fragment, truyền các đối số cần thiết
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
     * Đặt 2 nút trợ giúp: chọn khóa học và chọn chủ đề
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
     * Nếu không có khóa học hoặc không có chủ đề trong cơ sở dữ liệu, hãy đặt tổng số câu hỏi thành 0
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