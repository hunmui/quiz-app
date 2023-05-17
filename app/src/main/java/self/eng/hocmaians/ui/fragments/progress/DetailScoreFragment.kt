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
 * Đoạn này có 3 màn hình:
 * Màn hình đầu tiên: Lọc văn bản, mô tả biểu đồ và chính biểu đồ đó.
 * Màn hình thứ hai: Cho phép người dùng chọn điều kiện lọc.
 * Màn hình thứ ba (lọc theo chủ đề là tùy chọn): Cho phép người dùng chọn lọc theo khóa học và/hoặc chủ đề nào sẽ được lọc
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

        // khôi phục bố cục đang hiển thị khi xoay thiết bị
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

        // quay về từ màn hình thứ 3 đến đến màn hình thứ 2
        binding.btnGoBack.setOnClickListener {
            hideThirdScreen()
            showSecondScreen()
        }
    }

    /**
     * theo dõi trạng thái lọc
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
     * Tải các khóa học vào công cụ quay vòng, sau đó chỉ đặt trình nghe nhấp chuột trên nút Bộ lọc nếu màn hình này
     * chỉ chứa lọc theo khóa học
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

                    // tải khóa học đã chọn trong trường hợp xoay màn hình
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
     * Tải các chủ đề vào công cụ quay vòng, sau đó đặt trình nghe nhấp vào nút Bộ lọc
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

                    // tải chủ đề đã chọn trong trường hợp xoay màn hình
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
     * Lọc kết quả theo khóa học hoặc theo chủ đề, dựa trên biến `showFilterByTopicSpinner`. Nếu như
     * đúng thì lọc theo chủ đề; khác lọc theo khóa học
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
     * Đặt văn bản mô tả cho biểu đồ đường
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
     * Đi lấy điểm để điền biểu đồ đường
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
     * Thiết lập bộ dữ liệu đường biểu đồ đường
     *
     * @param scores danh sách điểm cần tải
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
            // biểu đồ này đã có một số dữ liệu
            lineDataSet = binding.lineChart.data.getDataSetByIndex(0) as LineDataSet

            // xóa giá trị trước đó và làm mới biểu đồ
            lineDataSet.values.clear()
            binding.lineChart.invalidate()

            lineDataSet.values = entries
            lineDataSet.notifyDataSetChanged()
            binding.lineChart.data.notifyDataChanged()
            binding.lineChart.notifyDataSetChanged()
        } else {
            // lần đầu tiên biểu đồ này có dữ liệu

            // tạo tập dữ liệu và đặt kiểu cho nó
            lineDataSet = LineDataSet(entries, getString(R.string.data_set_name))

            lineDataSet.apply {
                setDrawIcons(false)

                // vẽ nét đứt
                enableDashedLine(10f, 5f, 0f)

                // đường trắng và điểm
                color = Color.WHITE
                setCircleColor(Color.WHITE)

                // độ dày đường kẻ và kích thước điểm
                lineWidth = 1f
                circleRadius = 3f

                // vẽ điểm dưới dạng đường tròn liền
                setDrawCircleHole(false)

                // tùy chỉnh mục chú thích
                formLineWidth = 1f
                formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
                formSize = 15f

                // kích thước văn bản của các giá trị
                valueTextSize = 9f

                // vẽ đường chọn dạng nét đứt
                enableDashedHighlightLine(10f, 5f, 0f)

                // đặt vùng đã điền
                setDrawFilled(true)
                fillFormatter = IFillFormatter { _, _ -> MIN_SCORE }
            }

            val dataSets: MutableList<ILineDataSet> = mutableListOf()
            dataSets.add(lineDataSet) // thêm bộ dữ liệu

            // tạo một đối tượng dữ liệu với các bộ dữ liệu
            val data = LineData(dataSets)

            // thiết lập dữ liệu
            binding.lineChart.data = data
            binding.lineChart.data.setValueTextColor(Color.WHITE)

            // refresh graph
            binding.lineChart.invalidate()
        }

        // vẽ điểm theo thời gian
        binding.lineChart.animateXY(ANIMATE_X_DURATION, ANIMATE_Y_DURATION)
    }

    /**
     * Phải làm gì khi chưa có dữ liệu
     * 1. Hướng dẫn người dùng lọc điểm của mình
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
     * Đặt các thuộc tính cơ bản của biểu đồ đường như màu nền, chú thích, v.v.
     */
    private fun setupChartFoundation() {
        binding.lineChart.apply {
            // background color
            setBackgroundColor(Color.TRANSPARENT)

            // vô hiệu hóa văn bản mô tả
            description.isEnabled = false

            // kích hoạt thao tác chạm
            setTouchEnabled(true)

            // set listeners
            setOnChartValueSelectedListener(this@DetailScoreFragment)
            setDrawGridBackground(false)

            // tạo điểm đánh dấu để hiển thị hộp khi các giá trị được chọn
            val markerView = MyMarkerView(requireContext(), R.layout.custom_marker_view)

            // đặt điểm đánh dấu vào biểu đồ
            markerView.chartView = this
            marker = markerView

            // kích hoạt mở rộng và kéo
            isDragEnabled = true
            setScaleEnabled(true)

            // buộc phóng to pin dọc theo cả hai trục
            setPinchZoom(true)

            // lấy chú thích (chỉ có thể sau khi thiết lập dữ liệu)
            val legend: Legend = this.legend

            // vẽ các mục chú thích dưới dạng các dòng
            legend.form = Legend.LegendForm.LINE

            legend.textColor = Color.WHITE
        }
    }

    /**
     * Thiết lập xAxis và yAxis, sau đó thiết lập 2 đường giới hạn trên yAxis
     */
    private fun setupXYAxis() {
        lateinit var xAxis: XAxis
        lateinit var yAxis: YAxis

        binding.lineChart.apply {
            xAxis = this.xAxis

            // đường lưới dọc
            xAxis.enableGridDashedLine(10f, 10f, 0f)
            xAxis.textColor = Color.WHITE

            // vô hiệu hóa trục kép (chỉ sử dụng trục TRÁI)
            this.axisRight.isEnabled = false

            yAxis = this.axisLeft
            yAxis.apply {
                // đường lưới ngang
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

        // vẽ các đường giới hạn phía sau dữ liệu thay vì phía trên
        yAxis.setDrawLimitLinesBehindData(true)
        xAxis.setDrawLimitLinesBehindData(true)

        // chỉ thêm các đường giới hạn vào yAxis
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