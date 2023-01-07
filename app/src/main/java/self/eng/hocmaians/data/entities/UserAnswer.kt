package self.eng.hocmaians.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_answers")
data class UserAnswer(
    @ColumnInfo(name = "ua_id") @PrimaryKey(autoGenerate = true) val uaId: Long = 0,
    @ColumnInfo(name = "answer_number") val answerNumber: Int,
    @ColumnInfo(name = "question_id") val questionId: Long,
    val timestamp: Long
)