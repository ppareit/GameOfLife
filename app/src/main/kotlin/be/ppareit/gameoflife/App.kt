package be.ppareit.gameoflife

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        app = this
    }

    companion object {
        private val TAG = App::class.java.simpleName
        lateinit var app: App
            private set
        val settingsRepository: SettingsRepository by lazy { SettingsRepository(app) }

        fun getVersion(): String {
            val context = app.applicationContext
            val packageName = context.packageName
            return try {
                val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.getPackageInfo(
                        packageName,
                        PackageManager.PackageInfoFlags.of(0),
                    )
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getPackageInfo(packageName, 0)
                }
                packageInfo.versionName ?: ""
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e(TAG, "Unable to find the name $packageName in the package", e)
                ""
            }
        }
    }
}
