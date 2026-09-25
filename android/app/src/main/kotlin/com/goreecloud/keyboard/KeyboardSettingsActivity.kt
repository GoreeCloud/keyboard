package com.goreecloud.keyboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView

/**
 * Device-local settings surface for the native Keyboard runtime.
 *
 * These controls change only local presentation/typing behavior. They do not grant editor,
 * clipboard, network, account, telemetry, or learning authority.
 */
class KeyboardSettingsActivity : Activity() {
    private lateinit var settingsStore: KeyboardSettingsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.keyboard_settings_title)
        settingsStore = KeyboardSettingsStore(this)
        setContentView(buildContent())
    }

    private fun buildContent(): ScrollView {
        val current = settingsStore.load()
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val padding = dp(24)
            setPadding(padding, padding, padding, padding)
        }

        content.addView(TextView(this).apply {
            text = getString(R.string.keyboard_settings_title)
            textSize = 28f
        }, matchWidth())

        content.addView(TextView(this).apply {
            text = getString(R.string.keyboard_settings_summary)
            textSize = 16f
            setPadding(0, dp(8), 0, dp(24))
        }, matchWidth())

        content.addView(sectionTitle(getString(R.string.keyboard_settings_typing_section)), matchWidth())

        content.addView(settingSwitch(
            title = getString(R.string.keyboard_settings_swipe),
            summary = getString(R.string.keyboard_settings_swipe_summary),
            checked = current.swipeTypingEnabled,
            onChecked = settingsStore::setSwipeTypingEnabled,
        ), matchWidth())

        content.addView(settingSwitch(
            title = getString(R.string.keyboard_settings_suggestions),
            summary = getString(R.string.keyboard_settings_suggestions_summary),
            checked = current.suggestionsEnabled,
            onChecked = settingsStore::setSuggestionsEnabled,
        ), matchWidth())

        content.addView(settingSwitch(
            title = getString(R.string.keyboard_settings_autocorrect),
            summary = getString(R.string.keyboard_settings_autocorrect_summary),
            checked = current.autocorrectEnabled,
            onChecked = settingsStore::setAutocorrectEnabled,
        ), matchWidth())

        content.addView(settingSwitch(
            title = getString(R.string.keyboard_settings_predictions),
            summary = getString(R.string.keyboard_settings_predictions_summary),
            checked = current.predictionsEnabled,
            onChecked = settingsStore::setPredictionsEnabled,
        ), matchWidth())

        content.addView(settingSwitch(
            title = getString(R.string.keyboard_settings_auto_capitalize),
            summary = getString(R.string.keyboard_settings_auto_capitalize_summary),
            checked = current.autoCapitalizeEnabled,
            onChecked = settingsStore::setAutoCapitalizeEnabled,
        ), matchWidth())

        content.addView(sectionTitle(getString(R.string.keyboard_settings_appearance_section)), matchWidth())

        content.addView(TextView(this).apply {
            text = getString(R.string.keyboard_settings_key_height)
            textSize = 17f
        }, matchWidth())

        content.addView(TextView(this).apply {
            text = getString(R.string.keyboard_settings_key_height_summary)
            setPadding(0, dp(4), 0, dp(8))
        }, matchWidth())

        val heightGroup = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
        }
        KeyboardKeyHeight.values().forEach { option ->
            heightGroup.addView(RadioButton(this).apply {
                id = View.generateViewId()
                tag = option
                text = when (option) {
                    KeyboardKeyHeight.COMPACT -> getString(R.string.keyboard_settings_key_height_compact)
                    KeyboardKeyHeight.STANDARD -> getString(R.string.keyboard_settings_key_height_standard)
                    KeyboardKeyHeight.TALL -> getString(R.string.keyboard_settings_key_height_tall)
                }
                isChecked = option == current.keyHeight
                minHeight = dp(GlazeKeyboardTokens.GeneralInteractionFloorDp.toInt())
            })
        }
        heightGroup.setOnCheckedChangeListener { group, checkedId ->
            val selected = group.findViewById<RadioButton>(checkedId)?.tag as? KeyboardKeyHeight
            if (selected != null) settingsStore.setKeyHeight(selected)
        }
        content.addView(heightGroup, matchWidth())

        content.addView(sectionTitle(getString(R.string.keyboard_settings_dictionary_section)), matchWidth())

        content.addView(TextView(this).apply {
            text = getString(R.string.keyboard_settings_dictionary_summary)
            textSize = 16f
            setPadding(0, dp(4), 0, dp(16))
        }, matchWidth())

        content.addView(Button(this).apply {
            text = getString(R.string.keyboard_settings_portable_preferences)
            minHeight = dp(GlazeKeyboardTokens.GeneralInteractionFloorDp.toInt())
            setOnClickListener {
                startActivity(Intent(this@KeyboardSettingsActivity, KeyboardPortablePreferencesActivity::class.java))
            }
        }, matchWidth())

        content.addView(Button(this).apply {
            text = getString(R.string.keyboard_settings_android_settings)
            minHeight = dp(GlazeKeyboardTokens.GeneralInteractionFloorDp.toInt())
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        }, matchWidth())

        return ScrollView(this).apply {
            isFillViewport = true
            addView(
                content,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ),
            )
        }
    }

    private fun settingSwitch(
        title: String,
        summary: String,
        checked: Boolean,
        onChecked: (Boolean) -> Unit,
    ): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(0, dp(4), 0, dp(12))

        addView(Switch(this@KeyboardSettingsActivity).apply {
            text = title
            textSize = 17f
            isChecked = checked
            minHeight = dp(GlazeKeyboardTokens.GeneralInteractionFloorDp.toInt())
            setOnCheckedChangeListener { _, value -> onChecked(value) }
        }, matchWidth())

        addView(TextView(this@KeyboardSettingsActivity).apply {
            text = summary
            setPadding(dp(4), 0, 0, 0)
        }, matchWidth())
    }

    private fun sectionTitle(value: String): TextView = TextView(this).apply {
        text = value
        textSize = 20f
        setPadding(0, dp(20), 0, dp(10))
    }

    private fun matchWidth(): LinearLayout.LayoutParams = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
    )

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
