package com.goreecloud.keyboard

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeyboardSettingsLaunchRuntimeTest {
    @Test
    fun setupReplaySurvivesActivityRecreationAtCurrentStep() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("goreecloud_keyboard_setup", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("complete", true)
            .putInt("step", 0)
            .commit()

        ActivityScenario.launch(KeyboardSettingsActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val replay = findText(
                    activity.window.decorView,
                    activity.getString(R.string.keyboard_settings_run_setup),
                )
                assertNotNull(replay)
                replay!!.performClick()

                val continueButton = findText(
                    activity.window.decorView,
                    activity.getString(R.string.keyboard_setup_continue),
                )
                assertNotNull(continueButton)
                continueButton!!.performClick()
                assertNotNull(findText(activity.window.decorView, "Step 2 of 4"))
            }

            scenario.recreate()

            scenario.onActivity { activity ->
                assertNotNull(findText(activity.window.decorView, "Step 2 of 4"))
                assertNotNull(
                    findText(
                        activity.window.decorView,
                        activity.getString(R.string.keyboard_setup_replay_title),
                    ),
                )
            }
        }
    }

    @Test
    fun setupReplayFirstStepHasExplicitCloseAndPreservesCompletion() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val preferences = context.getSharedPreferences("goreecloud_keyboard_setup", Context.MODE_PRIVATE)
        preferences.edit()
            .putBoolean("complete", true)
            .putInt("step", 0)
            .commit()

        ActivityScenario.launch(KeyboardSettingsActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val replay = findText(
                    activity.window.decorView,
                    activity.getString(R.string.keyboard_settings_run_setup),
                )
                assertNotNull(replay)
                replay!!.performClick()

                assertNotNull(
                    findText(
                        activity.window.decorView,
                        activity.getString(R.string.keyboard_setup_replay_title),
                    ),
                )
                val closeReplay = findText(
                    activity.window.decorView,
                    activity.getString(R.string.keyboard_setup_close_replay),
                )
                assertNotNull(closeReplay)
                closeReplay!!.performClick()

                assertNotNull(
                    findText(
                        activity.window.decorView,
                        activity.getString(R.string.keyboard_settings_run_setup),
                    ),
                )
                assertEquals(true, preferences.getBoolean("complete", false))
            }
        }
    }

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
    private fun findText(root: View, expected: String): TextView? {
        if (root is TextView && root.text.toString() == expected) return root
        if (root is ViewGroup) {
            for (index in 0 until root.childCount) {
                findText(root.getChildAt(index), expected)?.let { return it }
            }
        }
        return null
    }

}
