package self.eng.hocmaians.data.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "questions")
@Parcelize
data class Question(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    var question: String,
    @ColumnInfo(name = "option_1") var option1: String,
    @ColumnInfo(name = "option_2") var option2: String,
    @ColumnInfo(name = "option_3") var option3: String,
    @ColumnInfo(name = "option_4") var option4: String,
    @ColumnInfo(name = "correct_answer") var answerNr: Int,
    @ColumnInfo(name = "explanation") var explanation: String?,
    @ColumnInfo(name = "is_bookmark") var isBookmark: Int,
    @ColumnInfo(name = "is_user_added") val isUserAdded: Int,
    @ColumnInfo(name = "topic_id") val topicId: Int
) : Parcelable