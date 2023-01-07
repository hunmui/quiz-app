package self.eng.hocmaians.ui.fragments.progress

import android.graphics.Color
import android.graphics.DashPathEffect
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import dagger.hilt.android.AndroidEntryPoint
import self.eng.hocmaians.R
import self.eng.hocmaians.data.entities.Course
import self.eng.hocmaians.data.entities.Score
import self.eng.hocmaians.data.entities.Topic
import self.eng.hocmaians.databinding.FragmentDetailScoreBinding
import self.eng.hocmaians.ui.custom.MyMarkerView
import self.eng.hocmaians.util.CommonMethods
import self.eng.hocmaians.util.Constants.ANIMATE_X_DURATION
import self.eng.hocmaians.util.Constants.ANIMATE_Y_DURATION
import self.eng.hocmaians.util.Constants.COURSE_BASED_RESULTS
import self.eng.hocmaians.util.Constants.LIMIT_LINE_TEXT_SIZE
import self.eng.hocmaians.util.Constants.MAX_SCORE
import self.eng.hocmaians.util.Constants.MAX_Y_VALUE
import self.eng.hocmaians.util.Constants.MIN_SCORE
import self.eng.hocmaians.util.Constants.MIN_Y_VALUE
import self.eng.hocmaians.util.Constants.MIXED_COURSE_NAME
import self.eng.hocmaians.util.Constants.MIXED_QUIZ_RESULTS
import self.eng.hocmaians.util.Constants.MIXED_TOPIC_NAME
import self.eng.hocmaians.util.Constants.OVERALL_RESULTS
import self.eng.hocmaians.util.Status

/**
 * This fragment has 3 screens:
 * First screen: Filter text, graph description and the graph itself.
 * Second screen: Let user choose filter condition.
 * Third Screen (filter by topic is optional): Let user choose filter by which course and/or which topic to be filtered
 */
@AndroidEntryPoint
class DetailScoreFragment : Fragment(R.layout.fragment_detail_score), OnChartValueSelectedListener {

    // view binding
    private var _binding: FragmentDetailScoreBinding? = null
    private val binding get() = _binding!!

    // view model
    private val viewModel: DetailScoreViewModel by viewModels()

    private var noCourse = false
    private var noTopic = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetailScoreBinding.bind(view)

        subscribeToObserver()

        if (savedInstanceState != null && viewModel.isScoresInitialized) {
            setGraphDescription()
            goGetDataForGraph()
        } else {
            whenNoData()
        }

        // restore currently shown layout when rotate the device
        when {
            viewModel.showFirstLayout -> {
                showFirstScreen()
                hideSecondScreen()
                hideThirdScreen()
            }
            viewModel.showSecondLayout -> {
                hideFirstScreen()
                showSecondScreen()
                hideThirdScreen()
            }
            viewModel.showThirdLayout -> {
                hideFirstScreen()
                hideSecondScreen()
                showThirdScreen()
            }
        }

        setupChartFoundation()
        setupXYAxis()

        binding.tvChooseFilter.setOnClickListener {
            hideFirstScreen()
            showSecondScreen()
        }

        binding.rgFilter.setOnCheckedChangeListener { _, checkedIndex ->
            when (checkedIndex) {
                R.id.rb_filter_by_overall -> {
                    hideSecondScreen()

                    viewModel.filterByOverall()

                    setGraphDescription()
                    goGetDataForGraph()

                    showFirstScreen()
                }
                R.id.rb_filter_by_mixed_quiz -> {
                    hideSecondScreen()

                    viewModel.filterByMixedQuiz()

                    setGraphDescription()
                    goGetDataForGraph()

                    showFirstScreen()
                }
                R.id.rb_filter_by_course -> {
                    hideSecondScreen()
                    viewModel.showFilterByTopicSpinner = false
                    showThirdScreen()
                }
                R.id.rb_filter_by_topic -> {
                    hideSecondScreen()
                    viewModel.showFilterByTopicSpinner = true
                    showThirdScreen()
                }
            }
        }

