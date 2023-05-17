package self.eng.hocmaians.ui.fragments.quiz.test

import android.app.AlertDialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.Surface
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import self.eng.hocmaians.R
import self.eng.hocmaians.data.entities.Question
import self.eng.hocmaians.databinding.FragmentQuizBinding
import self.eng.hocmaians.ui.custom.ZoomOutPageTransformer
import self.eng.hocmaians.util.CommonMethods
import self.eng.hocmaians.util.Constants
import self.eng.hocmaians.util.Constants.COURSE_NAMES
import self.eng.hocmaians.util.Constants.FORMAT_COUNT_DOWN_TIMER
import self.eng.hocmaians.util.Constants.KEY_CURRENT_ORIENTATION
import self.eng.hocmaians.util.Constants.ONE_MINUTE_IN_MILLIS
import self.eng.hocmaians.util.Constants.QUIZ_FRAGMENT
import self.eng.hocmaians.util.Constants.TIME_OUT
import self.eng.hocmaians.util.Constants.ZERO_QUESTIONS
import self.eng.hocmaians.util.Status
import java.util.*


// TODO: CANNOT Handles up button when doing quiz AND when checking answers?? Can but have to rewrite every single fragment to handle up btn
/**
 * KHÔNG cho phép người dùng xoay màn hình trong đoạn này
 *
 * nhận Xoay màn hình hiện tại:
 * https://stackoverflow.com/questions/10380989/how-do-i-get-the-current-orientation-activityinfo-screen-orientation-of-an-a
 *
 * giữ định hướng hiện tại:
 * https://stackoverflow.com/questions/51710304/set-landscape-orientation-for-fragment-in-single-activity-architecture
 */
@AndroidEntryPoint
class QuizFragment : Fragment(R.layout.fragment_quiz) {

    // view binding
    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    // view model
    private val viewModel: QuizViewModel by viewModels()

    // chứa tất cả các đối số đã truyền
    private val args: QuizFragmentArgs by navArgs()

    // adapters
    private lateinit var quizAdapter: QuizAdapter
    private lateinit var checkAnswersAdapter: CheckAnswersAdapter

    private var isCheckAnswersOpened = false

    // hướng màn hình hiện tại
    private var currentOrientation: Int = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    // hẹn giờ cho bài kiểm tra thực hành
    private lateinit var countDownTimer: CountDownTimer
    private var timeLeftInMillis: Long = Constants.PRACTICE_TEST_TIMER_IN_MILLIS

