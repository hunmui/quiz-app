package self.eng.hocmaians.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import self.eng.hocmaians.data.AppDatabase
import self.eng.hocmaians.util.Constants.TEST_DB
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {

    @Provides
    @Named(TEST_DB)
    fun provideInMemoryDb(
        @ApplicationContext context: Context
    ): AppDatabase = Room.inMemoryDatabaseBuilder(
        context,
        AppDatabase::class.java
    ).allowMainThreadQueries().build()
}