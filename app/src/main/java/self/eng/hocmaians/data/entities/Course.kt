package self.eng.hocmaians.data.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "courses")
@Parcelize
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String,
    var priority: Long
) : Parcelable {
    override fun toString(): String = this.name
}