    /**
     * Xử lý sự kiện khi người dùng bấm vào nút quay lại trên thiết bị
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)

        setHasOptionsMenu(true)

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    /**
     * Nhận xoay màn hình hiện tại.
     *
     * @return xoay màn hình hiện tại.
     */
    private fun getScreenOrientation(): Int {
        val rotation: Int = activity?.windowManager?.defaultDisplay!!.rotation
        val dm = DisplayMetrics()
        activity?.windowManager!!.defaultDisplay.getMetrics(dm)
        val width = dm.widthPixels
        val height = dm.heightPixels

        // nếu hướng tự nhiên của thiết bị là dọc:
        return if (
            ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && height > width) ||
            ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && width > height)
        ) {
            when (rotation) {
                Surface.ROTATION_0 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                Surface.ROTATION_90 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                Surface.ROTATION_180 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                Surface.ROTATION_270 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                else -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        } else {
            when (rotation) {
                Surface.ROTATION_0 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                Surface.ROTATION_90 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                Surface.ROTATION_180 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                Surface.ROTATION_270 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                else -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }
    }

    /**
     * Lưu hướng hiện tại
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(KEY_CURRENT_ORIENTATION, currentOrientation)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentQuizBinding.bind(view)

        subscribeToObserver()

        currentOrientation = savedInstanceState?.getInt(
            KEY_CURRENT_ORIENTATION
        ) ?: getScreenOrientation()

        // sửa hướng màn hình cho tất cả các fragments trong MainActivity
        activity?.requestedOrientation = currentOrientation

        setupCheckAnswerRecyclerView()

        // chỉ khởi tạo nếu lần đầu tiên fragment này được tạo
        if (savedInstanceState == null) {
            viewModel.getQuestions(questionQuantity = args.quizAmount, topicId = args.topicId)

            viewModel.questions.observe(viewLifecycleOwner) { questions ->

                val actualQuestionList: List<Question> = if (args.quizAmount == ZERO_QUESTIONS) {
                    questions.shuffled()
                } else {
                    questions
                }

                viewModel.initialize(questions = actualQuestionList)
                setupQuizViewPager(questions = actualQuestionList)

                setDoneQuantityText(viewModel.doneQuantity)
            }
        }

        // bắt đầu hẹn giờ nếu đây là bài kiểm tra thực hành
        if (args.courseName == COURSE_NAMES[3]) {
            startCountDown()
        }

        setSomeTextOnTop()

        viewModel.answers.observe(viewLifecycleOwner) { answers ->
            checkAnswersAdapter.differ.submitList(answers)
        }

        binding.apply {
            // mở HOẶC đóng kiểm tra câu trả lời RecyclerView
            btnCheckAnswers.setOnClickListener {
                isCheckAnswersOpened = !isCheckAnswersOpened
                setVisibilityOfCheckAnswers()
            }

            // submit quiz
            btnSubmitQuiz.setOnClickListener {
                pbSubmitTest.visibility = View.VISIBLE
                viewModel.onSubmitTest(topicId = args.topicId)
            }
        }
    }

    /**
     * Quan sát trạng thái lưu câu trả lời của người dùng và ghi điểm.
     */
    private fun subscribeToObserver() {
        viewModel.save.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        binding.pbSubmitTest.visibility = View.GONE

                        // thay đổi fragment, truyền đối số cần thiết
                        val action = QuizFragmentDirections
                            .actionQuizFragmentToReviewAnswersFragment(
                                topicId = args.topicId,
                                doneTime = viewModel.timestamp,
                                whichFragment = QUIZ_FRAGMENT
                            )
                        findNavController().navigate(action)
                    }
                    Status.ERROR -> {
                        Toast.makeText(
                            requireContext(),
                            result.message ?: getString(R.string.unknown_error_occurred),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Status.LOADING -> {
                        /* NO-OP */
                    }
                }
            }
        }
    }

    /**
     * Thiết lập bộ điều hợp cho viewPager
     *
     * @param questions một danh sách các câu hỏi
     */
    private fun setupQuizViewPager(questions: List<Question>) {
        quizAdapter = QuizAdapter(this, questions)

        binding.viewPagerQuiz.apply {
            adapter = quizAdapter
            setPageTransformer(ZoomOutPageTransformer())
        }

        // set check listener on QuizAdapter to log user answer
        quizAdapter.returnAnswerToHostFragment { answeredPosition, questionPosition ->
            if (binding.viewPagerQuiz.currentItem == questionPosition) {
                onChecked(answeredPosition = answeredPosition)
            }
        }
    }

    /**
     * Đặt tên khóa học và tên chủ đề
     */
    private fun setSomeTextOnTop() {
        binding.apply {
            val courseText = "${getString(R.string.tv_course)} ${args.courseName}"
            val topicText = "${getString(R.string.tv_topic)} ${args.topicName}"

            tvCourse.text = courseText
            tvTopic.text = topicText
        }
    }

    /**
     * Thiết lập chế độ xem trình tái chế câu trả lời kiểm tra và đặt trình nghe nhấp chuột trên bộ điều hợp của nó
     */
    private fun setupCheckAnswerRecyclerView() {
        checkAnswersAdapter = CheckAnswersAdapter()

        binding.rvCheckAnswers.apply {
            adapter = checkAnswersAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        // đăng nhập trang viewPager mà người dùng muốn điều hướng
        checkAnswersAdapter.setOnCheckAnswerClickListener { viewPagerPosition ->
            onAnswerClick(position = viewPagerPosition)
        }
    }

    /**
     * Đặt chế độ hiển thị của chế độ xem trình tái chế câu trả lời kiểm tra và thay đổi văn bản btnCheckAnswers
     */
    private fun setVisibilityOfCheckAnswers() {
        if (isCheckAnswersOpened) {
            showRecyclerView()
        } else {
            hideRecyclerView()
        }
    }

    /**
     * Ghi lại câu trả lời của người dùng (trả lời mới hoặc thay đổi câu trả lời). Sau đó tự động chuyển sang câu hỏi tiếp theo,
     * chỉ khi câu hỏi đó mới được trả lời.
     *
     * @param answerPosition câu trả lời của người dùng.
     */
    private fun onChecked(answeredPosition: Int) {

        binding.apply {
            // lấy vị trí hiện tại của ViewPager2 (vị trí bắt đầu từ 0)
            checkAnswersAdapter.notifyItemChanged(viewPagerQuiz.currentItem)

            val nextPage = viewModel.onAnswerQuestion(
                currentPosition = viewPagerQuiz.currentItem,
                answerPosition = answeredPosition
            )
            setDoneQuantityText(viewModel.doneQuantity)

            // di chuyển đến trang tiếp theo
            if ((nextPage == (viewPagerQuiz.currentItem + 1)) &&
                (nextPage != viewPagerQuiz.adapter?.itemCount)
            ) {
                viewPagerQuiz.setCurrentItem(nextPage, false)
            }
        }
    }

    /**
     * Phải làm gì khi người dùng nhấp vào câu trả lời trong kiểm tra Chế độ xem trình tái chế câu trả lời: đóng chế độ xem trình tái chế,
     * và di chuyển viewPager đến câu hỏi tương ứng.
     *
     * @param position vị trí của câu hỏi
     */
    private fun onAnswerClick(position: Int) {
        isCheckAnswersOpened = false

        binding.apply {
            viewPagerQuiz.setCurrentItem(position, false)
            hideRecyclerView()
        }
    }

    /**
     * Đặt văn bản số lượng đã thực hiện.
     *
     * @param doneQuantity lượng người dùng đã thực hiện bao nhiêu câu hỏi cho đến nay.
     */
    private fun setDoneQuantityText(doneQuantity: Int) {
        val textToDisplay =
            "${getString(R.string.tv_done_quantity)} ${doneQuantity}/${viewModel.questionList.size}"
        binding.tvDoneQuantity.text = textToDisplay
    }

    /**
     * Show recycler view
     */
    private fun showRecyclerView() {
        binding.apply {
            btnCheckAnswers.text = getString(R.string.btn_back_to_quiz)
            viewPagerQuiz.visibility = View.GONE

            tvCheckQuestionNumber.visibility = View.VISIBLE
            verticalSplitLine2.visibility = View.VISIBLE
            tvAnswerStatus.visibility = View.VISIBLE
            rvCheckAnswers.visibility = View.VISIBLE
        }
    }

    /**
     * Hide recycler view
     */
    private fun hideRecyclerView() {
        binding.apply {
            btnCheckAnswers.text = getString(R.string.btn_check_answers)
            rvCheckAnswers.visibility = View.GONE
            tvCheckQuestionNumber.visibility = View.GONE
            verticalSplitLine2.visibility = View.GONE
            tvAnswerStatus.visibility = View.GONE

            viewPagerQuiz.visibility = View.VISIBLE
        }
    }

    /**
     * Close check answers adapter if it is visible, otherwise show a dialog to confirm user exit
     */
    private fun onBackPressed() {
        // if check answers recycler view is opening
        if (isCheckAnswersOpened) {
            isCheckAnswersOpened = false
            hideRecyclerView()
        } else {
            // user is doing quiz
            showAreYouSureDialog()
        }
    }

    /**
     * Đóng bộ điều hợp kiểm tra câu trả lời nếu nó hiển thị, nếu không sẽ hiển thị hộp thoại để xác nhận người dùng thoát
     */
    private fun showAreYouSureDialog() {
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
            .setTitle(getString(R.string.exit_dialog_title))
            .setMessage(getString(R.string.exit_dialog_message))

        dialogBuilder.setPositiveButton(getString(R.string.btn_cancel)) { _, _ ->
            dialogBuilder.setCancelable(true)
        }

        dialogBuilder.setNegativeButton(getString(R.string.btn_stop_test)) { _, _ ->
            findNavController().navigateUp()
        }

        val helpDialog: AlertDialog = dialogBuilder.create()
        helpDialog.show()
    }

    /**
     * Xử lý nút Up !!!
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        CommonMethods.showHelpDialog(
            context = requireContext(),
            title = getString(R.string.oops_dialog_title),
            message = getString(R.string.oops_dialog_message)
        )
        return findNavController().navigateUp()
    }

    /**
     * Hiển thị tvTimer và bắt đầu đếm ngược
     */
    private fun startCountDown() {
        binding.tvTimer.visibility = View.VISIBLE

        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                timeLeftInMillis = TIME_OUT
                updateCountDownText()

                countDownTimer.cancel()
                binding.pbSubmitTest.visibility = View.VISIBLE
                viewModel.onSubmitTest(topicId = args.topicId)
            }
        }.start()
    }

    /**
     * Cập nhật văn bản đếm ngược dựa trên thời gian còn lại tính bằng mili giây
     */
    private fun updateCountDownText() {
        val minutes = ((timeLeftInMillis / 1000) / 60).toInt()
        val seconds = ((timeLeftInMillis / 1000) % 60).toInt()

        val timeFormatted = String.format(
            Locale.getDefault(), FORMAT_COUNT_DOWN_TIMER, minutes, seconds
        )
        binding.tvTimer.text = timeFormatted

        if (timeLeftInMillis < ONE_MINUTE_IN_MILLIS) {
            binding.tvTimer.setTextColor(Color.RED)
        } else {
            binding.tvTimer.setTextColor(Color.WHITE)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }

        // reset screen orientation in order to rotate other fragments inside Main Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        _binding = null
    }
}