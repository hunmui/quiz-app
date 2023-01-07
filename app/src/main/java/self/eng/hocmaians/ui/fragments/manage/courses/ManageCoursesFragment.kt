package self.eng.hocmaians.ui.fragments.manage.courses

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import self.eng.hocmaians.R
import self.eng.hocmaians.databinding.FragmentManageCoursesBinding
import self.eng.hocmaians.util.Constants.PRACTICE_TEST_COURSE_ID

@AndroidEntryPoint
class ManageCoursesFragment : Fragment(R.layout.fragment_manage_courses) {

    // view binding
    private var _binding: FragmentManageCoursesBinding? = null
    private val binding get() = _binding!!

    // view model
    private val viewModel: ManageCoursesViewModel by viewModels()

    // adapter
    private lateinit var manageCoursesAdapter: ManageCoursesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentManageCoursesBinding.bind(view)

        setupAllCoursesRecyclerView()

        viewModel.courses.observe(viewLifecycleOwner) { courses ->
            manageCoursesAdapter.differ.submitList(courses)
        }

        // to manage topics fragment
        manageCoursesAdapter.setOnCourseClickListener { courseId, courseName ->

            // production code
            if (courseId == PRACTICE_TEST_COURSE_ID) {
                Toast.makeText(
                    requireContext(),
                    R.string.cannot_manage_practice_test,
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val action = ManageCoursesFragmentDirections
                    .actionManageCoursesFragmentToManageTopicsFragment(
                        courseId = courseId,
                        courseName = courseName
                    )
                findNavController().navigate(action)
            }

//            // still adding data code
//            val action = ManageCoursesFragmentDirections
//                .actionManageCoursesFragmentToManageTopicsFragment(
//                    courseId = courseId,
//                    courseName = courseName
//                )
//            findNavController().navigate(action)
        }
    }

    /**
     * Set up adapters and few attributes for courses recycler view
     */
    private fun setupAllCoursesRecyclerView() {
        manageCoursesAdapter = ManageCoursesAdapter()

        binding.rvAllCourses.apply {
            adapter = manageCoursesAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}