package self.eng.hocmaians.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scores")
data class Score(
    @PrimaryKey(autoGenerate = false) val timestamp: Long,
    @ColumnInfo(name = "topic_id") val topicId: Int,
    @ColumnInfo(name = "total_correct") val totalCorrect: Int,
    @ColumnInfo(name = "total_questions") val totalQuestions: Int
)