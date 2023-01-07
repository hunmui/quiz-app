package self.eng.hocmaians.ui.fragments.quiz.test

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import self.eng.hocmaians.R
import self.eng.hocmaians.data.entities.Question
import self.eng.hocmaians.databinding.FragmentEachQuizBinding
import self.eng.hocmaians.util.Constants.KEY_CURRENT_POSITION
import self.eng.hocmaians.util.Constants.KEY_CURRENT_QUESTION
import self.eng.hocmaians.util.Constants.UN_INITIALIZE_POSITION

/**
 * Holder fragment for viewPager in QuizFragment.
 */
class EachQuizFragment : Fragment(R.layout.fragment_each_quiz) {

    private var _binding: FragmentEachQuizBinding? = null
    private val binding get() = _binding!!

    private var question: Question? = null
    private var questionPosition: Int = UN_INITIALIZE_POSITION

    /**
     * Initialize variables for this fragment
     *
     * @param currentQuestion current question
     * @param pos current question position
     */
    fun init(currentQuestion: Question, pos: Int) {
        question = currentQuestion
        questionPosition = pos
    }

    /* ----------- Click listener on radio buttons ----------- */
    private var onRadioButtonClickListener: ((Int, Int) -> Unit)? = null

    fun setOnRadioButtonClickListener(listener: (Int, Int) -> Unit) {
        onRadioButtonClickListener = listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEachQuizBinding.bind(view)

        if (savedInstanceState != null) {
            questionPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION)
            question = savedInstanceState.getParcelable(KEY_CURRENT_QUESTION)
        }

        if (questionPosition == UN_INITIALIZE_POSITION || question == null) {
            onDestroy()
        } else {
            bindingViews()
        }
    }

    private fun bindingViews() {
        binding.apply {

            val questionText = "${questionPosition + 1}: ${question!!.question}"
            tvQuestion.text = questionText

            rbOption1.text = question!!.option1
            rbOption2.text = question!!.option2
            rbOption3.text = question!!.option3
            rbOption4.text = question!!.option4

            // set Click lister on radio buttons
            rgOptions.setOnCheckedChangeListener { _, checkedId ->
                val answerNumber = when (checkedId) {
                    R.id.rb_option_1 -> {
                        rbOption1.jumpDrawablesToCurrentState()
                        1
                    }
                    R.id.rb_option_2 -> {
                        rbOption2.jumpDrawablesToCurrentState()
                        2
                    }
                    R.id.rb_option_3 -> {
                        rbOption3.jumpDrawablesToCurrentState()
                        3
                    }
                    else -> {
                        rbOption4.jumpDrawablesToCurrentState()
                        4
                    }
                }

                this@EachQuizFragment.onRadioButtonClickListener?.let {
                    it(answerNumber, questionPosition)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CURRENT_POSITION, questionPosition)
        outState.putParcelable(KEY_CURRENT_QUESTION, question)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}