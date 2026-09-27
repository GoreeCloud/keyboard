package com.goreecloud.keyboard

import android.content.Context

internal class KeyboardSetupPreferences(
    private val context: Context,
) {
    private val preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun isComplete(): Boolean {
        if (preferences.contains(COMPLETE)) {
            return preferences.getBoolean(COMPLETE, false)
        }

        if (isUpgradeInstall()) {
            preferences.edit()
                .putBoolean(COMPLETE, true)
                .putInt(STEP, 0)
                .commit()
            return true
        }

        return false
    }

    fun currentStep(): Int =
        preferences.getInt(STEP, 0).coerceIn(0, STEP_COUNT - 1)

    fun setCurrentStep(step: Int) {
        preferences.edit()
            .putInt(STEP, step.coerceIn(0, STEP_COUNT - 1))
            .commit()
    }

    fun markComplete() {
        preferences.edit()
            .putBoolean(COMPLETE, true)
            .putInt(STEP, 0)
            .commit()
    }

    private fun isUpgradeInstall(): Boolean =
        runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            info.lastUpdateTime > info.firstInstallTime
        }.getOrDefault(false)

    private companion object {
        const val NAME = "goreecloud_keyboard_setup"
        const val COMPLETE = "complete"
        const val STEP = "step"
        const val STEP_COUNT = 4
    }
}
