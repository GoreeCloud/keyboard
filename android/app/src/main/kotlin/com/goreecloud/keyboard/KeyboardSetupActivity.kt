package com.goreecloud.keyboard

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView

/**
 * First-run setup wizard for enabling Keyboard and choosing initial device-local preferences.
 *
 * The wizard never requests network/account access and never reads clipboard payloads.
 */
class KeyboardSetupActivity : Activity() {
    private lateinit var setupPreferences: KeyboardSetupPreferences
    private lateinit var settingsStore: KeyboardSettingsStore
    private lateinit var clipboardPreferences: KeyboardClipboardPreferences
    private lateinit var palette: GlazeKeyboardTokens.Palette
    private var step = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupPreferences = KeyboardSetupPreferences(this)
        settingsStore = KeyboardSettingsStore(this)
        clipboardPreferences = KeyboardClipboardPreferences(this)
        palette = GlazeKeyboardTokens.palette(
            if (
                resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES
            ) GlazeKeyboardTokens.Appearance.DARK else GlazeKeyboardTokens.Appearance.LIGHT,
        )

        if (setupPreferences.isComplete() && !intent.getBooleanExtra(EXTRA_FORCE_SETUP, false)) {
            startActivity(Intent(this, KeyboardSettingsActivity::class.java))
            finish()
            return
        }

