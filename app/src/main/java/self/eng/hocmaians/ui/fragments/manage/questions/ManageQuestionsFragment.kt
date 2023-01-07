package self.eng.hocmaians.ui.fragments.manage.questions

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import self.eng.hocmaians.R
import self.eng.hocmaians.databinding.FragmentManageQuestionsBinding
import self.eng.hocmaians.ui.fragments.bookmark.BookmarksAdapter
import self.eng.hocmaians.util.Constants.ACTION_ADD_QUESTION
import self.eng.hocmaians.util.Constants.ACTION_EDIT_QUESTION

@AndroidEntryPoint
class ManageQuestionsFragment : Fragment(R.layout.fragment_manage_questions) {

    // view binding
    private var _binding: FragmentManageQuestionsBinding? = null
    private val binding get() = _binding!!

    // view model
    private val viewModel: ManageQuestionsViewModel by viewModels()

    // passed args
    private val args: ManageQuestionsFragmentArgs by navArgs()

    // adapter
    private lateinit var manageQuestionsAdapter: BookmarksAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentManageQuestionsBinding.bind(view)

        setupAllQuestionsRecyclerView()
        setHeaderText()

//        viewModel.getQuestionsByTopic(args.topicId).observe(viewLifecycleOwner) { questions ->
//            manageQuestionsAdapter.differ.submitList(questions)
//        }

        // production code
        viewModel.getUserAddedQuestionsByTopic(
            topicId = args.topicId
        ).observe(viewLifecycleOwner, { questions ->
            manageQuestionsAdapter.differ.submitList(questions)

            if (questions.isEmpty()) {
                binding.tvNoQuestions.visibility = View.VISIBLE
            }
        })

        // add new question
        binding.fabAddQuestion.setOnClickListener {
            val action = ManageQuestionsFragmentDirections
                .actionManageQuestionsFragmentToAddEditQuestionFragment(
                    questionAction = ACTION_ADD_QUESTION,
                    question = null,
                    topicId = args.topicId
                )
            findNavController().navigate(action)
        }

        // edit question
        manageQuestionsAdapter.setOnQuestionClickListener { question ->
            val action = ManageQuestionsFragmentDirections
                .actionManageQuestionsFragmentToAddEditQuestionFragment(
                    questionAction = ACTION_EDIT_QUESTION,
                    question = question,
                    topicId = args.topicId
                )
            findNavController().navigate(action)
        }
    }

    /**
     * Set up all questions recycler view
     */
    private fun setupAllQuestionsRecyclerView() {
        manageQuestionsAdapter = BookmarksAdapter()

        binding.rvAllQuestions.apply {
            adapter = manageQuestionsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    /**
     * Set header text for the fragment
     */
    private fun setHeaderText() {
        val headerString = "${getString(R.string.tv_all_questions_1)} ${args.topicName} " +
                "${getString(R.string.tv_all_questions_2)} ${args.courseName}"
        binding.tvAllQuestions.text = headerString
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}