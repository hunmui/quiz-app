package self.eng.hocmaians.data

import androidx.room.Database
import androidx.room.RoomDatabase
import self.eng.hocmaians.data.entities.*

@Database(
    entities = [
        Course::class,
        Topic::class,
        Question::class,
        UserAnswer::class,
        Score::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val dao: AppDao
}