package com.goreecloud.keyboard

import android.content.Context

/**
 * Device-local clipboard policy preferences. Clipboard payloads are never stored here.
 *
 * History defaults off. Per-app policy is created only for the current editor application when
 * the user changes it; Keyboard does not enumerate installed applications.
 */
internal class KeyboardClipboardPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun historyEnabled(): Boolean = preferences.getBoolean(HISTORY_ENABLED, false)

    fun setHistoryEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(HISTORY_ENABLED, enabled).apply()
    }

    fun retention(): ClipboardRetention =
        runCatching {
            ClipboardRetention.valueOf(
                preferences.getString(RETENTION, ClipboardRetention.TEN_MINUTES.name)
                    ?: ClipboardRetention.TEN_MINUTES.name,
            )
        }.getOrDefault(ClipboardRetention.TEN_MINUTES)

    fun setRetention(retention: ClipboardRetention) {
        preferences.edit().putString(RETENTION, retention.name).apply()
    }

    fun policyFor(packageName: String?): ClipboardAppPolicy {
        if (packageName.isNullOrBlank()) return ClipboardAppPolicy.BLOCK
        return runCatching {
            ClipboardAppPolicy.valueOf(
                preferences.getString(policyKey(packageName), ClipboardAppPolicy.ASK.name)
                    ?: ClipboardAppPolicy.ASK.name,
            )
        }.getOrDefault(ClipboardAppPolicy.ALLOW)
    }

    fun setPolicy(packageName: String?, policy: ClipboardAppPolicy) {
        if (packageName.isNullOrBlank()) return
        preferences.edit().putString(policyKey(packageName), policy.name).apply()
    }

    private fun policyKey(packageName: String): String = "app_policy_" + packageName

    private companion object {
        const val NAME = "goreecloud_keyboard_clipboard_policy"
        const val HISTORY_ENABLED = "history_enabled"
        const val RETENTION = "history_retention"
    }
}
