package com.goreecloud.keyboard

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.inputmethod.InputMethodManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Emulator-backed validation of the installed IME declaration only.
 *
 * This does not enable GoreeCloud Keyboard, select it as the current IME, inject text into another
 * application, or establish representative-device input-routing acceptance. Those remain separate
 * Development/Release Candidate gates.
 */
@RunWith(AndroidJUnit4::class)
class KeyboardSystemRegistrationRuntimeTest {
    @Test
    fun installedServiceIsDiscoverableAsBoundInputMethod() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageManager = context.packageManager
        val component = ComponentName(context, KeyboardService::class.java)
        val serviceInfo = if (Build.VERSION.SDK_INT >= 33) {
            packageManager.getServiceInfo(
                component,
                PackageManager.ComponentInfoFlags.of(PackageManager.GET_META_DATA.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getServiceInfo(component, PackageManager.GET_META_DATA)
        }

        assertTrue("KeyboardService must remain exported for Android IME binding", serviceInfo.exported)
        assertEquals(
            "KeyboardService must be protected by Android's IME binding permission",
            Manifest.permission.BIND_INPUT_METHOD,
            serviceInfo.permission,
        )

        val inputMethodMetadata = serviceInfo.metaData?.getInt("android.view.im", 0) ?: 0
        assertNotEquals("KeyboardService must publish an input-method metadata resource", 0, inputMethodMetadata)

        context.resources.getXml(inputMethodMetadata).use { parser ->
            var event = parser.eventType
            while (event != org.xmlpull.v1.XmlPullParser.START_TAG && event != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                event = parser.next()
            }
            assertEquals("input-method", parser.name)
        }

        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val installed = inputMethodManager.inputMethodList.any { inputMethod ->
            inputMethod.packageName == context.packageName &&
                inputMethod.serviceName == KeyboardService::class.java.name
        }
        assertTrue(
            "Android must discover the installed GoreeCloud Keyboard service as an input method",
            installed,
        )

        val inputMethod = inputMethodManager.inputMethodList.first { inputMethod ->
            inputMethod.packageName == context.packageName &&
                inputMethod.serviceName == KeyboardService::class.java.name
        }
        assertEquals(
            "IME settings must open the first-party Keyboard settings surface",
            KeyboardSettingsActivity::class.java.name,
            inputMethod.settingsActivity,
        )

        val launcher = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setPackage(context.packageName)
        }
        val launcherActivities = packageManager.queryIntentActivities(launcher, 0)
        assertTrue(
            "The Development app must expose a launcher shortcut for Keyboard settings",
            launcherActivities.any { it.activityInfo.name == KeyboardSettingsActivity::class.java.name },
        )
    }
}
