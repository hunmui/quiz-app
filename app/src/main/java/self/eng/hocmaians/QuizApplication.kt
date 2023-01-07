package self.eng.hocmaians

import android.app.Application
import android.content.res.Resources
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QuizApplication : Application() {

    companion object {
        lateinit var resource: Resources
    }

    override fun onCreate() {
        super.onCreate()

        resource = resources
    }
}