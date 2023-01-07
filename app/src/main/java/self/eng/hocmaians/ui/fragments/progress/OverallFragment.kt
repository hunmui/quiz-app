package self.eng.hocmaians.ui.fragments.progress

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import self.eng.hocmaians.R
import self.eng.hocmaians.databinding.FragmentOverallBinding
import self.eng.hocmaians.ui.custom.RadarMarkerView
import self.eng.hocmaians.ui.fragments.progress.model.AvgScoreAndLabel
import self.eng.hocmaians.util.CommonMethods
import self.eng.hocmaians.util.Constants.COURSE_NAMES
import self.eng.hocmaians.util.Constants.FILTER_BY_GRAMMAR
import self.eng.hocmaians.util.Constants.FILTER_BY_OVERALL
import self.eng.hocmaians.util.Constants.FILTER_BY_PRACTICE_TEST
import self.eng.hocmaians.util.Constants.FILTER_BY_PRONUNCIATION
import self.eng.hocmaians.util.Constants.FILTER_BY_VOCABULARY
import self.eng.hocmaians.util.Constants.GRAMMAR_COURSE_ID
import self.eng.hocmaians.util.Constants.MIXED_QUIZ
import self.eng.hocmaians.util.Constants.OVERALL
import self.eng.hocmaians.util.Constants.PIE_CHART_Y_ANIMATION_TIME
import self.eng.hocmaians.util.Constants.PRACTICE_TEST_COURSE_ID
import self.eng.hocmaians.util.Constants.PRONUNCIATION_COURSE_ID
import self.eng.hocmaians.util.Constants.RADAR_CHART_X_ANIMATION_TIME
import self.eng.hocmaians.util.Constants.RADAR_CHART_Y_ANIMATION_TIME
import self.eng.hocmaians.util.Constants.VOCABULARY_COURSE_ID

/**
 * observeDataForCharts -> getDataCharts -> setDataForCharts
 */
@AndroidEntryPoint
class OverallFragment : Fragment(R.layout.fragment_overall), OnChartValueSelectedListener {

    private var _binding: FragmentOverallBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OverallViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOverallBinding.bind(view)

        // only observe overall scores if first time this fragment gets created
        if (savedInstanceState == null) {
            observeEachCourseScoresForRadarChart()
            observeEachCourseScoresForPieChart()
        } else {
            getDataForRadarChart()
            getDataForPieChart()
        }

        setUpRadarChart()
        setUpPieChart()

        setHelpButtons()

