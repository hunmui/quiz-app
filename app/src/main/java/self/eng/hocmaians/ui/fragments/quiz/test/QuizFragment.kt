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
 * DOES NOT allow user to rotate screen in this fragment
 *
 * get Current Screen Rotation:
 * https://stackoverflow.com/questions/10380989/how-do-i-get-the-current-orientation-activityinfo-screen-orientation-of-an-a
 *
 * keep current orientation:
 * https://stackoverflow.com/questions/51710304/set-landscape-orientation-for-fragment-in-single-activity-architecture
 */
@AndroidEntryPoint
class QuizFragment : Fragment(R.layout.fragment_quiz) {

    // view binding
    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    // view model
    private val viewModel: QuizViewModel by viewModels()

    // containing all passed arguments
    private val args: QuizFragmentArgs by navArgs()

    // adapters
    private lateinit var quizAdapter: QuizAdapter
    private lateinit var checkAnswersAdapter: CheckAnswersAdapter

    private var isCheckAnswersOpened = false

    // current screen orientation
    private var currentOrientation: Int = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    // timer for practice test
    private lateinit var countDownTimer: CountDownTimer
    private var timeLeftInMillis: Long = Constants.PRACTICE_TEST_TIMER_IN_MILLIS

    /**
     * Handles event when user click the back button on the device
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
     * Get the current screen rotation.
     *
     * @return the current screen rotation.
     */
    private fun getScreenOrientation(): Int {
        val rotation: Int = activity?.windowManager?.defaultDisplay!!.rotation
        val dm = DisplayMetrics()
        activity?.windowManager!!.defaultDisplay.getMetrics(dm)
        val width = dm.widthPixels
        val height = dm.heightPixels

        // if the device's natural orientation is portrait:
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
     * Save the current orientation
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

        // fix screen orientation for all fragments in MainActivity
        activity?.requestedOrientation = currentOrientation

        setupCheckAnswerRecyclerView()

        // only initialize if first time this fragment gets created
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

        // start timer if this is practice test
        if (args.courseName == COURSE_NAMES[3]) {
            startCountDown()
        }

        setSomeTextOnTop()

        viewModel.answers.observe(viewLifecycleOwner) { answers ->
            checkAnswersAdapter.differ.submitList(answers)
        }

        binding.apply {
            // open OR close check answers recyclerView
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
     * Observe the status of saving user answers and score.
     */
    private fun subscribeToObserver() {
        viewModel.save.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        binding.pbSubmitTest.visibility = View.GONE

                        // changing fragment, pass required arguments
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
     * Set up adapter for viewPager
     *
     * @param questions a list of questions
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
     * Set course name and topic name
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
     * Set up check answer recycler view, and set a click listener on its adapter
     */
    private fun setupCheckAnswerRecyclerView() {
        checkAnswersAdapter = CheckAnswersAdapter()

        binding.rvCheckAnswers.apply {
            adapter = checkAnswersAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        // log the viewPager's page that the user want to navigate
        checkAnswersAdapter.setOnCheckAnswerClickListener { viewPagerPosition ->
            onAnswerClick(position = viewPagerPosition)
        }
    }

    /**
     * Set visibility of check answers recycler view, and change btnCheckAnswers text
     */
    private fun setVisibilityOfCheckAnswers() {
        if (isCheckAnswersOpened) {
            showRecyclerView()
        } else {
            hideRecyclerView()
        }
    }

    /**
     * Log user's answer (newly answered or change answer). Then automatically move to next question,
     * only if that question is newly answered.
     *
     * @param answeredPosition user's answer.
     */
    private fun onChecked(answeredPosition: Int) {

        binding.apply {
            // get current position of the ViewPager2 (position start from 0)
            checkAnswersAdapter.notifyItemChanged(viewPagerQuiz.currentItem)

            val nextPage = viewModel.onAnswerQuestion(
                currentPosition = viewPagerQuiz.currentItem,
                answerPosition = answeredPosition
            )
            setDoneQuantityText(viewModel.doneQuantity)

            // move to next page
            if ((nextPage == (viewPagerQuiz.currentItem + 1)) &&
                (nextPage != viewPagerQuiz.adapter?.itemCount)
            ) {
                viewPagerQuiz.setCurrentItem(nextPage, false)
            }
        }
    }

    /**
     * What to do when user click an answer in check Answer recycler view: close the recycler view,
     * and move viewPager to the corresponding question.
     *
     * @param position position of question
     */
    private fun onAnswerClick(position: Int) {
        isCheckAnswersOpened = false

        binding.apply {
            viewPagerQuiz.setCurrentItem(position, false)
            hideRecyclerView()
        }
    }

    /**
     * Set the done quantity text.
     *
     * @param doneQuantity how many question has the user done so far.
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
     * Show this dialog to user to confirm his decision
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
     * Handle the Up button in a funny way!!!
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
     * Make tvTimer visible and start the count down
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
     * Update count down text based on the time left in millisecond
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