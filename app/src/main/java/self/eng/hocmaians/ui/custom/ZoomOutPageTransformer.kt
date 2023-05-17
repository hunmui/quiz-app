package self.eng.hocmaians.ui.custom

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs
import kotlin.math.max

private const val MIN_SCALE = 0.85f
private const val MIN_ALPHA = 0.5f

/**
 * Hoạt hình tùy chỉnh cho viewpager2
 */
class ZoomOutPageTransformer : ViewPager2.PageTransformer {

    override fun transformPage(view: View, position: Float) {
        view.apply {
            val pageWidth = width
            val pageHeight = height
            when {
                position < -1 -> { // [-Infinity,-1)
                    // Trang này nằm ngoài màn hình ở bên trái.
                    alpha = 0f
                }
                position <= 1 -> { // [-1,1]
                    // Sửa đổi chuyển tiếp slide mặc định để thu nhỏ trang
                    val scaleFactor = max(MIN_SCALE, 1 - abs(position))
                    val verticalMargin = pageHeight * (1 - scaleFactor) / 2
                    val horizontalMargin = pageWidth * (1 - scaleFactor) / 2
                    translationX = if (position < 0) {
                        horizontalMargin - verticalMargin / 2
                    } else {
                        horizontalMargin + verticalMargin / 2
                    }

                    // Thu nhỏ trang (giữa MIN_SCALE và 1)
                    scaleX = scaleFactor
                    scaleY = scaleFactor

                    // Làm mờ trang so với kích thước của nó.
                    alpha = (MIN_ALPHA +
                            (((scaleFactor - MIN_SCALE) / (1 - MIN_SCALE)) * (1 - MIN_ALPHA)))
                }
                else -> { // (1,+Infinity]
                    // Trang này nằm ngoài màn hình ở bên phải.
                    alpha = 0f
                }
            }
        }
    }
}