        render()
    }

    override fun onResume() {
        super.onResume()
        if (::setupPreferences.isInitialized && !isFinishing) render()
    }

    private fun render() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(24))
            setBackgroundColor(palette.canvasArgb)
        }

        root.addView(TextView(this).apply {
            text = getString(R.string.keyboard_setup_title)
            textSize = 28f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(palette.onSurfaceArgb)
        }, matchWidth())

        root.addView(TextView(this).apply {
            text = getString(R.string.keyboard_setup_progress, step + 1, TOTAL_STEPS)
            textSize = 13f
            setTextColor(palette.onSurfaceMutedArgb)
            setPadding(0, dp(4), 0, dp(18))
        }, matchWidth())

        when (step) {
            0 -> buildWelcomeStep(root)
            1 -> buildTypingStep(root)
            2 -> buildClipboardStep(root)
            else -> buildFinishStep(root)
        }

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(20), 0, 0)
        }
        if (step > 0) {
            nav.addView(button(getString(R.string.keyboard_setup_back)) {
                step -= 1
                render()
            }, LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginEnd = dp(8) })
        }
        nav.addView(button(
            if (step == TOTAL_STEPS - 1) {
                getString(R.string.keyboard_setup_finish)
            } else {
                getString(R.string.keyboard_setup_continue)
            },
        ) {
            if (step == TOTAL_STEPS - 1) {
                setupPreferences.markComplete()
                startActivity(Intent(this, KeyboardSettingsActivity::class.java))
                finish()
            } else {
                step += 1
                render()
            }
        }, LinearLayout.LayoutParams(0, dp(48), 1f))
        root.addView(nav, matchWidth())

        setContentView(ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(palette.canvasArgb)
            addView(root, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ))
        })
    }

    private fun buildWelcomeStep(root: LinearLayout) {
        root.addView(stepHeading(
            getString(R.string.keyboard_setup_welcome_heading),
            getString(R.string.keyboard_setup_welcome_summary),
        ), matchWidth())

        root.addView(statusCard(
            title = getString(R.string.keyboard_setup_enable_title),
            status = if (isKeyboardEnabled()) {
                getString(R.string.keyboard_setup_enabled)
            } else {
                getString(R.string.keyboard_setup_not_enabled)
            },
            action = getString(R.string.keyboard_setup_open_keyboard_settings),
        ) {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }, matchWidth())

        root.addView(statusCard(
            title = getString(R.string.keyboard_setup_select_title),
            status = if (isKeyboardSelected()) {
                getString(R.string.keyboard_setup_selected)
            } else {
                getString(R.string.keyboard_setup_not_selected)
            },
            action = getString(R.string.keyboard_setup_choose_keyboard),
        ) {
            (getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager)
                ?.showInputMethodPicker()
        }, matchWidth().apply { topMargin = dp(12) })
    }

    private fun buildTypingStep(root: LinearLayout) {
        root.addView(stepHeading(
            getString(R.string.keyboard_setup_typing_heading),
            getString(R.string.keyboard_setup_typing_summary),
        ), matchWidth())

        val current = settingsStore.load()
        val card = card()
        card.addView(switchRow(
            getString(R.string.keyboard_settings_suggestions),
            getString(R.string.keyboard_settings_suggestions_summary),
            current.suggestionsEnabled,
            settingsStore::setSuggestionsEnabled,
        ))
        card.addView(switchRow(
            getString(R.string.keyboard_settings_autocorrect),
            getString(R.string.keyboard_settings_autocorrect_summary),
            current.autocorrectEnabled,
            settingsStore::setAutocorrectEnabled,
        ))
        card.addView(switchRow(
            getString(R.string.keyboard_settings_predictions),
            getString(R.string.keyboard_settings_predictions_summary),
            current.predictionsEnabled,
            settingsStore::setPredictionsEnabled,
        ))
        card.addView(switchRow(
            getString(R.string.keyboard_settings_swipe),
            getString(R.string.keyboard_settings_swipe_summary),
            current.swipeTypingEnabled,
            settingsStore::setSwipeTypingEnabled,
        ))
        card.addView(switchRow(
            getString(R.string.keyboard_settings_number_row),
            getString(R.string.keyboard_settings_number_row_summary),
            current.numberRowEnabled,
            settingsStore::setNumberRowEnabled,
        ))
        card.addView(switchRow(
            getString(R.string.keyboard_settings_haptics),
            getString(R.string.keyboard_settings_haptics_summary),
            current.hapticFeedbackEnabled,
            settingsStore::setHapticFeedbackEnabled,
        ))
        card.addView(switchRow(
            getString(R.string.keyboard_settings_sound),
            getString(R.string.keyboard_settings_sound_summary),
            current.keyPressSoundEnabled,
            settingsStore::setKeyPressSoundEnabled,
        ))
        card.addView(switchRow(
            getString(R.string.keyboard_settings_learn_from_typing),
            getString(R.string.keyboard_settings_learn_from_typing_summary),
            current.learnFromTypingEnabled,
            settingsStore::setLearnFromTypingEnabled,
        ))
        root.addView(card, matchWidth())
    }

    private fun buildClipboardStep(root: LinearLayout) {
        root.addView(stepHeading(
            getString(R.string.keyboard_setup_clipboard_heading),
            getString(R.string.keyboard_setup_clipboard_summary),
        ), matchWidth())

        val card = card()
        card.addView(switchRow(
            getString(R.string.keyboard_settings_clipboard_history),
            getString(R.string.keyboard_settings_clipboard_history_summary),
            clipboardPreferences.historyEnabled(),
            clipboardPreferences::setHistoryEnabled,
        ))
        card.addView(TextView(this).apply {
            text = getString(R.string.keyboard_setup_clipboard_retention)
            textSize = 16f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setTextColor(palette.onSurfaceArgb)
            setPadding(0, dp(16), 0, dp(8))
        }, matchWidth())

        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        ClipboardRetention.entries.forEach { value ->
            val label = when (value) {
                ClipboardRetention.TEN_MINUTES -> "10 min"
                ClipboardRetention.ONE_HOUR -> "1 hour"
                ClipboardRetention.ONE_DAY -> "24 hours"
            }
            row.addView(button(label) {
                clipboardPreferences.setRetention(value)
                render()
            }, LinearLayout.LayoutParams(0, dp(44), 1f).apply {
                if (value != ClipboardRetention.ONE_DAY) marginEnd = dp(6)
            })
        }
        card.addView(row, matchWidth())

        card.addView(TextView(this).apply {
            text = getString(R.string.keyboard_setup_clipboard_privacy)
            textSize = 13.5f
            setTextColor(palette.onSurfaceMutedArgb)
            setPadding(0, dp(14), 0, 0)
        }, matchWidth())
        root.addView(card, matchWidth())
    }

    private fun buildFinishStep(root: LinearLayout) {
        root.addView(stepHeading(
            getString(R.string.keyboard_setup_finish_heading),
            getString(R.string.keyboard_setup_finish_summary),
        ), matchWidth())

        root.addView(card().apply {
            addView(TextView(this@KeyboardSetupActivity).apply {
                text = getString(
                    R.string.keyboard_setup_finish_status,
                    if (isKeyboardEnabled()) "Enabled" else "Not enabled",
                    if (isKeyboardSelected()) "Selected" else "Not selected",
                )
                textSize = 15f
                setTextColor(palette.onSurfaceArgb)
            }, matchWidth())
        }, matchWidth())
    }

    private fun isKeyboardEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_INPUT_METHODS,
        ).orEmpty()
        return enabled.contains(packageName)
    }

    private fun isKeyboardSelected(): Boolean {
        val selected = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD,
        ).orEmpty()
        return selected.startsWith(packageName + "/")
    }

    private fun stepHeading(title: String, summary: String): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(16))
            addView(TextView(this@KeyboardSetupActivity).apply {
                text = title
                textSize = 21f
                typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
                setTextColor(palette.onSurfaceArgb)
            }, matchWidth())
            addView(TextView(this@KeyboardSetupActivity).apply {
                text = summary
                textSize = 14.5f
                setTextColor(palette.onSurfaceMutedArgb)
                setPadding(0, dp(6), 0, 0)
            }, matchWidth())
        }

    private fun statusCard(
        title: String,
        status: String,
        action: String,
        onClick: () -> Unit,
    ): LinearLayout = card().apply {
        addView(TextView(this@KeyboardSetupActivity).apply {
            text = title
            textSize = 17f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(palette.onSurfaceArgb)
        }, matchWidth())
        addView(TextView(this@KeyboardSetupActivity).apply {
            text = status
            textSize = 14f
            setTextColor(palette.onSurfaceMutedArgb)
            setPadding(0, dp(4), 0, dp(12))
        }, matchWidth())
        addView(button(action, onClick), matchWidth().apply { height = dp(46) })
    }

    private fun switchRow(
        title: String,
        summary: String,
        checked: Boolean,
        onChecked: (Boolean) -> Unit,
    ): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(7), 0, dp(7))
        addView(LinearLayout(this@KeyboardSetupActivity).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(this@KeyboardSetupActivity).apply {
                text = title
                textSize = 16f
                setTextColor(palette.onSurfaceArgb)
            }, matchWidth())
            addView(TextView(this@KeyboardSetupActivity).apply {
                text = summary
                textSize = 13f
                setTextColor(palette.onSurfaceMutedArgb)
            }, matchWidth())
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        addView(Switch(this@KeyboardSetupActivity).apply {
            isChecked = checked
            setOnCheckedChangeListener { _, value -> onChecked(value) }
        })
    }

    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(14), dp(16), dp(14))
        background = GradientDrawable().apply {
            cornerRadius = dp(20).toFloat()
            setColor(palette.surfaceArgb)
            setStroke(dp(1), palette.lineArgb)
        }
    }

    private fun button(label: String, onClick: () -> Unit): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 14f
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                cornerRadius = dp(14).toFloat()
                setColor(resolveAccent())
            }
            setOnClickListener { onClick() }
        }

    private fun resolveAccent(): Int {
        val value = android.util.TypedValue()
        return if (theme.resolveAttribute(android.R.attr.colorAccent, value, true)) {
            if (value.resourceId != 0) getColor(value.resourceId) else value.data
        } else {
            palette.onSurfaceArgb
        }
    }

    private fun matchWidth(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        const val EXTRA_FORCE_SETUP = "force_setup"
        private const val TOTAL_STEPS = 4
    }
}
