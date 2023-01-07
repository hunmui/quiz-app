package self.eng.hocmaians.ui.fragments.manage.questions

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import self.eng.hocmaians.R
import self.eng.hocmaians.data.entities.Question
import self.eng.hocmaians.databinding.FragmentAddEditQuestionBinding
import self.eng.hocmaians.util.CommonMethods
import self.eng.hocmaians.util.Constants
import self.eng.hocmaians.util.Status

@AndroidEntryPoint
class AddEditQuestionFragment : Fragment(R.layout.fragment_add_edit_question) {

    // view binding
    private var _binding: FragmentAddEditQuestionBinding? = null
    private val binding get() = _binding!!

    // view model
    private val viewModel: ManageQuestionsViewModel by viewModels()

    // passed args
    private val args: AddEditQuestionFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddEditQuestionBinding.bind(view)

        subscribeToObserver()

        binding.edtCorrectAnswer.doOnTextChanged { text, _, _, _ ->
            text?.let {
                if (it.length > 1 ||
                    (it.length == 1 && !viewModel.listOfValidAnswer.contains(
                        it.toString().lowercase()
                    ))
                ) {
                    binding.tilCorrectAnswer.error = getString(R.string.add_correct_answer_msg)
                } else {
                    binding.tilCorrectAnswer.error = null
                }
            }
        }

        if (args.questionAction == Constants.ACTION_ADD_QUESTION) {
            // if this is adding question:
            // 1, Change the title to "Add question"
            // 2, show the corresponding help message
            // 3, Do not show delete button
            // 4, Show add question button and set a click listener on it to add question

            binding.apply {
                tvAddEditQuestion.text = getString(R.string.tv_add_question)

                btnDeleteQuestion.visibility = View.GONE

                btnAddEditQuestion.apply {
                    text = getString(R.string.tv_add_question)

                    setOnClickListener {
                        addQuestion()
                    }
                }
            }
        } else {
            // if this is editing question:
            // 1, Change the title to "Edit question"
            // 2, Hide text prefix in tilFirstOption, tilSecondOption, tilThirdOption, tilFourthOption
            // 3, Fill the edit text with question attributes
            // 4, Show delete button, and set a click listener on it to delete question
            // 5, Show edit question button, and set a click listener on it to edit question

            binding.apply {
                tvAddEditQuestion.text = getString(R.string.tv_edit_question)

                setNoPrefix()
                setTextToTextFields(args.question!!)

                btnDeleteQuestion.apply {
                    visibility = View.VISIBLE

                    setOnClickListener {
                        deleteQuestion()
                    }
                }

                btnAddEditQuestion.apply {
                    text = getString(R.string.tv_edit_question)

                    setOnClickListener {
                        editQuestion()
                    }
                }
            }
        }
    }

    private fun subscribeToObserver() {
        viewModel.insertQuestionStatus.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->

                binding.pbManipulateQuestion.visibility = View.GONE

                when (result.status) {
                    Status.SUCCESS -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.add_question_successfully,
                            Toast.LENGTH_LONG
                        ).show()

                        resetAllTextFields()
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
        })

        viewModel.updateQuestionStatus.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->

                binding.pbManipulateQuestion.visibility = View.GONE

                when (result.status) {
                    Status.SUCCESS -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.edit_question_successfully,
                            Toast.LENGTH_LONG
                        ).show()

                        findNavController().navigateUp()
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
        })

        viewModel.deleteQuestionStatus.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->

                binding.pbManipulateQuestion.visibility = View.GONE

                when (result.status) {
                    Status.SUCCESS -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.delete_question_successfully,
                            Toast.LENGTH_LONG
                        ).show()

                        findNavController().navigateUp()
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
        })
    }

    private fun addQuestion() {
        binding.apply {

            pbManipulateQuestion.visibility = View.VISIBLE

            viewModel.insertQuestion(
                quiz = edtQuestion.text.toString(),
                option1 = "${tilFirstOption.prefixText} ${edtFirstOption.text.toString()}",
                option2 = "${tilSecondOption.prefixText} ${edtSecondOption.text.toString()}",
                option3 = "${tilThirdOption.prefixText} ${edtThirdOption.text.toString()}",
                option4 = "${tilFourthOption.prefixText} ${edtFourthOption.text.toString()}",
                correctAnswer = edtCorrectAnswer.text.toString(),
                explanation = edtExplanation.text.toString(),
                topicId = args.topicId
            )
        }
    }

    private fun editQuestion() {
        binding.apply {

            pbManipulateQuestion.visibility = View.VISIBLE

            args.question?.let {
                viewModel.editQuestion(
                    question = it,
                    quiz = edtQuestion.text.toString(),
                    option1 = edtFirstOption.text.toString(),
                    option2 = edtSecondOption.text.toString(),
                    option3 = edtThirdOption.text.toString(),
                    option4 = edtFourthOption.text.toString(),
                    correctAnswer = edtCorrectAnswer.text.toString(),
                    explanation = edtExplanation.text.toString()
                )
            }
        }
    }

    private fun deleteQuestion() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_question_dialog_title))
            .setMessage(getString(R.string.delete_question_dialog_message))
            .setPositiveButton(getString(R.string.btn_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.btn_delete_question)) { _, _ ->
                binding.pbManipulateQuestion.visibility = View.VISIBLE

                args.question?.let {
                    viewModel.deleteQuestionAndAnswers(question = it)
                }
            }
            .show()
    }

    private fun setNoPrefix() {
        binding.apply {
            tilFirstOption.prefixText = null
            tilSecondOption.prefixText = null
            tilThirdOption.prefixText = null
            tilFourthOption.prefixText = null
        }
    }

    private fun setTextToTextFields(question: Question) {
        binding.apply {
            edtQuestion.setText(question.question)
            edtFirstOption.setText(question.option1)
            edtSecondOption.setText(question.option2)
            edtThirdOption.setText(question.option3)
            edtFourthOption.setText(question.option4)
            edtCorrectAnswer.setText(CommonMethods.convertIndexToText(question.answerNr))
            edtExplanation.setText(question.explanation)
        }
    }

    private fun resetAllTextFields() {
        binding.apply {
            edtQuestion.setText("")
            edtFirstOption.setText("")
            edtSecondOption.setText("")
            edtThirdOption.setText("")
            edtFourthOption.setText("")
            edtCorrectAnswer.setText("")
            edtExplanation.setText("")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}