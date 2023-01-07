package self.eng.hocmaians.ui.fragments.manage.topics

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
import self.eng.hocmaians.data.entities.Topic
import self.eng.hocmaians.databinding.FragmentAddEditTopicBinding
import self.eng.hocmaians.util.Constants
import self.eng.hocmaians.util.Constants.MAX_TOPIC_NAME_LENGTH
import self.eng.hocmaians.util.Status

@AndroidEntryPoint
class AddEditTopicFragment : Fragment(R.layout.fragment_add_edit_topic) {

    // view binding
    private var _binding: FragmentAddEditTopicBinding? = null
    private val binding get() = _binding!!

    // view model
    private val viewModel: ManageTopicsViewModel by viewModels()

    // passed args
    private val args: AddEditTopicFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddEditTopicBinding.bind(view)

        subscribeToObserver()

        binding.apply {

            // wait until all topic names are fully loaded
            btnAddEditTopic.isEnabled = false

            edtTopicName.doOnTextChanged { text, _, _, _ ->
                if (text!!.length > MAX_TOPIC_NAME_LENGTH) {
                    binding.tilTopicName.error = getString(R.string.topic_name_exceed_max_character)
                } else {
                    binding.tilTopicName.error = null
                }
            }
        }

        viewModel.getTopicsNamesByCourse(args.courseId).observe(
            viewLifecycleOwner, { topicNames ->
                binding.apply {
                    btnAddEditTopic.isEnabled = true

                    if (args.topicAction == Constants.ACTION_ADD_TOPIC) {
                        // if this is adding topic:
                        // 1, Change the title to "Add topic"
                        // 2, Show add topic button and set a click listener on it to add topic

                        btnAddEditTopic.apply {
                            text = getString(R.string.tv_add_topic)

                            setOnClickListener {
                                addTopic(topicNames)
                            }
                        }

                        tvAddTopicHome.text = getString(R.string.tv_add_topic)
                    } else {
                        // if this is editing topic:
                        // 1, Change the title to "Edit topic"
                        // 2, Show delete topic button
                        // 3, Fill the edit text with topic name
                        // 4, Show edit topic button, and set a click listener on it to edit topic

                        tvAddTopicHome.text = getString(R.string.tv_edit_topic)

                        edtTopicName.setText(args.topic?.name)

                        btnAddEditTopic.apply {
                            text = getString(R.string.tv_edit_topic)

                            setOnClickListener {
                                editTopic(topicNames)
                            }
                        }

                        btnDeleteTopic.apply {
                            visibility = View.VISIBLE

                            setOnClickListener {
                                deleteTopic(topic = args.topic!!)
                            }
                        }
                    }
                }
            })
    }

    /**
     * Observe the state of adding, updating or deleting a topic
     */
    private fun subscribeToObserver() {
        viewModel.insertTopicStatus.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.add_topic_successfully,
                            Toast.LENGTH_LONG
                        ).show()

                        binding.edtTopicName.setText("")
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

        viewModel.updateTopicStatus.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.edit_topic_successfully,
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

        viewModel.deleteTopicStatus.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        binding.pbDeleteTopic.visibility = View.GONE

                        Toast.makeText(
                            requireContext(),
                            R.string.delete_topic_successfully,
                            Toast.LENGTH_SHORT
                        ).show()

                        findNavController().navigateUp()
                    }
                    Status.ERROR -> {
                        /* NO-OP */
                    }
                    Status.LOADING -> {
                        /* NO-OP */
                    }
                }
            }
        })
    }

    /**
     * Add topic. After that, make edit text field empty so that user can add the next topic
     */
    private fun addTopic(topicNames: List<String>) {
        viewModel.insertTopic(
            topicName = binding.edtTopicName.text.toString(),
            existingTopicNames = topicNames,
            courseId = args.courseId
        )
    }

    /**
     * Edit topic name. After that, return to ManageTopicsFragment to load updated topic.
     */
    private fun editTopic(topicNames: List<String>) {
        args.topic?.let {
            viewModel.updateTopic(
                topicId = it.id,
                topicName = binding.edtTopicName.text.toString(),
                existingTopicNames = topicNames
            )
        }
    }

    /**
     * Delete topic and its associated stuff (questions, answers and scores). Then return to
     * ManageTopicsFragment.
     *
     * @param topic the topic to delete
     */
    private fun deleteTopic(topic: Topic) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_topic_dialog_title))
            .setMessage(getString(R.string.delete_topic_dialog_message))
            .setPositiveButton(getString(R.string.btn_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.tv_delete_topic)) { _, _ ->
                binding.pbDeleteTopic.visibility = View.VISIBLE
                viewModel.deleteTopic(topic = topic)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}