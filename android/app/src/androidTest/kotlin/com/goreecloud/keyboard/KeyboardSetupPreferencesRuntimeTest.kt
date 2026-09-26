package com.goreecloud.keyboard

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeyboardSetupPreferencesRuntimeTest {
    @Test
    fun setupCompletionPersistsOnlyAfterFinish() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("goreecloud_keyboard_setup", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

        val first = KeyboardSetupPreferences(context)
        assertFalse(first.isComplete())

        first.markComplete()

        val reloaded = KeyboardSetupPreferences(context)
        assertTrue(reloaded.isComplete())
    }
}
