package self.eng.hocmaians.data.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "topics")
@Parcelize
data class Topic(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String,
    var priority: Long,
    @ColumnInfo(name = "is_user_added") val isUserAdded: Int,
    @ColumnInfo(name = "course_id") val courseId: Int
) : Parcelable {
    override fun toString(): String = this.name
}