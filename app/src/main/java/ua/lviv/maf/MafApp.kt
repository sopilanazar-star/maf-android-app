package ua.lviv.maf

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MafApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Цей код автоматично застосує налаштування до КОЖНОГО екрана в додатку
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                val insetsController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)

                // Робимо появу панелі по свайпу (щоб вона не перекривала контент назавжди)
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

                // Ховаємо ВСІ системні панелі (і нижні кнопки, і верхній годинник з батареєю)
                insetsController.hide(WindowInsetsCompat.Type.systemBars())

                // 🔥 Якщо хочеш ПОВНІСТЮ як в іграх (сховати ще й верхню панель з батареєю і часом),
                // то закоментуй рядок вище і розкоментуй цей:
                // insetsController.hide(WindowInsetsCompat.Type.systemBars())
            }

            // Інші обов'язкові методи інтерфейсу (просто залишаємо порожніми)
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}