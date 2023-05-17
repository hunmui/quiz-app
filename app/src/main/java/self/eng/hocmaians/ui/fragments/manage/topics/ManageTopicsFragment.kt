package self.eng.hocmaians.ui.fragments.manage.topics

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import self.eng.hocmaians.R
import self.eng.hocmaians.databinding.FragmentManageTopicsBinding
import self.eng.hocmaians.util.Constants.ACTION_ADD_TOPIC
import self.eng.hocmaians.util.Constants.ACTION_EDIT_TOPIC

@AndroidEntryPoint
class ManageTopicsFragment : Fragment(R.layout.fragment_manage_topics) {

    // view binding
    private var _binding: FragmentManageTopicsBinding? = null
    private val binding get() = _binding!!

    // view model
    private val viewModel: ManageTopicsViewModel by viewModels()

    // passed args
    private val args: ManageTopicsFragmentArgs by navArgs()

    // adapter
    private lateinit var manageTopicsAdapter: ManageTopicsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentManageTopicsBinding.bind(view)

        setupAllTopicsRecyclerView()
        setHeaderText()

        viewModel.getTopicsByCourse(args.courseId).observe(viewLifecycleOwner) { topics ->
            manageTopicsAdapter.differ.submitList(topics)
        }

        // add topic
        binding.fabAddTopic.setOnClickListener {
            val action = ManageTopicsFragmentDirections
                .actionManageTopicsFragmentToAddEditTopicFragment(
                    topicAction = ACTION_ADD_TOPIC,
                    topic = null,
                    courseId = args.courseId
                )
            findNavController().navigate(action)
        }

        // edit topic
        manageTopicsAdapter.setOnEditTopicListener { topic ->
            val action = ManageTopicsFragmentDirections
                .actionManageTopicsFragmentToAddEditTopicFragment(
                    topicAction = ACTION_EDIT_TOPIC,
                    topic = topic,
                    courseId = args.courseId
                )
            findNavController().navigate(action)
        }

        // đến manage questions fragment
        manageTopicsAdapter.setOnTopicClickListener { topicId, topicName ->
            val action = ManageTopicsFragmentDirections
                .actionManageTopicsFragmentToManageQuestionsFragment(
                    topicId = topicId,
                    topicName = topicName,
                    courseName = args.courseName
                )
            findNavController().navigate(action)
        }
    }

    /**
     * Thiết lập bộ điều hợp và một vài thuộc tính cho chế độ xem trình tái chế chủ đề
     */
    private fun setupAllTopicsRecyclerView() {
        manageTopicsAdapter = ManageTopicsAdapter()

        binding.rvAllTopics.apply {
            adapter = manageTopicsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    /**
     * Đặt văn bản tiêu đề của fragment
     */
    private fun setHeaderText() {
        val headerString = "${getString(R.string.tv_all_topics)} ${args.courseName}"
        binding.tvAllTopics.text = headerString
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}