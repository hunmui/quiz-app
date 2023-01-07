package self.eng.hocmaians.util

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import self.eng.hocmaians.QuizApplication
import self.eng.hocmaians.R
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * This class contains all the methods which are used in the whole app
 */
object CommonMethods {

    /**
     * Convert system time in millisecond to Formatted date: dd/MM/yyyy hh:mm a
     *
     * @param milliseconds system time in millisecond
     * @return the formatted date
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
     * Calculate user score, display it in form of a string with the following format: x.x (7.8)
     *
     * @param totalCorrect user total correct answers
     * @param totalQuestions total questions
     * @return the formatted user score
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
     * Create an alert dialog to help user.
     *
     * @param context the context which needs an alert dialog
     * @param title alert dialog title
     * @param message alert dialog message
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
     * Convert user/correct answer in form of an Integer to Text
     *
     * @param index user/correct answer in Integer
     * @return user/correct answer in Text
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