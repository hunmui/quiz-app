package self.eng.hocmaians.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import self.eng.hocmaians.data.AppDatabase
import self.eng.hocmaians.repositories.AppRepository
import self.eng.hocmaians.repositories.IRepository
import self.eng.hocmaians.util.Constants
import self.eng.hocmaians.util.Constants.TEST_DB_PATH
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        Constants.DB_NAME
    )
        .createFromAsset(TEST_DB_PATH)
        .build()

    @Singleton
    @Provides
    fun provideAppRepository(
        db: AppDatabase
    ): IRepository = AppRepository(db.dao)
}