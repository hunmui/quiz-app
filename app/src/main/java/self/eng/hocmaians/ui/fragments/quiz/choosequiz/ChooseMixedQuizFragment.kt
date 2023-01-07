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
import self.eng.hocmaians.databinding.FragmentChooseMixedQuizBinding
import self.eng.hocmaians.util.CommonMethods
import self.eng.hocmaians.util.Constants.MIXED_COURSE_NAME
import self.eng.hocmaians.util.Constants.MIXED_TOPIC_ID
import self.eng.hocmaians.util.Constants.MIXED_TOPIC_NAME
import self.eng.hocmaians.util.Constants.QUANTITY_LIST
import self.eng.hocmaians.util.Status

@AndroidEntryPoint
class ChooseMixedQuizFragment : Fragment(R.layout.fragment_choose_mixed_quiz) {

    // view binding
    private var _binding: FragmentChooseMixedQuizBinding? = null
    private val binding get() = _binding!!

    // view model
    private val viewModel: ChooseMixedQuizViewModel by viewModels()

    // toast holder
    private var toastHolder: Toast? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChooseMixedQuizBinding.bind(view)

        subscribeToObserver()

        // only count all questions if first time this fragment get created
        if (savedInstanceState == null) {
            viewModel.allQuestions.observe(viewLifecycleOwner) { totalQuestions ->
                viewModel.totalQuestionsInDb = totalQuestions
            }
        }

        setupChooseQuantitySpinner()

        binding.apply {
            // help alert dialog
            ivChooseQuantityHelp.setOnClickListener {
                CommonMethods.showHelpDialog(
                    context = requireContext(),
                    title = getString(R.string.choose_quantity_help_title),
                    message = getString(R.string.choose_quantity_help_msg)
                )
            }

            // start mixed quiz
            btnStartMixedQuiz.setOnClickListener {
                viewModel.onStartTest()
            }
        }
    }

    /**
     * Set up Choose Quantity Spinner. Update chosenQuantity when user choose a quantity.
     */
    private fun setupChooseQuantitySpinner() {
        val quantityAdapter: ArrayAdapter<Int> = ArrayAdapter(
            requireContext(),
            R.layout.spinner_display_text,
            QUANTITY_LIST
        )
        quantityAdapter.setDropDownViewResource(R.layout.each_spinner_text_view)

        binding.spinnerChooseQuantity.apply {
            adapter = quantityAdapter

            // in case of screen rotation
            viewModel.chosenQuantity?.let {
                this.setSelection(quantityAdapter.getPosition(it))
            }

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val chosenQuantity = parent?.selectedItem as Int
                    viewModel.onChooseQuantity(quantity = chosenQuantity)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
    }

    /**
     * Observe start test status
     */
    private fun subscribeToObserver() {
        viewModel.startTestStatus.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        // changing fragment, pass required arguments
                        val action = HomeFragmentDirections.actionHomeFragmentToQuizFragment(
                            courseName = MIXED_COURSE_NAME,
                            topicId = MIXED_TOPIC_ID,
                            topicName = MIXED_TOPIC_NAME,
                            quizAmount = viewModel.chosenQuantity!!
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
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        toastHolder?.cancel()
    }
}