        // go back from third screen to second screen
        binding.btnGoBack.setOnClickListener {
            hideThirdScreen()
            showSecondScreen()
        }
    }

    /**
     * Observe filter progress state
     */
    private fun subscribeToObserver() {
        viewModel.filter.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        hideThirdScreen()

                        setGraphDescription()
                        goGetDataForGraph()

                        showFirstScreen()
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
     * Load courses into spinner, then set click listener on Filter button only if this screen
     * contains only filtering by courses
     */
    private fun loadCoursesIntoSpinner() {
        viewModel.courses.observe(viewLifecycleOwner) { courses ->
            if (courses.isEmpty()) {
                binding.spinnerChooseCourse.adapter = null
                noCourse = true
            } else {
                val courseAdapter: ArrayAdapter<Course> = ArrayAdapter(
                    requireContext(),
                    R.layout.spinner_display_text,
                    courses
                )
                courseAdapter.setDropDownViewResource(R.layout.each_spinner_text_view)

                binding.spinnerChooseCourse.apply {
                    adapter = courseAdapter

                    // load the chosen course in case of screen rotation
                    viewModel.chosenCourse?.let {
                        this.setSelection(courseAdapter.getPosition(it))
                    }

                    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            val chosenCourse = parent?.selectedItem as Course
                            viewModel.onChooseCourse(course = chosenCourse)

                            if (viewModel.showFilterByTopicSpinner) {
                                loadTopicsIntoSpinner()
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                        }
                    }
                }

                // if only filter by courses
                if (!viewModel.showFilterByTopicSpinner) {
                    binding.btnFilterResults.setOnClickListener {
                        filterByCourseOrTopic()
                    }
                }
            }
        }
    }

    /**
     * Load topics into spinner, then set click listener on Filter button
     */
    private fun loadTopicsIntoSpinner() {
        viewModel.topicsByCourse.observe(viewLifecycleOwner) { topics ->
            if (topics.isEmpty()) {
                binding.spinnerChooseTopic.adapter = null
                noTopic = true
            } else {
                val topicsAdapter: ArrayAdapter<Topic> = ArrayAdapter(
                    requireContext(),
                    R.layout.spinner_display_text,
                    topics
                )
                topicsAdapter.setDropDownViewResource(R.layout.each_spinner_text_view)

                binding.spinnerChooseTopic.apply {
                    adapter = topicsAdapter

                    // load the chosen topic in case of screen rotation
                    viewModel.chosenTopic?.let {
                        this.setSelection(topicsAdapter.getPosition(it))
                    }

                    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            viewModel.chosenTopic = parent?.selectedItem as Topic
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                        }
                    }
                }

                // filter by topics
                binding.btnFilterResults.setOnClickListener {
                    filterByCourseOrTopic()
                }
            }
        }
    }

    /**
     * Filter results by course or by topic, based on the variable `showFilterByTopicSpinner`. If
     * it is true, then filter by topic; else filter by course
     */
    private fun filterByCourseOrTopic() {
        when {
            noCourse -> {
                Toast.makeText(
                    requireContext(),
                    R.string.progress_no_courses,
                    Toast.LENGTH_SHORT
                ).show()
            }
            viewModel.showFilterByTopicSpinner && noTopic -> {
                Toast.makeText(
                    requireContext(),
                    R.string.progress_no_courses,
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                viewModel.onFilterProgress()
            }
        }
    }

    /**
     * Set description text for the line graph
     */
    private fun setGraphDescription() {
        val description: String = when (viewModel.getGraphDataBy) {
            OVERALL_RESULTS -> getString(R.string.graph_desc_overall)
            MIXED_QUIZ_RESULTS -> {
                "${getString(R.string.graph_desc_by_topic_1)} $MIXED_TOPIC_NAME" +
                        "${getString(R.string.graph_desc_by_topic_2)} $MIXED_COURSE_NAME"
            }
            COURSE_BASED_RESULTS -> {
                "${getString(R.string.graph_desc_by_course)} ${viewModel.chosenCourse!!.name}"
            }
            else -> {
                "${getString(R.string.graph_desc_by_topic_1)} ${viewModel.chosenTopic!!.name}" +
                        "${getString(R.string.graph_desc_by_topic_2)} ${viewModel.chosenCourse!!.name}"
            }
        }

        binding.tvGraphDescription.text = description
    }

    /**
     * Go get scores to fill line graph
     */
    private fun goGetDataForGraph() {
        viewModel.scores.observe(viewLifecycleOwner) { scores ->
            setupLineDataSet(scores = scores)

            binding.tvAverageScore.apply {
                visibility = View.VISIBLE
                text = if (scores.isEmpty()) {
                    getString(R.string.have_not_done_any_question)
                } else {
                    "${getString(R.string.overall_score)} ${viewModel.calculateAvgScore(scores)}"
                }
            }
        }
    }

    /**
     * Set up line graph line data set
     *
     * @param scores a list of scores to load
     */
    private fun setupLineDataSet(scores: List<Score>) {
        // a list of entries
        val entries: MutableList<Entry> = mutableListOf()

        // fill entries
        for (i in scores.indices) {
            val userScoreInString = CommonMethods.userScoreInString(
                scores[i].totalCorrect,
                scores[i].totalQuestions
            )

            // Entry(xValue, yValue)
            entries.add(
                Entry(
                    (i + 1).toFloat(),
                    userScoreInString.toFloat()
                )
            )
        }

        lateinit var lineDataSet: LineDataSet

        if (binding.lineChart.data != null && binding.lineChart.data.dataSetCount > 0) {
            // this graph already has some data
            lineDataSet = binding.lineChart.data.getDataSetByIndex(0) as LineDataSet

            // clear previous value and refresh the graph
            lineDataSet.values.clear()
            binding.lineChart.invalidate()

            lineDataSet.values = entries
            lineDataSet.notifyDataSetChanged()
            binding.lineChart.data.notifyDataChanged()
            binding.lineChart.notifyDataSetChanged()
        } else {
            // first time this graph has data

            // create a dataset and give it a type
            lineDataSet = LineDataSet(entries, getString(R.string.data_set_name))

            lineDataSet.apply {
                setDrawIcons(false)

                // draw dashed line
                enableDashedLine(10f, 5f, 0f)

                // white lines and points
                color = Color.WHITE
                setCircleColor(Color.WHITE)

                // line thickness and point size
                lineWidth = 1f
                circleRadius = 3f

                // draw points as solid circles
                setDrawCircleHole(false)

                // customize legend entry
                formLineWidth = 1f
                formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
                formSize = 15f

                // text size of values
                valueTextSize = 9f

                // draw selection line as dashed
                enableDashedHighlightLine(10f, 5f, 0f)

                // set the filled area
                setDrawFilled(true)
                fillFormatter = IFillFormatter { _, _ -> MIN_SCORE }
            }

            val dataSets: MutableList<ILineDataSet> = mutableListOf()
            dataSets.add(lineDataSet) // add the data sets

            // create a data object with the data sets
            val data = LineData(dataSets)

            // set data
            binding.lineChart.data = data
            binding.lineChart.data.setValueTextColor(Color.WHITE)

            // refresh graph
            binding.lineChart.invalidate()
        }

        // draw points over time
        binding.lineChart.animateXY(ANIMATE_X_DURATION, ANIMATE_Y_DURATION)
    }

    /**
     * What to do when there is no data yet
     * 1. Direct the user to filter his scores
     */
    private fun whenNoData() {
        binding.tvGraphDescription.text = getString(R.string.graph_desc_when_no_data)
    }

    private fun hideFirstScreen() {
        viewModel.showFirstLayout = false

        binding.apply {
            tvChooseFilter.visibility = View.GONE
            view1.visibility = View.GONE
            tvGraphDescription.visibility = View.GONE
            lineChart.visibility = View.GONE
            tvAverageScore.visibility = View.GONE
        }
    }

    private fun showFirstScreen() {
        viewModel.showFirstLayout = true

        binding.apply {
            tvChooseFilter.visibility = View.VISIBLE
            view1.visibility = View.VISIBLE
            tvGraphDescription.visibility = View.VISIBLE
            lineChart.visibility = View.VISIBLE
            tvAverageScore.visibility = View.VISIBLE
        }
    }

    private fun showSecondScreen() {
        viewModel.showSecondLayout = true

        binding.apply {
            tvFilterBy.visibility = View.VISIBLE
            rgFilter.visibility = View.VISIBLE
        }
    }

    private fun hideSecondScreen() {
        viewModel.showSecondLayout = false

        binding.apply {
            tvFilterBy.visibility = View.GONE
            rgFilter.visibility = View.GONE
            rgFilter.clearCheck()
        }
    }

    private fun showThirdScreen() {
        viewModel.showThirdLayout = true

        binding.apply {
            tvStar1.visibility = View.VISIBLE
            tvChooseCourse.visibility = View.VISIBLE
            spinnerChooseCourse.visibility = View.VISIBLE
            ivDropdown1.visibility = View.VISIBLE
            btnFilterResults.visibility = View.VISIBLE
            btnGoBack.visibility = View.VISIBLE

            loadCoursesIntoSpinner()

            if (viewModel.showFilterByTopicSpinner) {
                tvStar2.visibility = View.VISIBLE
                tvChooseTopic.visibility = View.VISIBLE
                spinnerChooseTopic.visibility = View.VISIBLE
                ivDropdown2.visibility = View.VISIBLE
            }
        }
    }

    private fun hideThirdScreen() {
        binding.apply {
            tvStar1.visibility = View.GONE
            tvChooseCourse.visibility = View.GONE
            spinnerChooseCourse.visibility = View.GONE
            ivDropdown1.visibility = View.GONE
            btnFilterResults.visibility = View.GONE
            btnGoBack.visibility = View.GONE

            if (viewModel.showFilterByTopicSpinner) {
                tvStar2.visibility = View.GONE
                tvChooseTopic.visibility = View.GONE
                spinnerChooseTopic.visibility = View.GONE
                ivDropdown2.visibility = View.GONE
            }

            viewModel.showThirdLayout = false
            viewModel.showFilterByTopicSpinner = false
        }
    }

    /**
     * Set line graph basic properties such as background color, legend, etc.
     */
    private fun setupChartFoundation() {
        binding.lineChart.apply {
            // background color
            setBackgroundColor(Color.TRANSPARENT)

            // disable description text
            description.isEnabled = false

            // enable touch gestures
            setTouchEnabled(true)

            // set listeners
            setOnChartValueSelectedListener(this@DetailScoreFragment)
            setDrawGridBackground(false)

            // create marker to display box when values are selected
            val markerView = MyMarkerView(requireContext(), R.layout.custom_marker_view)

            // set the marker to the chart
            markerView.chartView = this
            marker = markerView

            // enable scaling and dragging
            isDragEnabled = true
            setScaleEnabled(true)

            // force pin zoom along both axis
            setPinchZoom(true)

            // get the legend (only possible after setting data)
            val legend: Legend = this.legend

            // draw legend entries as lines
            legend.form = Legend.LegendForm.LINE

            legend.textColor = Color.WHITE
        }
    }

    /**
     * Set up xAxis and yAxis, then set 2 limit lines on yAxis
     */
    private fun setupXYAxis() {
        lateinit var xAxis: XAxis
        lateinit var yAxis: YAxis

        binding.lineChart.apply {
            xAxis = this.xAxis

            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 0f)
            xAxis.textColor = Color.WHITE

            // disable dual axis (only use LEFT axis)
            this.axisRight.isEnabled = false

            yAxis = this.axisLeft
            yAxis.apply {
                // horizontal grid lines
                enableGridDashedLine(10f, 10f, 0f)

                textColor = Color.WHITE

                // axis range
                axisMaximum = MAX_Y_VALUE
                axisMinimum = MIN_Y_VALUE
            }
        }

        createLimitLines(xAxis, yAxis)
    }

    private fun createLimitLines(xAxis: XAxis, yAxis: YAxis) {
        val ll1 = LimitLine(MAX_SCORE, getString(R.string.upper_y_limit))
        ll1.apply {
            lineWidth = 4f
            enableDashedLine(10f, 10f, 0f)
            labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
            textSize = LIMIT_LINE_TEXT_SIZE
            textColor = Color.WHITE
        }

        val ll2 = LimitLine(MIN_SCORE, getString(R.string.lower_y_limit))
        ll2.apply {
            lineWidth = 4f
            enableDashedLine(10f, 10f, 0f)
            labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
            textSize = LIMIT_LINE_TEXT_SIZE
            textColor = Color.WHITE
        }

        // draw limit lines behind data instead of on top
        yAxis.setDrawLimitLinesBehindData(true)
        xAxis.setDrawLimitLinesBehindData(true)

        // add limit lines to yAxis only
        yAxis.addLimitLine(ll1)
        yAxis.addLimitLine(ll2)
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
    }

    override fun onNothingSelected() {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}