package com.goreecloud.keyboard

import android.content.Context

internal class KeyboardSetupPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun isComplete(): Boolean = preferences.getBoolean(COMPLETE, false)

    fun markComplete() {
        preferences.edit().putBoolean(COMPLETE, true).apply()
    }

    private companion object {
        const val NAME = "goreecloud_keyboard_setup"
        const val COMPLETE = "complete"
    }
}
