package self.eng.hocmaians.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import self.eng.hocmaians.R

/**
 * Custom marker which is used to display text when clicking a point in graph
 */
@SuppressLint("ViewConstructor")
class MyMarkerView(
    context: Context,
    layoutResource: Int
) : MarkerView(context, layoutResource) {

    // this is the text view in custom_marker_view.xml
    private val tvAttemptNumber: TextView = findViewById(R.id.tv_attempt_number)
    private val tvContent: TextView = findViewById(R.id.tv_line_chart_content)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e != null) {
            if (e is CandleEntry) {
                val candleEntry: CandleEntry = e
                tvContent.text = Utils.formatNumber(candleEntry.high, 0, true)
            } else {
                val attemptNumber = "${e.x.toInt()}:"
                tvAttemptNumber.text = attemptNumber
                tvContent.text = e.y.toString()
            }
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF.getInstance(-(width / 2.0f), -(height.toFloat()))
    }
}