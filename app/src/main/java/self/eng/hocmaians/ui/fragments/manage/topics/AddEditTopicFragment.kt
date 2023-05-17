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

            // đợi cho đến khi tất cả các tên chủ đề được tải đầy đủ
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
                        // nếu đây là thêm chủ đề:
                        // 1, Đổi tiêu đề thành "Thêm chủ đề"
                        // 2, Hiển thị nút thêm chủ đề và đặt trình nghe nhấp chuột vào đó để thêm chủ đề

                        btnAddEditTopic.apply {
                            text = getString(R.string.tv_add_topic)

                            setOnClickListener {
                                addTopic(topicNames)
                            }
                        }

                        tvAddTopicHome.text = getString(R.string.tv_add_topic)
                    } else {
                        // nếu đây là chủ đề chỉnh sửa:
                        // 1, Đổi tiêu đề thành "Sửa chủ đề"
                        // 2, Hiện nút xóa chủ đề
                        // 3, Điền vào văn bản chỉnh sửa với tên chủ đề
                        // 4, Hiển thị nút chỉnh sửa chủ đề và đặt trình nghe nhấp chuột vào đó để chỉnh sửa chủ đề

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
     * Quan sát trạng thái thêm, cập nhật hoặc xóa chủ đề
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
     * Thêm chủ đề. Sau đó, để trống trường văn bản chỉnh sửa để người dùng có thể thêm chủ đề tiếp theo
     */
    private fun addTopic(topicNames: List<String>) {
        viewModel.insertTopic(
            topicName = binding.edtTopicName.text.toString(),
            existingTopicNames = topicNames,
            courseId = args.courseId
        )
    }

    /**
     * Sửa tên chủ đề. Sau đó, quay lại ManageTopicsFragment để tải chủ đề đã cập nhật.
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
     * Xóa chủ đề và nội dung liên quan (câu hỏi, câu trả lời và điểm số). Sau đó quay trở lại
     * ManageTopicsFragment.
     *
     * @param topic chủ đề cần xóa
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