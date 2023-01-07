package self.eng.hocmaians.ui.fragments.bookmark

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import self.eng.hocmaians.R
import self.eng.hocmaians.data.entities.Course
import self.eng.hocmaians.data.entities.Question
import self.eng.hocmaians.data.entities.Topic
import self.eng.hocmaians.databinding.FragmentBookmarksBinding
import self.eng.hocmaians.util.CommonMethods
import self.eng.hocmaians.util.Constants.QUESTION_NOT_BOOKMARKED
import self.eng.hocmaians.util.Status

@AndroidEntryPoint
class BookmarksFragment : Fragment(R.layout.fragment_bookmarks) {

    // view binding
    private var _binding: FragmentBookmarksBinding? = null
    private val binding get() = _binding!!

    // view model
    private val viewModel: BookmarksViewModel by viewModels()

    // bookmarks adapter
    private lateinit var bookmarksAdapter: BookmarksAdapter

    private var noCourses = false
    private var noTopics = false

    // toast holder
    private var toastHolder: Toast? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBookmarksBinding.bind(view)

        subscribeToObserver()

        setupBookmarkRecyclerView()

        // get all bookmarks only if first time this fragment gets created
        if (savedInstanceState == null) {
            viewModel.getAllBookmarks()
        }

        viewModel.bookmarks.observe(viewLifecycleOwner) { bookmarks ->
            bookmarksAdapter.differ.submitList(bookmarks)

            if (bookmarks.isEmpty()) {
                binding.tvNoBookmarkFound.visibility = View.VISIBLE
            } else {
                binding.tvNoBookmarkFound.visibility = View.GONE
            }
        }

        // load courses, and topic to spinner to filter bookmarks
        viewModel.courses.observe(viewLifecycleOwner) { courses ->
            if (courses.isEmpty()) {
                noCourses = true
                binding.spinnerChooseCourseFilter.adapter = null
            } else {
                loadCoursesToSpinner(courses = courses)
            }
        }

        // show or hide filter layout
        binding.tvBookmarksFilter.setOnClickListener {
            viewModel.isFilterSectionOpen = !viewModel.isFilterSectionOpen
            if (viewModel.isFilterSectionOpen) {
                showFilterLayout()
            } else {
                hideFilterLayout()
            }
        }

        if (viewModel.isFilterSectionOpen) {
            showFilterLayout()
        } else {
            hideFilterLayout()
        }

        setBookmarksDescription()

        // filtering
        binding.btnFilterBookmarks.setOnClickListener {
            when {
                noCourses -> {
                    toastHolder = Toast.makeText(
                        requireContext(),
                        R.string.no_courses_to_filter,
                        Toast.LENGTH_LONG
                    )
                    toastHolder?.show()
                }
                noTopics -> {
                    toastHolder = Toast.makeText(
                        requireContext(),
                        R.string.no_topics_to_filter,
                        Toast.LENGTH_LONG
                    )
                    toastHolder?.show()
                }
                else -> {
                    viewModel.onFilterBookmarks()
                }
            }
        }

