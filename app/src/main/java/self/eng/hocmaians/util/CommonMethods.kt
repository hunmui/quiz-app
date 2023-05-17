package self.eng.hocmaians.util

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import self.eng.hocmaians.QuizApplication
import self.eng.hocmaians.R
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Lớp này chứa tất cả các phương thức được sử dụng trong toàn bộ ứng dụng
 */
object CommonMethods {

    /**
     * Chuyển đổi thời gian hệ thống tính bằng mili giây thành Ngày được định dạng: dd/MM/yyyy hh:mm a
     *
     * @param milliseconds thời gian hệ thống tính bằng mili giây
     * @return ngày định dạng
     */
    fun millisToDateTime(milliseconds: Long): String {

        val timeFormat = SimpleDateFormat(Constants.TIME_FORMAT_PATTERN, Locale.getDefault())
        val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT_PATTERN, Locale.getDefault())

        val time: String
        val date: String

        if (milliseconds >= 0) {
            time = timeFormat.format(milliseconds)
            date = dateFormat.format(milliseconds)
        } else {
            // some how seconds is < 0
            time = timeFormat.format(Constants.DEFAULT_MILLISECOND)
            date = dateFormat.format(Constants.DEFAULT_MILLISECOND)
        }
        return "$date $time"
    }

    /**
     * Tính điểm người dùng, hiển thị dưới dạng chuỗi với định dạng sau: x.x (7.8)
     *
     * @param totalCorrect tổng số câu trả lời đúng của người dùng
     * @param totalQuestions tổng số câu hỏi
     * @return điểm người dùng đã định dạng
     */
    fun userScoreInString(totalCorrect: Int, totalQuestions: Int): String {

        val calculatedScore: Double =
            if (totalCorrect > totalQuestions || totalQuestions <= 0 || totalCorrect < 0) {
                // somehow this situation happens
                Constants.INVALID_SCORE
            } else {
                (totalCorrect.toDouble() / totalQuestions) * 10.0
            }

        val decimalFormat = DecimalFormat("0.0")
        return decimalFormat.format(calculatedScore).replace(",", ".")
    }

    /**
     * Tạo hộp thoại cảnh báo để giúp người dùng.
     *
     * @param context bối cảnh cần một hộp thoại cảnh báo
     * @param title tiêu đề hộp thoại cảnh báo
     * @param message Hộp thoại cảnh báo
     */
    fun showHelpDialog(context: Context, title: String, message: String) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(context.getString(R.string.close)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Chuyển đổi người dùng/câu trả lời đúng ở dạng Số nguyên thành Văn bản
     *
     * @param index user/correct trả lời đúng trong Số nguyên
     * @return user/câu trả lời đúng trong Text
     */
    fun convertIndexToText(index: Int): String = when (index) {
        1 -> "A"
        2 -> "B"
        3 -> "C"
        4 -> "D"
        Constants.NOT_ANSWER_YET -> QuizApplication.resource.getString(R.string.not_yet_answered)
        else -> QuizApplication.resource.getString(R.string.invalid_answer_index)
    }
}