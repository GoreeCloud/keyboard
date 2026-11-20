package com.goreecloud.keyboard

import android.content.Context
import com.goreecloud.ui.material3.GoreeAccent

/** Stores only presentation preferences; never stores user-entered content. */
class KeyboardPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
    var accent: GoreeAccent
        get() = GoreeAccent.entries.firstOrNull { it.id == prefs.getString("accent", "iris") } ?: GoreeAccent.Iris
        set(value) { prefs.edit().putString("accent", value.id).apply() }

    var haptics: Boolean
        get() = prefs.getBoolean("haptics", false)
        set(value) { prefs.edit().putBoolean("haptics", value).apply() }
}
