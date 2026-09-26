package com.goreecloud.keyboard

import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeyboardInputSurfaceHostRuntimeTest {
    @Test
    fun auxiliarySurfaceCanReturnToKeyboardSurface() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val host = KeyboardInputSurfaceHost(context)
        val keyboard = View(context)
        val clipboard = View(context)

        host.showSurface(keyboard)
        assertSame(keyboard, host.currentSurface())

        host.showSurface(clipboard)
        assertSame(clipboard, host.currentSurface())

        host.showSurface(keyboard)
        assertSame(keyboard, host.currentSurface())
    }
}