        binding.rbFilterPieChart.setOnClickListener {
            onChooseToFilterPieChart()
        }
    }

    /**
     * Overall scores. A radar chart entry: (45.3%, "Grammar").
     * Observe each course, get its scores then let view model calculate the average score in
     * each course (in percentage).
     */
    private fun observeEachCourseScoresForRadarChart() {

        viewModel.courses.observe(viewLifecycleOwner) { courses ->
            courses.forEach { course ->
                viewModel.getUserScoresByCourse(
                    courseId = course.id
                ).observe(viewLifecycleOwner) { scores ->
                    viewModel.calAvgScoreInPercentage(
                        scores = scores,
                        labelName = course.name
                    )
                }
            }
        }

        // mixed quiz scores
        viewModel.getUserScoresByMixedQuiz().observe(viewLifecycleOwner) { scores ->
            viewModel.calAvgScoreInPercentage(
                scores = scores,
                labelName = MIXED_QUIZ
            )
        }

        getDataForRadarChart()
    }

    /**
     * Overall scores. A pie char entry: (6.7, "Grammar")
     * Observe each course, get its scores then let view model calculate the average score in each
     * course.
     */
    private fun observeEachCourseScoresForPieChart() {

        // clear the previous pie chart data
        viewModel.clearPreviousScoreData()

        viewModel.courses.observe(viewLifecycleOwner) { courses ->
            viewModel.ensureDataQuantity = courses.size

            courses.forEach { course ->
                viewModel.getUserScoresByCourse(
                    courseId = course.id
                ).observe(viewLifecycleOwner) { scores ->
                    viewModel.calculateAverageScore(
                        scores = scores,
                        labelName = course.name
                    )
                }
            }

            getDataForPieChart()
        }
    }

    /**
     * Each course score. A pie chart entry: (7.8, "Topic 1 Pronunciation")
     * Observe each topic, get its scores then let view model calculate the average score in each
     * topic.
     *
     * @param courseId get all topics in a course
     */
    private fun observeEachTopicScoresForPieChart(courseId: Int) {

        // clear the previous pie chart data
        viewModel.clearPreviousScoreData()

        viewModel.getTopicsByCourse(courseId = courseId).observe(viewLifecycleOwner) { topics ->
            viewModel.ensureDataQuantity = topics.size

            topics.forEach { topic ->
                viewModel.getUserScoresByTopic(
                    topicId = topic.id
                ).observe(viewLifecycleOwner) { scores ->
                    viewModel.calculateAverageScore(
                        scores = scores,
                        labelName = topic.name
                    )
                }
            }

            getDataForPieChart()
        }
    }

    /**
     * Observe radar chart data from viewModel, then load it into radar chart. Also, get the course
     * name user need to improve on.
     */
    private fun getDataForRadarChart() {
        viewModel.radarChartData.observe(viewLifecycleOwner) { radarChartData ->
            if (radarChartData.size == COURSE_NAMES.size) {
                viewModel.getCourseThatHasTheLowestAvgScore()
                setDataForRadarChart(radarChartData = radarChartData)
            }
        }

        viewModel.courseNameNeedToImprove.observe(viewLifecycleOwner) { courseName ->
            if (courseName.isNotBlank()) {
                val improveText = "${getString(R.string.should_improve_on)} $courseName"
                binding.tvShouldImproveOn.visibility = View.VISIBLE
                binding.tvShouldImproveOn.text = improveText
            }
        }
    }

    /**
     * Observe pie chart data from viewModel, then load it into pie chart.
     */
    private fun getDataForPieChart() {
        viewModel.pieChartData.observe(viewLifecycleOwner) { pieChartData ->
            if (pieChartData.size == viewModel.ensureDataQuantity) {
                setDataForPieChart(pieChartData = pieChartData)
            }

            setPieChartDescription()
        }
    }

    /**
     * When user click on filter pie chart radio button, pop up an alert dialog to let user choose
     * filter type
     */
    private fun onChooseToFilterPieChart() {

        val optionsToFilter: Array<String> = arrayOf(
            OVERALL, COURSE_NAMES[0], COURSE_NAMES[1], COURSE_NAMES[2], COURSE_NAMES[3]
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.pie_chart_filter))
            .setNegativeButton(getString(R.string.btn_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setSingleChoiceItems(optionsToFilter, viewModel.chosenDialogIndex) { _, index ->
                binding.rbFilterPieChart.isChecked = false
                viewModel.chosenDialogIndex = index
            }
            .setPositiveButton(getString(R.string.btn_filter)) { dialog, _ ->
                binding.rbFilterPieChart.isChecked = false
                onFilterPieChart(chosenFilterIndex = viewModel.chosenDialogIndex)
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Observe data from viewModel for the corresponding filter index
     *
     * @param chosenFilterIndex chosen filter index from alert dialog
     */
    private fun onFilterPieChart(chosenFilterIndex: Int) {
        when (chosenFilterIndex) {
            FILTER_BY_OVERALL -> observeEachCourseScoresForPieChart()
            FILTER_BY_GRAMMAR -> observeEachTopicScoresForPieChart(courseId = GRAMMAR_COURSE_ID)
            FILTER_BY_VOCABULARY -> observeEachTopicScoresForPieChart(courseId = VOCABULARY_COURSE_ID)
            FILTER_BY_PRONUNCIATION -> observeEachTopicScoresForPieChart(courseId = PRONUNCIATION_COURSE_ID)
            FILTER_BY_PRACTICE_TEST -> observeEachTopicScoresForPieChart(courseId = PRACTICE_TEST_COURSE_ID)
        }
    }

    /**
     * Let the radar chart display data
     *
     * @param radarChartData a list of course name and its average score
     */
    private fun setDataForRadarChart(radarChartData: List<AvgScoreAndLabel>) {

        val entries: MutableList<RadarEntry> = mutableListOf()
        val courseNames: MutableList<String> = mutableListOf()

        // extract average score and corresponding course name
        radarChartData.forEach { avgScoreAndLabel ->
            // NOTE: The order of the entries when being added to the entries array determines
            // their position around the center of the radarChart.
            entries.add(RadarEntry(avgScoreAndLabel.avgScore))
            courseNames.add(avgScoreAndLabel.labelName)
        }

        // set up x and y axis, also the legend
        setupXYAxisAndLegendForRadarChart(courseNames = courseNames)

        val set1 = RadarDataSet(entries, getString(R.string.radar_chart_data_label))

        set1.apply {
            color = Color.rgb(255, 165, 0)
            fillColor = Color.rgb(255, 165, 0)
            setDrawFilled(true)
            fillAlpha = 180
            lineWidth = 2f
            isDrawHighlightCircleEnabled = true
            setDrawHighlightIndicators(false)
        }

        val sets: List<IRadarDataSet> = listOf(set1)

        val data = RadarData(sets)

        data.apply {
            setValueTextSize(8f)
            setDrawValues(false)
            setValueTextColor(Color.WHITE)
        }

        binding.radarChart.data = data
        binding.radarChart.invalidate()

        // animate x and y axis
        binding.radarChart.animateXY(
            RADAR_CHART_X_ANIMATION_TIME,
            RADAR_CHART_Y_ANIMATION_TIME,
            Easing.EaseInOutQuad
        )
    }

    /**
     * Let the radar chart display data
     *
     * @param pieChartData a list of label name and its average score
     */
    private fun setDataForPieChart(pieChartData: List<AvgScoreAndLabel>) {
        val entries: MutableList<PieEntry> = mutableListOf()

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        pieChartData.forEach {
            entries.add(PieEntry(it.avgScore, it.labelName))
        }

        val dataSet = PieDataSet(entries, getString(R.string.radar_chart_data_label))

        dataSet.apply {
            setDrawIcons(false)

            sliceSpace = 3f
            iconsOffset = MPPointF(0f, 40f)
            selectionShift = 5f
        }

        // add a lot of colors
        val colors: MutableList<Int> = mutableListOf()

        for (c in ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c)

        for (c in ColorTemplate.JOYFUL_COLORS)
            colors.add(c)

        for (c in ColorTemplate.COLORFUL_COLORS)
            colors.add(c)

        for (c in ColorTemplate.LIBERTY_COLORS)
            colors.add(c)

        for (c in ColorTemplate.PASTEL_COLORS)
            colors.add(c)

        colors.add(ColorTemplate.getHoloBlue())

        dataSet.colors = colors

        val data = PieData(dataSet)

        data.apply {
            setValueFormatter(PercentFormatter(binding.pieChart))
            setValueTextSize(10f)
            setValueTextColor(Color.BLACK)
        }

        binding.pieChart.data = data

        binding.pieChart.apply {
            // undo all highlights
            highlightValues(null)

            animateY(PIE_CHART_Y_ANIMATION_TIME, Easing.EaseInOutQuad)
            invalidate()
        }
    }

    /**
     * Set up radar chart foundation
     */
    private fun setUpRadarChart() {
        binding.radarChart.apply {
            // set the background for the radarChart
            setBackgroundColor(Color.TRANSPARENT)

            // disable radarChart description (looks ugly)
            description.isEnabled = false

            // width, and color of the radar radarChart's cross lines (line that connects 2 labels)
            webLineWidth = 1f
            webColor = Color.LTGRAY

            // width, and color of the radar radarChart's web lines (lines that are like spider web)
            webLineWidthInner = 1f
            webColorInner = Color.LTGRAY

            // transparency for all web lines (0: transparent, 255: no transparent)
            webAlpha = 100

            // custom MarkerView (extend MarkerView) and specify the layout to use for it
            val markerView: MarkerView = RadarMarkerView(
                context = requireContext(),
                layoutResource = R.layout.radar_marker_view
            )
            markerView.chartView = this // For bounds control
            marker = markerView // Set the marker to the radarChart
        }
    }

    /**
     * Set up x and y axis, also legend for radar chart
     *
     * @param courseNames represents labels in radar chart
     */
    private fun setupXYAxisAndLegendForRadarChart(courseNames: List<String>) {
        val xAxis: XAxis = binding.radarChart.xAxis

        xAxis.apply {
            textSize = 9f
            xOffset = 0f
            yOffset = 0f
            valueFormatter = object : ValueFormatter() {

                override fun getFormattedValue(value: Float): String {
                    return courseNames[value.toInt()]
                }
            }
            textColor = Color.WHITE
        }

        val yAxis: YAxis = binding.radarChart.yAxis

        yAxis.apply {
            setLabelCount(courseNames.size, false)
            textSize = 9f
            axisMinimum = 0f
            axisMaximum = 80f
            setDrawLabels(false)
        }

        val legend: Legend = binding.radarChart.legend

        legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            xEntrySpace = 7f
            yEntrySpace = 5f
            textColor = Color.WHITE
        }
    }

    /**
     * Set up pie chart foundation
     */
    private fun setUpPieChart() {
        binding.pieChart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            setExtraOffsets(5f, 10f, 5f, 5f)

            dragDecelerationFrictionCoef = 0.95f
            centerText = generateCenterSpannableText()

            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)

            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)

            holeRadius = 50f
            transparentCircleRadius = 53f

            setDrawCenterText(true)

            rotationAngle = 0f
            // enable rotation of the chart by touch
            isRotationEnabled = true
            isHighlightPerTapEnabled = true

            // add a selection listener
            setOnChartValueSelectedListener(this@OverallFragment)

            // entry label styling
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
        }

        val legend: Legend = binding.pieChart.legend

        legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            orientation = Legend.LegendOrientation.VERTICAL
            textColor = Color.WHITE
            setDrawInside(false)
            xEntrySpace = 7f
            yEntrySpace = 0f
            yOffset = 0f
        }
    }

    /**
     * Style some text in the pie chart hole
     *
     * @return the styled text
     */
    private fun generateCenterSpannableText(): SpannableString {
        val spannableString = SpannableString("EnglishQuiz\ndeveloped with LOVE")

        val startPos = 0
        val englishQuizEndPos = 11
        val developedWithEndPos = spannableString.length - 5
        val endPos = spannableString.length

        spannableString.apply {

            // EnglishQuiz
            setSpan(RelativeSizeSpan(1.7f), startPos, englishQuizEndPos, 0)

            // developed with
            setSpan(StyleSpan(Typeface.NORMAL), englishQuizEndPos, developedWithEndPos, 0)
            setSpan(ForegroundColorSpan(Color.GRAY), englishQuizEndPos, developedWithEndPos, 0)
            setSpan(RelativeSizeSpan(.8f), englishQuizEndPos, developedWithEndPos, 0)

            // LOVE
            setSpan(StyleSpan(Typeface.ITALIC), developedWithEndPos, endPos, 0)
            setSpan(
                ForegroundColorSpan(ColorTemplate.getHoloBlue()),
                developedWithEndPos,
                endPos,
                0
            )
        }

        return spannableString
    }

    /**
     * Set pie chart description
     */
    private fun setPieChartDescription() {
        binding.tvPieChartDescription.text = when (viewModel.chosenDialogIndex) {
            FILTER_BY_OVERALL -> getString(R.string.pie_chart_overall_desc)
            FILTER_BY_GRAMMAR -> "${getString(R.string.pie_chart_desc)} ${COURSE_NAMES[0]}"
            FILTER_BY_VOCABULARY -> "${getString(R.string.pie_chart_desc)} ${COURSE_NAMES[1]}"
            FILTER_BY_PRONUNCIATION -> "${getString(R.string.pie_chart_desc)} ${COURSE_NAMES[2]}"
            FILTER_BY_PRACTICE_TEST -> "${getString(R.string.pie_chart_desc)} ${COURSE_NAMES[3]}"
            else -> getString(R.string.pie_chart_overall_desc)
        }
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
    }

    override fun onNothingSelected() {
    }

    private fun setHelpButtons() {
        binding.apply {
            ivHowToReadRadarChart.setOnClickListener {
                CommonMethods.showHelpDialog(
                    context = requireContext(),
                    title = getString(R.string.read_radar_chart_title),
                    message = getString(R.string.read_radar_chart_msg)
                )
            }

            ivHowToReadPieChart.setOnClickListener {
                CommonMethods.showHelpDialog(
                    context = requireContext(),
                    title = getString(R.string.read_pie_chart_title),
                    message = getString(R.string.read_pie_chart_msg)
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}