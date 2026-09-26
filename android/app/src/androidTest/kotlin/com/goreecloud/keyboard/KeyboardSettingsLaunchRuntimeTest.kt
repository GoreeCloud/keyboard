package com.goreecloud.keyboard

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeyboardSettingsLaunchRuntimeTest {
    @Test
    fun launcherActivityReachesResumedState() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("goreecloud_keyboard_setup", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("complete", false)
            .commit()

        ActivityScenario.launch(KeyboardSettingsActivity::class.java).use { scenario ->
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
        }
    }
}
