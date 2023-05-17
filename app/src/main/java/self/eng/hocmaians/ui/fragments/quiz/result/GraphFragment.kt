package self.eng.hocmaians.ui.fragments.quiz.result

import android.graphics.Color
import android.graphics.DashPathEffect
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
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
import self.eng.hocmaians.databinding.FragmentGraphBinding
import self.eng.hocmaians.data.entities.Score
import self.eng.hocmaians.ui.custom.MyMarkerView
import self.eng.hocmaians.util.CommonMethods
import self.eng.hocmaians.util.Constants
import self.eng.hocmaians.util.Constants.GRAPH_FRAGMENT
import self.eng.hocmaians.util.Constants.MIXED_COURSE_NAME
import self.eng.hocmaians.util.Constants.MIXED_TOPIC_ID
import self.eng.hocmaians.util.Constants.MIXED_TOPIC_NAME

/**
 * Hiển thị một biểu đồ nhỏ đẹp mắt để hiển thị tiến trình của người dùng. Ngoài ra, hiển thị danh sách các lần thử trước của người dùng
 * để người dùng có thể điều hướng quay lại lần thử cụ thể đó.
 *
 * LƯU Ý: có rất nhiều hằng số ma thuật trong biểu đồ đường
 */
@AndroidEntryPoint
class GraphFragment : Fragment(R.layout.fragment_graph), OnChartValueSelectedListener {

    // view binding
    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

    // all passed arguments
    private val args: GraphFragmentArgs by navArgs()

    // view model
    private val viewModel: GraphViewModel by viewModels()

    // adapter
    private lateinit var attemptsAdapter: AttemptsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGraphBinding.bind(view)

        setupRecyclerView()

        viewModel.getUserScoresByTopic(args.topicId).observe(viewLifecycleOwner) { scores ->
            setDataForGraph(scores = scores)
            attemptsAdapter.differ.submitList(scores)
        }

        // set graph description
        if (args.topicId == MIXED_TOPIC_ID) {
            setGraphDescription(
                topicName = MIXED_TOPIC_NAME,
                courseName = MIXED_COURSE_NAME
            )
        } else {
            viewModel.getCourseNameByTopicId(args.topicId).observe(viewLifecycleOwner)
            { courseName ->
                viewModel.getTopicNameByItsId(args.topicId).observe(viewLifecycleOwner)
                { topicName ->
                    setGraphDescription(
                        topicName = topicName,
                        courseName = courseName
                    )
                }
            }
        }

        setupLineChart()
        setupXYAxis()

        binding.ivReadGraphHelper.setOnClickListener {
            CommonMethods.showHelpDialog(
                context = requireContext(),
                title = getString(R.string.tv_read_graph_helper),
                message = getString(R.string.read_graph_helper_message)
            )
        }
    }

    /**
     * Đặt mô tả cho đồ thị
     *
     * @param topicName tên chủ đề
     * @param courseName tên khóa học
     */
    private fun setGraphDescription(topicName: String, courseName: String) {
        val graphDescription = "${getString(R.string.tv_graph_desc_1)} $topicName" +
                "${getString(R.string.tv_graph_desc_2)} $courseName"
        binding.tvGraphDescription.text = graphDescription
    }

    /**
     * Thiết lập chế độ xem trình tái chế lần thử trước
     */
    private fun setupRecyclerView() {
        attemptsAdapter = AttemptsAdapter()

        binding.rvPreviousAttempts.apply {
            adapter = attemptsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        // user choose a specific attempt
        attemptsAdapter.setOnScoreClickListener { score ->
            val action = GraphFragmentDirections
                .actionGraphFragmentToReviewAnswersFragment(
                    score.topicId,
                    score.timestamp,
                    GRAPH_FRAGMENT
                )
            findNavController().navigate(action)
        }
    }

    /**
     * Set data for the graph
     */
    private fun setDataForGraph(scores: List<Score>) {

        // danh sách các mục
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
            lineDataSet = binding.lineChart.data.getDataSetByIndex(0) as LineDataSet
            lineDataSet.values = entries
            lineDataSet.notifyDataSetChanged()
            binding.lineChart.data.notifyDataChanged()
            binding.lineChart.notifyDataSetChanged()
        } else {
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

                // set the filled area
                setDrawFilled(true)
                fillFormatter = IFillFormatter { _, _ -> Constants.MIN_SCORE }
            }

            val dataSets: MutableList<ILineDataSet> = mutableListOf()
            dataSets.add(lineDataSet) // add the data sets

            // tạo một đối tượng dữ liệu với các bộ dữ liệu
            val data = LineData(dataSets)

            // set data
            binding.lineChart.data = data
            binding.lineChart.data.setValueTextColor(Color.WHITE)
        }
    }

    /**
     * Đặt thuộc tính và trình nghe của biểu đồ
     */
    private fun setupLineChart() {
        binding.lineChart.apply {
            // background color
            setBackgroundColor(Color.BLACK)

            // disable description text
            description.isEnabled = false

            // kích hoạt thao tác chạm
            setTouchEnabled(true)

            // set listeners
            setOnChartValueSelectedListener(this@GraphFragment)
            setDrawGridBackground(false)

            // tạo điểm đánh dấu để hiển thị hộp khi các giá trị được chọn
            val markerView = MyMarkerView(requireContext(), R.layout.custom_marker_view)

            // đặt điểm đánh dấu cho biểu đồ
            markerView.chartView = this
            marker = markerView

            // kích hoạt mở rộng và kéo
            isDragEnabled = true
            setScaleEnabled(true)

            // buộc phóng to pin dọc theo cả hai trục
            setPinchZoom(true)

            // vẽ điểm theo thời gian
            animateXY(Constants.ANIMATE_X_DURATION, Constants.ANIMATE_Y_DURATION)

            // lấy chú thích (chỉ có thể sau khi thiết lập dữ liệu)
            val legend: Legend = this.legend

            // draw legend entries as lines
            legend.form = Legend.LegendForm.LINE

            legend.textColor = Color.WHITE
        }
    }

    /**
     * Set up xAxis and yAxis
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
                axisMaximum = Constants.MAX_Y_VALUE
                axisMinimum = Constants.MIN_Y_VALUE
            }
        }

        createLimitLines(xAxis, yAxis)
    }

    /**
     * Tạo 1 đường giới hạn cho xAxis và 2 đường giới hạn cho yAxis
     *
     * @param xAxis xAxis
     * @param yAxis yAxis
     */
    private fun createLimitLines(xAxis: XAxis, yAxis: YAxis) {
        val ll1 = LimitLine(Constants.MAX_SCORE, getString(R.string.upper_y_limit))
        ll1.apply {
            lineWidth = 4f
            enableDashedLine(10f, 10f, 0f)
            labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
            textSize = Constants.LIMIT_LINE_TEXT_SIZE
            textColor = Color.WHITE
        }

        val ll2 = LimitLine(Constants.MIN_SCORE, getString(R.string.lower_y_limit))
        ll2.apply {
            lineWidth = 4f
            enableDashedLine(10f, 10f, 0f)
            labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
            textSize = Constants.LIMIT_LINE_TEXT_SIZE
            textColor = Color.WHITE
        }

        // draw limit lines behind data instead of on top
        yAxis.setDrawLimitLinesBehindData(true)
        xAxis.setDrawLimitLinesBehindData(true)

        // add limit lines to yAxis only
        yAxis.addLimitLine(ll1)
        yAxis.addLimitLine(ll2)
    }

    /**
     * Selected an entry
     */
    override fun onValueSelected(e: Entry?, h: Highlight?) {
    }

    override fun onNothingSelected() {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}