package self.eng.hocmaians.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import self.eng.hocmaians.R
import java.text.DecimalFormat

@SuppressLint("ViewConstructor")
class RadarMarkerView(
    context: Context,
    layoutResource: Int
) : MarkerView(context, layoutResource) {
    private val tvContent: TextView = findViewById(R.id.tv_radar_chart_content)
    private val format: DecimalFormat = DecimalFormat("##0")

    // runs every time the MarkerView is re-drawn, can be used to update the
    // content (user-interface)
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        tvContent.text = String.format("%s %%", format.format(e?.y))
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF = MPPointF(-(width / 2).toFloat(), (-height - 10).toFloat())
}