        swipeToDeleteBookMark()
    }

    /**
     * Observe filter bookmark state
     */
    private fun subscribeToObserver() {
        viewModel.filter.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        setBookmarksDescription()

                        viewModel.bookmarks.observe(viewLifecycleOwner) { bookmarks ->
                            bookmarksAdapter.differ.submitList(bookmarks)

                            if (bookmarks.isEmpty()) {
                                binding.tvNoBookmarkFound.visibility = View.VISIBLE
                            } else {
                                binding.tvNoBookmarkFound.visibility = View.GONE
                            }
                        }
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
        }
    }

    /**
     * Set up bookmarks recycler view
     */
    private fun setupBookmarkRecyclerView() {
        bookmarksAdapter = BookmarksAdapter()

        binding.rvAllBookmarks.apply {
            adapter = bookmarksAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    /**
     * Load all course from the Courses table in the database.
     * Let the spinner display all courses, then load all topics which are corresponding to
     * the chosen course.
     *
     * @param courses a list of all courses available
     */
    private fun loadCoursesToSpinner(courses: List<Course>) {

        val courseAdapter: ArrayAdapter<Course> = ArrayAdapter(
            requireContext(),
            R.layout.spinner_display_text,
            courses
        )
        courseAdapter.setDropDownViewResource(R.layout.each_spinner_text_view)
        binding.spinnerChooseCourseFilter.adapter = courseAdapter

        // load the chosen course in case of screen rotation
        viewModel.chosenCourse?.let {
            binding.spinnerChooseCourseFilter.setSelection(courseAdapter.getPosition(it))
        }

        binding.spinnerChooseCourseFilter.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val chosenCourse = parent?.selectedItem as Course
                    viewModel.onChooseCourse(course = chosenCourse)
                    viewModel.topicsByCourse.observe(viewLifecycleOwner) { topics ->
                        if (topics.isEmpty()) {
                            binding.spinnerChooseTopicFilter.adapter = null
                            noTopics = true
                        } else {
                            loadTopicsToSpinner(topics = topics)
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
    }

    /**
     * Loads all topics from the Topic table in the database, based on the chosen Course.
     * Let the spinner display all the selected topics. Then show total number of questions that are
     * available in the selected topic.
     *
     * @param topics a list of all topics based on the chosen course
     */
    private fun loadTopicsToSpinner(topics: List<Topic>) {

        val topicAdapter: ArrayAdapter<Topic> = ArrayAdapter(
            requireContext(),
            R.layout.spinner_display_text,
            topics
        )
        topicAdapter.setDropDownViewResource(R.layout.each_spinner_text_view)
        binding.spinnerChooseTopicFilter.adapter = topicAdapter

        // load the chosen topic in case of screen rotation
        viewModel.chosenTopic.let {
            binding.spinnerChooseTopicFilter.setSelection(topicAdapter.getPosition(it))
        }

        binding.spinnerChooseTopicFilter.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val chosenTopic = parent?.selectedItem as Topic
                    viewModel.onChooseTopic(topic = chosenTopic)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
    }

    /**
     * Show filter bookmark section, and show the corresponding help message
     */
    private fun showFilterLayout() {
        binding.apply {
            filterLayout.visibility = View.VISIBLE

            ivFilterBookmarksHelp.setOnClickListener {
                CommonMethods.showHelpDialog(
                    context = requireContext(),
                    title = getString(R.string.filter_bookmarks_help_title),
                    message = getString(R.string.visible_filter_bookmarks_help_msg)
                )
            }
        }
    }

    /**
     * Hide filter bookmark section, and show the corresponding help message
     */
    private fun hideFilterLayout() {
        binding.apply {
            filterLayout.visibility = View.GONE

            ivFilterBookmarksHelp.setOnClickListener {
                CommonMethods.showHelpDialog(
                    context = requireContext(),
                    title = getString(R.string.filter_bookmarks_help_title),
                    message = getString(R.string.hidden_filter_bookmarks_help_msg)
                )
            }
        }
    }

    /**
     * In Recycler view, swipe a bookmark to remove it from the list
     */
    private fun swipeToDeleteBookMark() {
        val itemTouchHelperCallBack = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val question: Question = bookmarksAdapter.differ.currentList[position]

                viewModel.updateQuestionBookmark(
                    questionId = question.id,
                    bookmark = QUESTION_NOT_BOOKMARKED
                )

                toastHolder = Toast.makeText(
                    requireContext(),
                    R.string.bookmark_removed_success,
                    Toast.LENGTH_SHORT
                )
                toastHolder?.show()
            }
        }

        ItemTouchHelper(itemTouchHelperCallBack).apply {
            attachToRecyclerView(binding.rvAllBookmarks)
        }
    }

    /**
     * Set bookmark description, is it all bookmarks or it just belongs to a specific topic
     */
    private fun setBookmarksDescription() {
        val bookmarksText = if (viewModel.chosenCourse!= null && viewModel.chosenTopic != null) {
            "${getString(R.string.bookmarks_1)} " +
                    "${viewModel.chosenTopic!!.name}, " +
                    "${getString(R.string.bookmarks_2)} ${viewModel.chosenCourse!!.name}"
        } else {
            getString(R.string.all_bookmarks)
        }

        binding.tvWhichBookmarks.text = bookmarksText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        toastHolder?.cancel()
    }
}