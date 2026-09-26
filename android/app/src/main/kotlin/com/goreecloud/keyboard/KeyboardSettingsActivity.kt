package com.goreecloud.keyboard

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Device-local settings surface for the native Keyboard runtime.
 *
 * These controls change only local presentation/typing behavior. Optional learning is an explicit,
 * off-by-default grant to retain bounded device-local word-frequency counters. No setting grants
 * clipboard, network, account, telemetry, or sensitive-editor authority.
 */
class KeyboardSettingsActivity : Activity() {
    private lateinit var settingsStore: KeyboardSettingsStore
    private lateinit var learningStore: KeyboardLearningStore
    private lateinit var setupPreferences: KeyboardSetupPreferences
    private lateinit var palette: GlazeKeyboardTokens.Palette
    private var setupStep: Int = 0
    private var setupWizardActive: Boolean = false
    private var accentColor: Int = 0xFF2563EB.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsStore = KeyboardSettingsStore(this)
        learningStore = KeyboardLearningStore(this)
        setupPreferences = KeyboardSetupPreferences(this)
        palette = currentPalette()

        window.statusBarColor = palette.canvasArgb
        window.navigationBarColor = palette.canvasArgb
        window.decorView.systemUiVisibility =
            if (isDarkAppearance()) 0
            else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        if (setupPreferences.isComplete()) {
            setContentView(buildContent())
        } else {
            renderSetupWizard()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::setupPreferences.isInitialized && setupWizardActive) {
            renderSetupWizard()
        }
    }

    private fun renderSetupWizard() {
        setupWizardActive = true
        setContentView(buildSetupWizard())
    }

    private fun buildSetupWizard(): ScrollView {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(28))
            setBackgroundColor(palette.canvasArgb)
        }

        root.addView(TextView(this).apply {
            text = getString(R.string.keyboard_setup_title)
            textSize = 28f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(palette.onSurfaceArgb)
        }, matchWidth())

        root.addView(TextView(this).apply {
            text = getString(R.string.keyboard_setup_progress, setupStep + 1, 4)
            textSize = 13f
            setTextColor(palette.onSurfaceMutedArgb)
            setPadding(0, dp(4), 0, dp(18))
        }, matchWidth())

        when (setupStep) {
            0 -> buildSetupActivationStep(root)
            1 -> buildSetupTypingStep(root)
            2 -> buildSetupClipboardStep(root)
            else -> buildSetupFinishStep(root)
        }

        val navigation = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(20), 0, 0)
        }

        if (setupStep > 0) {
            navigation.addView(
                actionButton(getString(R.string.keyboard_setup_back)) {
                    setupStep -= 1
                    renderSetupWizard()
                },
                LinearLayout.LayoutParams(0, dp(56), 1f).apply {
                    marginEnd = dp(8)
                },
            )
        }

        navigation.addView(
            actionButton(
                if (setupStep == 4 - 1) {
                    getString(R.string.keyboard_setup_finish)
                } else {
                    getString(R.string.keyboard_setup_continue)
                },
            ) {
                if (setupStep == 4 - 1) {
                    setupPreferences.markComplete()
                    setupWizardActive = false
                    setContentView(buildContent())
                } else {
                    setupStep += 1
                    renderSetupWizard()
                }
            },
            LinearLayout.LayoutParams(0, dp(56), 1f),
        )

        root.addView(navigation, matchWidth())

        return ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(palette.canvasArgb)
            addView(
                root,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ),
            )
        }
    }

    private fun buildSetupActivationStep(root: LinearLayout) {
        root.addView(
            setupHeading(
                getString(R.string.keyboard_setup_welcome_heading),
                getString(R.string.keyboard_setup_welcome_summary),
            ),
            matchWidth(),
        )

        root.addView(card().apply {
            addView(TextView(this@KeyboardSettingsActivity).apply {
                text = getString(R.string.keyboard_setup_enable_title)
                textSize = 17f
                typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
                setTextColor(palette.onSurfaceArgb)
            }, matchWidth())
            addView(TextView(this@KeyboardSettingsActivity).apply {
                text = if (isGoreeCloudKeyboardEnabled()) {
                    getString(R.string.keyboard_setup_enabled)
                } else {
                    getString(R.string.keyboard_setup_not_enabled)
                }
                textSize = 14f
                setTextColor(palette.onSurfaceMutedArgb)
                setPadding(0, dp(5), 0, dp(12))
            }, matchWidth())
            addView(actionButton(getString(R.string.keyboard_setup_open_keyboard_settings)) {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }, matchWidth())
        }, matchWidth())

        root.addView(card().apply {
            addView(TextView(this@KeyboardSettingsActivity).apply {
                text = getString(R.string.keyboard_setup_select_title)
                textSize = 17f
                typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
                setTextColor(palette.onSurfaceArgb)
            }, matchWidth())
            addView(TextView(this@KeyboardSettingsActivity).apply {
                text = if (isGoreeCloudKeyboardSelected()) {
                    getString(R.string.keyboard_setup_selected)
                } else {
                    getString(R.string.keyboard_setup_not_selected)
                }
                textSize = 14f
                setTextColor(palette.onSurfaceMutedArgb)
                setPadding(0, dp(5), 0, dp(12))
            }, matchWidth())
            addView(actionButton(getString(R.string.keyboard_setup_choose_keyboard)) {
                (getSystemService(INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager)
                    ?.showInputMethodPicker()
            }, matchWidth())
        }, matchWidth().apply { topMargin = dp(12) })
    }

    private fun buildSetupTypingStep(root: LinearLayout) {
        root.addView(
            setupHeading(
                getString(R.string.keyboard_setup_typing_heading),
                getString(R.string.keyboard_setup_typing_summary),
            ),
            matchWidth(),
        )

        val current = settingsStore.load()
        root.addView(card().apply {
            addView(settingRow(
                getString(R.string.keyboard_settings_suggestions),
                getString(R.string.keyboard_settings_suggestions_summary),
                current.suggestionsEnabled,
                settingsStore::setSuggestionsEnabled,
            ), matchWidth())
            addDivider()
            addView(settingRow(
                getString(R.string.keyboard_settings_autocorrect),
                getString(R.string.keyboard_settings_autocorrect_summary),
                current.autocorrectEnabled,
                settingsStore::setAutocorrectEnabled,
            ), matchWidth())
            addDivider()
            addView(settingRow(
                getString(R.string.keyboard_settings_predictions),
                getString(R.string.keyboard_settings_predictions_summary),
                current.predictionsEnabled,
                settingsStore::setPredictionsEnabled,
            ), matchWidth())
            addDivider()
            addView(settingRow(
                getString(R.string.keyboard_settings_auto_capitalize),
                getString(R.string.keyboard_settings_auto_capitalize_summary),
                current.autoCapitalizeEnabled,
                settingsStore::setAutoCapitalizeEnabled,
            ), matchWidth())
            addDivider()
            addView(settingRow(
                getString(R.string.keyboard_settings_double_space_period),
                getString(R.string.keyboard_settings_double_space_period_summary),
                current.doubleSpacePeriodEnabled,
                settingsStore::setDoubleSpacePeriodEnabled,
            ), matchWidth())
            addDivider()
            addView(settingRow(
                getString(R.string.keyboard_settings_swipe),
                getString(R.string.keyboard_settings_swipe_summary),
                current.swipeTypingEnabled,
                settingsStore::setSwipeTypingEnabled,
            ), matchWidth())
            addDivider()
            addView(settingRow(
                getString(R.string.keyboard_settings_swipe_trail),
                getString(R.string.keyboard_settings_swipe_trail_summary),
                current.swipeTrailEnabled,
                settingsStore::setSwipeTrailEnabled,
            ), matchWidth())
            addDivider()
            addView(settingRow(
                getString(R.string.keyboard_settings_spacebar_cursor),
                getString(R.string.keyboard_settings_spacebar_cursor_summary),
                current.spacebarCursorControlEnabled,
                settingsStore::setSpacebarCursorControlEnabled,
            ), matchWidth())
            addDivider()
            addView(settingRow(
                getString(R.string.keyboard_settings_number_row),
                getString(R.string.keyboard_settings_number_row_summary),
                current.numberRowEnabled,
                settingsStore::setNumberRowEnabled,
            ), matchWidth())
            addDivider()
            addView(TextView(this@KeyboardSettingsActivity).apply {
                text = getString(R.string.keyboard_settings_key_height)
                textSize = 17f
                setTextColor(palette.onSurfaceArgb)
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            }, matchWidth())
            addView(buildHeightSegment(current.keyHeight), matchWidth())
            addDivider()
            addView(TextView(this@KeyboardSettingsActivity).apply {
                text = getString(R.string.keyboard_settings_toolbar_style)
                textSize = 17f
                setTextColor(palette.onSurfaceArgb)
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            }, matchWidth())
            addView(buildToolbarStyleSegment(current.toolbarStyle), matchWidth())
            addDivider()
            addView(settingRow(
                getString(R.string.keyboard_settings_emoji_toolbar),
                getString(R.string.keyboard_settings_emoji_toolbar_summary),
                current.emojiToolbarEnabled,
                settingsStore::setEmojiToolbarEnabled,
            ), matchWidth())
            addDivider()
            addView(settingRow(
                getString(R.string.keyboard_settings_long_press_hints),
                getString(R.string.keyboard_settings_long_press_hints_summary),
                current.longPressHintsEnabled,
                settingsStore::setLongPressHintsEnabled,
            ), matchWidth())
            addDivider()
            addView(TextView(this@KeyboardSettingsActivity).apply {
                text = getString(R.string.keyboard_settings_long_press_delay)
                textSize = 17f
                setTextColor(palette.onSurfaceArgb)
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            }, matchWidth())
            addView(buildLongPressDelaySegment(current.longPressDelay), matchWidth())
            addDivider()
            addView(settingRow(
                getString(R.string.keyboard_settings_haptics),
                getString(R.string.keyboard_settings_haptics_summary),
                current.hapticFeedbackEnabled,
                settingsStore::setHapticFeedbackEnabled,
            ), matchWidth())
            addDivider()
            addView(settingRow(
                getString(R.string.keyboard_settings_sound),
                getString(R.string.keyboard_settings_sound_summary),
                current.keyPressSoundEnabled,
                settingsStore::setKeyPressSoundEnabled,
            ), matchWidth())
            addDivider()
            addView(settingRow(
                getString(R.string.keyboard_settings_learn_from_typing),
                getString(R.string.keyboard_settings_learn_from_typing_summary),
                current.learnFromTypingEnabled,
                settingsStore::setLearnFromTypingEnabled,
            ), matchWidth())
        }, matchWidth())
    }

    private fun buildSetupClipboardStep(root: LinearLayout) {
        root.addView(
            setupHeading(
                getString(R.string.keyboard_setup_clipboard_heading),
                getString(R.string.keyboard_setup_clipboard_summary),
            ),
            matchWidth(),
        )

        val clipboardPreferences = KeyboardClipboardPreferences(this)
        root.addView(card().apply {
            addView(settingRow(
                getString(R.string.keyboard_settings_clipboard_history),
                getString(R.string.keyboard_settings_clipboard_history_summary),
                clipboardPreferences.historyEnabled(),
                clipboardPreferences::setHistoryEnabled,
            ), matchWidth())
            addDivider()
            addView(TextView(this@KeyboardSettingsActivity).apply {
                text = getString(R.string.keyboard_setup_clipboard_retention)
                textSize = 17f
                setTextColor(palette.onSurfaceArgb)
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            }, matchWidth())
            addView(
                buildClipboardRetentionSegment(
                    selected = clipboardPreferences.retention(),
                    onSelected = clipboardPreferences::setRetention,
                ),
                matchWidth(),
            )
            addView(TextView(this@KeyboardSettingsActivity).apply {
                text = getString(R.string.keyboard_setup_clipboard_privacy)
                textSize = 13.5f
                setTextColor(palette.onSurfaceMutedArgb)
                setPadding(0, dp(14), 0, 0)
            }, matchWidth())
        }, matchWidth())
    }

    private fun buildSetupFinishStep(root: LinearLayout) {
        root.addView(
            setupHeading(
                getString(R.string.keyboard_setup_finish_heading),
                getString(R.string.keyboard_setup_finish_summary),
            ),
            matchWidth(),
        )

        root.addView(card().apply {
            addView(TextView(this@KeyboardSettingsActivity).apply {
                text = getString(
                    R.string.keyboard_setup_finish_status,
                    if (isGoreeCloudKeyboardEnabled()) "Enabled" else "Not enabled",
                    if (isGoreeCloudKeyboardSelected()) "Selected" else "Not selected",
                )
                textSize = 15f
                setTextColor(palette.onSurfaceArgb)
            }, matchWidth())
        }, matchWidth())
    }

    private fun setupHeading(title: String, summary: String): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(16))
            addView(TextView(this@KeyboardSettingsActivity).apply {
                text = title
                textSize = 21f
                typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
                setTextColor(palette.onSurfaceArgb)
            }, matchWidth())
            addView(TextView(this@KeyboardSettingsActivity).apply {
                text = summary
                textSize = 14.5f
                setTextColor(palette.onSurfaceMutedArgb)
                setPadding(0, dp(6), 0, 0)
            }, matchWidth())
        }

    private fun isGoreeCloudKeyboardEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_INPUT_METHODS,
        ).orEmpty()
        return enabled.contains(packageName)
    }

    private fun isGoreeCloudKeyboardSelected(): Boolean {
        val selected = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD,
        ).orEmpty()
        return selected.startsWith(packageName + "/")
    }

    private fun buildContent(): ScrollView {
        val current = settingsStore.load()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(28))
            setBackgroundColor(palette.canvasArgb)
        }
        root.addView(buildHeader(), matchWidth())
        root.addView(sectionLabel(getString(R.string.keyboard_settings_typing_section)), matchWidth())

        val typingCard = card().apply {
            addView(settingRow(getString(R.string.keyboard_settings_suggestions), getString(R.string.keyboard_settings_suggestions_summary), current.suggestionsEnabled, settingsStore::setSuggestionsEnabled), matchWidth())
            addDivider()
            addView(settingRow(getString(R.string.keyboard_settings_autocorrect), getString(R.string.keyboard_settings_autocorrect_summary), current.autocorrectEnabled, settingsStore::setAutocorrectEnabled), matchWidth())
            addDivider()
            addView(settingRow(getString(R.string.keyboard_settings_predictions), getString(R.string.keyboard_settings_predictions_summary), current.predictionsEnabled, settingsStore::setPredictionsEnabled), matchWidth())
            addDivider()
            addView(settingRow(getString(R.string.keyboard_settings_auto_capitalize), getString(R.string.keyboard_settings_auto_capitalize_summary), current.autoCapitalizeEnabled, settingsStore::setAutoCapitalizeEnabled), matchWidth())
            addDivider()
            addView(settingRow(getString(R.string.keyboard_settings_double_space_period), getString(R.string.keyboard_settings_double_space_period_summary), current.doubleSpacePeriodEnabled, settingsStore::setDoubleSpacePeriodEnabled), matchWidth())
        }
        root.addView(typingCard, matchWidth().apply { bottomMargin = dp(22) })

        root.addView(sectionLabel(getString(R.string.keyboard_settings_layout_section)), matchWidth())
        val layoutCard = card().apply {
            addView(settingRow(getString(R.string.keyboard_settings_number_row), getString(R.string.keyboard_settings_number_row_summary), current.numberRowEnabled, settingsStore::setNumberRowEnabled), matchWidth())
            addDivider()
            addView(settingRow(getString(R.string.keyboard_settings_number_row_sensitive), getString(R.string.keyboard_settings_number_row_sensitive_summary), current.numberRowInSensitiveFieldsEnabled, settingsStore::setNumberRowInSensitiveFieldsEnabled), matchWidth())
            addDivider()
            addView(TextView(this@KeyboardSettingsActivity).apply {
                text = getString(R.string.keyboard_settings_key_height)
                textSize = 17f
                setTextColor(palette.onSurfaceArgb)
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            }, matchWidth())
            addView(TextView(this@KeyboardSettingsActivity).apply {
                text = getString(R.string.keyboard_settings_key_height_summary)
                textSize = 14f
                setTextColor(palette.onSurfaceMutedArgb)
                setPadding(0, dp(5), 0, dp(14))
            }, matchWidth())
            addView(buildHeightSegment(current.keyHeight), matchWidth())
        }
        root.addView(layoutCard, matchWidth().apply { bottomMargin = dp(22) })

        root.addView(sectionLabel(getString(R.string.keyboard_settings_gesture_section)), matchWidth())
        val gestureCard = card().apply {
            addView(settingRow(getString(R.string.keyboard_settings_swipe), getString(R.string.keyboard_settings_swipe_summary), current.swipeTypingEnabled, settingsStore::setSwipeTypingEnabled), matchWidth())
            addDivider()
            addView(settingRow(getString(R.string.keyboard_settings_swipe_trail), getString(R.string.keyboard_settings_swipe_trail_summary), current.swipeTrailEnabled, settingsStore::setSwipeTrailEnabled), matchWidth())
            addDivider()
            addView(settingRow(getString(R.string.keyboard_settings_spacebar_cursor), getString(R.string.keyboard_settings_spacebar_cursor_summary), current.spacebarCursorControlEnabled, settingsStore::setSpacebarCursorControlEnabled), matchWidth())
        }
        root.addView(gestureCard, matchWidth().apply { bottomMargin = dp(22) })

        root.addView(sectionLabel(getString(R.string.keyboard_settings_appearance_section)), matchWidth())
        val appearanceCard = card().apply {
            addView(TextView(this@KeyboardSettingsActivity).apply {
                text = getString(R.string.keyboard_settings_toolbar_style)
                textSize = 17f
                setTextColor(palette.onSurfaceArgb)
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            }, matchWidth())
            addView(TextView(this@KeyboardSettingsActivity).apply {
                text = getString(R.string.keyboard_settings_toolbar_style_summary)
                textSize = 14f
                setTextColor(palette.onSurfaceMutedArgb)
                setPadding(0, dp(5), 0, dp(14))
            }, matchWidth())
            addView(buildToolbarStyleSegment(current.toolbarStyle), matchWidth())
            addDivider()
            addView(settingRow(getString(R.string.keyboard_settings_emoji_toolbar), getString(R.string.keyboard_settings_emoji_toolbar_summary), current.emojiToolbarEnabled, settingsStore::setEmojiToolbarEnabled), matchWidth())
            addDivider()
            addView(settingRow(getString(R.string.keyboard_settings_long_press_hints), getString(R.string.keyboard_settings_long_press_hints_summary), current.longPressHintsEnabled, settingsStore::setLongPressHintsEnabled), matchWidth())
        }
        root.addView(appearanceCard, matchWidth().apply { bottomMargin = dp(22) })

        root.addView(sectionLabel(getString(R.string.keyboard_settings_feedback_section)), matchWidth())
        val feedbackCard = card().apply {
            addView(settingRow(getString(R.string.keyboard_settings_haptics), getString(R.string.keyboard_settings_haptics_summary), current.hapticFeedbackEnabled, settingsStore::setHapticFeedbackEnabled), matchWidth())
            addDivider()
            addView(settingRow(getString(R.string.keyboard_settings_sound), getString(R.string.keyboard_settings_sound_summary), current.keyPressSoundEnabled, settingsStore::setKeyPressSoundEnabled), matchWidth())
            addDivider()
            addView(TextView(this@KeyboardSettingsActivity).apply {
                text = getString(R.string.keyboard_settings_long_press_delay)
                textSize = 17f
                setTextColor(palette.onSurfaceArgb)
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            }, matchWidth())
            addView(TextView(this@KeyboardSettingsActivity).apply {
                text = getString(R.string.keyboard_settings_long_press_delay_summary)
                textSize = 14f
                setTextColor(palette.onSurfaceMutedArgb)
                setPadding(0, dp(5), 0, dp(14))
            }, matchWidth())
            addView(buildLongPressDelaySegment(current.longPressDelay), matchWidth())
        }
        root.addView(feedbackCard, matchWidth().apply { bottomMargin = dp(22) })

        root.addView(sectionLabel(getString(R.string.keyboard_settings_dictionary_section)), matchWidth())
        val dictionaryCard = card().apply {
            addView(
                LinearLayout(this@KeyboardSettingsActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL

                    addView(ImageView(this@KeyboardSettingsActivity).apply {
                        setImageResource(R.drawable.ic_goreecloud_keyboard)
                        contentDescription = null
                    }, LinearLayout.LayoutParams(dp(44), dp(44)).apply {
                        marginEnd = dp(14)
                    })

                    addView(
                        LinearLayout(this@KeyboardSettingsActivity).apply {
                            orientation = LinearLayout.VERTICAL
                            addView(TextView(this@KeyboardSettingsActivity).apply {
                                text = getString(R.string.keyboard_settings_dictionary_title)
                                textSize = 17f
                                setTextColor(palette.onSurfaceArgb)
                                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                            }, matchWidth())
                            addView(TextView(this@KeyboardSettingsActivity).apply {
                                text = getString(R.string.keyboard_settings_dictionary_badge)
                                textSize = 13f
                                setTextColor(accentColor)
                                setPadding(0, dp(3), 0, 0)
                            }, matchWidth())
                        },
                        LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f),
                    )
                },
                matchWidth(),
            )

            addView(TextView(this@KeyboardSettingsActivity).apply {
                text = getString(R.string.keyboard_settings_dictionary_summary)
                textSize = 14f
                setTextColor(palette.onSurfaceMutedArgb)
                setPadding(0, dp(14), 0, 0)
            }, matchWidth())

            addDivider()

            addView(
                settingRow(
                    title = getString(R.string.keyboard_settings_learn_from_typing),
                    summary = getString(R.string.keyboard_settings_learn_from_typing_summary),
                    checked = current.learnFromTypingEnabled,
                    onChecked = settingsStore::setLearnFromTypingEnabled,
                ),
                matchWidth(),
            )

            val learnedCount = TextView(this@KeyboardSettingsActivity).apply {
                textSize = 13.5f
                setTextColor(palette.onSurfaceMutedArgb)
                setPadding(0, dp(8), 0, dp(10))
            }
            fun refreshLearnedCount() {
                learnedCount.text = getString(
                    R.string.keyboard_settings_learned_count,
                    learningStore.learnedWordCount(),
                )
            }
            refreshLearnedCount()
            addView(learnedCount, matchWidth())

            addView(actionButton(
                label = getString(R.string.keyboard_settings_clear_learned),
                onClick = {
                    learningStore.clear()
                    refreshLearnedCount()
                },
            ), matchWidth())
        }
        root.addView(dictionaryCard, matchWidth().apply { bottomMargin = dp(22) })

        root.addView(sectionLabel(getString(R.string.keyboard_settings_clipboard_section)), matchWidth())
        val clipboardPreferences = KeyboardClipboardPreferences(this)
        val clipboardHistoryStore = EncryptedClipboardHistoryStore(this)
        val clipboardCard = card().apply {
            addView(
                settingRow(
                    title = getString(R.string.keyboard_settings_clipboard_history),
                    summary = getString(R.string.keyboard_settings_clipboard_history_summary),
                    checked = clipboardPreferences.historyEnabled(),
                    onChecked = clipboardPreferences::setHistoryEnabled,
                ),
                matchWidth(),
            )
            addDivider()

            addView(
                TextView(this@KeyboardSettingsActivity).apply {
                    text = getString(R.string.keyboard_settings_clipboard_retention)
                    textSize = 17f
                    setTextColor(palette.onSurfaceArgb)
                    typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                },
                matchWidth(),
            )
            addView(
                TextView(this@KeyboardSettingsActivity).apply {
                    text = getString(R.string.keyboard_settings_clipboard_retention_summary)
                    textSize = 14f
                    setTextColor(palette.onSurfaceMutedArgb)
                    setPadding(0, dp(5), 0, dp(14))
                },
                matchWidth(),
            )
            addView(
                buildClipboardRetentionSegment(
                    selected = clipboardPreferences.retention(),
                    onSelected = clipboardPreferences::setRetention,
                ),
                matchWidth(),
            )

            addDivider()
            addView(
                TextView(this@KeyboardSettingsActivity).apply {
                    text = getString(R.string.keyboard_settings_clipboard_privacy_note)
                    textSize = 13.5f
                    setTextColor(palette.onSurfaceMutedArgb)
                    setPadding(0, 0, 0, dp(10))
                },
                matchWidth(),
            )
            addView(
                actionButton(
                    label = getString(R.string.keyboard_settings_clear_clipboard_history),
                    onClick = clipboardHistoryStore::clearAll,
                ),
                matchWidth(),
            )
        }
        root.addView(clipboardCard, matchWidth().apply { bottomMargin = dp(22) })

        root.addView(sectionLabel(getString(R.string.keyboard_settings_more_section)), matchWidth())

        root.addView(actionButton(
            label = getString(R.string.keyboard_settings_run_setup),
            onClick = {
                setupStep = 0
                renderSetupWizard()
            },
        ), matchWidth().apply { bottomMargin = dp(10) })

        root.addView(actionButton(
            label = getString(R.string.keyboard_settings_portable_preferences),
            onClick = {
                startActivity(Intent(this, KeyboardPortablePreferencesActivity::class.java))
            },
        ), matchWidth().apply { bottomMargin = dp(10) })

        root.addView(actionButton(
            label = getString(R.string.keyboard_settings_android_settings),
            onClick = {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            },
        ), matchWidth())

        return ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = true
            setBackgroundColor(palette.canvasArgb)
            addView(
                root,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ),
            )
            ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
                val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
                insets
            }
            ViewCompat.requestApplyInsets(this)
        }
    }

    private fun buildHeader(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(4), 0, dp(26))

        addView(ImageView(this@KeyboardSettingsActivity).apply {
            setImageResource(R.drawable.ic_goreecloud_keyboard)
            contentDescription = null
        }, LinearLayout.LayoutParams(dp(56), dp(56)).apply {
            marginEnd = dp(16)
        })

        addView(
            LinearLayout(this@KeyboardSettingsActivity).apply {
                orientation = LinearLayout.VERTICAL
                addView(TextView(this@KeyboardSettingsActivity).apply {
                    text = getString(R.string.keyboard_settings_title)
                    textSize = 26f
                    setTextColor(palette.onSurfaceArgb)
                    typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                }, matchWidth())
                addView(TextView(this@KeyboardSettingsActivity).apply {
                    text = getString(R.string.keyboard_settings_summary)
                    textSize = 14f
                    setTextColor(palette.onSurfaceMutedArgb)
                    setPadding(0, dp(4), 0, 0)
                }, matchWidth())
            },
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f),
        )
    }

    private fun buildHeightSegment(selected: KeyboardKeyHeight): LinearLayout {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            background = roundedDrawable(palette.canvasArgb, palette.lineArgb, 16)
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }

        val buttons = linkedMapOf<KeyboardKeyHeight, TextView>()
        KeyboardKeyHeight.values().forEach { option ->
            val label = when (option) {
                KeyboardKeyHeight.COMPACT -> getString(R.string.keyboard_settings_key_height_compact)
                KeyboardKeyHeight.STANDARD -> getString(R.string.keyboard_settings_key_height_standard)
                KeyboardKeyHeight.TALL -> getString(R.string.keyboard_settings_key_height_tall)
            }
            val item = TextView(this).apply {
                text = label
                gravity = Gravity.CENTER
                textSize = 14f
                minHeight = dp(44)
                isClickable = true
                isFocusable = true
                contentDescription = "$label key height"
            }
            buttons[option] = item
            container.addView(
                item,
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    if (option != KeyboardKeyHeight.TALL) marginEnd = dp(4)
                },
            )
        }

        fun refresh(value: KeyboardKeyHeight) {
            buttons.forEach { (option, item) ->
                val isSelected = option == value
                item.setTextColor(if (isSelected) Color.WHITE else palette.onSurfaceArgb)
                item.typeface = Typeface.create(
                    "sans-serif-medium",
                    if (isSelected) Typeface.BOLD else Typeface.NORMAL,
                )
                item.background = if (isSelected) {
                    roundedDrawable(accentColor, null, 12)
                } else {
                    roundedDrawable(Color.TRANSPARENT, null, 12)
                }
                item.isSelected = isSelected
            }
        }

        buttons.forEach { (option, item) ->
            item.setOnClickListener {
                settingsStore.setKeyHeight(option)
                refresh(option)
            }
        }
        refresh(selected)
        return container
    }

    private fun buildToolbarStyleSegment(selected: KeyboardToolbarStyle): LinearLayout {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            background = roundedDrawable(palette.canvasArgb, palette.lineArgb, 16)
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }

        val buttons = linkedMapOf<KeyboardToolbarStyle, TextView>()
        KeyboardToolbarStyle.values().forEach { option ->
            val label = when (option) {
                KeyboardToolbarStyle.ICONS_ONLY ->
                    getString(R.string.keyboard_settings_toolbar_icons_only)
                KeyboardToolbarStyle.ICONS_WITH_LABELS ->
                    getString(R.string.keyboard_settings_toolbar_icons_labels)
            }
            val item = TextView(this).apply {
                text = label
                gravity = Gravity.CENTER
                textSize = 14f
                minHeight = dp(44)
                isClickable = true
                isFocusable = true
                contentDescription = label
            }
            buttons[option] = item
            container.addView(
                item,
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    if (option != KeyboardToolbarStyle.ICONS_WITH_LABELS) marginEnd = dp(4)
                },
            )
        }

        fun refresh(value: KeyboardToolbarStyle) {
            buttons.forEach { (option, item) ->
                val isSelected = option == value
                item.setTextColor(if (isSelected) Color.WHITE else palette.onSurfaceArgb)
                item.typeface = Typeface.create(
                    "sans-serif-medium",
                    if (isSelected) Typeface.BOLD else Typeface.NORMAL,
                )
                item.background = if (isSelected) {
                    roundedDrawable(accentColor, null, 12)
                } else {
                    roundedDrawable(Color.TRANSPARENT, null, 12)
                }
                item.isSelected = isSelected
            }
        }

        buttons.forEach { (option, item) ->
            item.setOnClickListener {
                settingsStore.setToolbarStyle(option)
                refresh(option)
            }
        }
        refresh(selected)
        return container
    }

    private fun buildClipboardRetentionSegment(
        selected: ClipboardRetention,
        onSelected: (ClipboardRetention) -> Unit,
    ): LinearLayout {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            background = roundedDrawable(palette.canvasArgb, palette.lineArgb, 16)
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }
        val buttons = linkedMapOf<ClipboardRetention, TextView>()
        ClipboardRetention.entries.forEach { option ->
            val label = when (option) {
                ClipboardRetention.TEN_MINUTES ->
                    getString(R.string.keyboard_settings_clipboard_retention_10m)
                ClipboardRetention.ONE_HOUR ->
                    getString(R.string.keyboard_settings_clipboard_retention_1h)
                ClipboardRetention.ONE_DAY ->
                    getString(R.string.keyboard_settings_clipboard_retention_24h)
            }
            val item = TextView(this).apply {
                text = label
                gravity = Gravity.CENTER
                textSize = 14f
                minHeight = dp(44)
                isClickable = true
                isFocusable = true
                contentDescription = label
            }
            buttons[option] = item
            container.addView(
                item,
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    if (option != ClipboardRetention.ONE_DAY) marginEnd = dp(4)
                },
            )
        }
        fun refresh(value: ClipboardRetention) {
            buttons.forEach { (option, item) ->
                val selectedNow = option == value
                item.setTextColor(if (selectedNow) Color.WHITE else palette.onSurfaceArgb)
                item.typeface = Typeface.create(
                    "sans-serif-medium",
                    if (selectedNow) Typeface.BOLD else Typeface.NORMAL,
                )
                item.background = if (selectedNow) {
                    roundedDrawable(accentColor, null, 12)
                } else {
                    roundedDrawable(Color.TRANSPARENT, null, 12)
                }
                item.isSelected = selectedNow
            }
        }
        buttons.forEach { (option, item) ->
            item.setOnClickListener {
                onSelected(option)
                refresh(option)
            }
        }
        refresh(selected)
        return container
    }

    private fun buildLongPressDelaySegment(selected: KeyboardLongPressDelay): LinearLayout {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            background = roundedDrawable(palette.canvasArgb, palette.lineArgb, 16)
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }
        val buttons = linkedMapOf<KeyboardLongPressDelay, TextView>()
        KeyboardLongPressDelay.values().forEach { option ->
            val label = when (option) {
                KeyboardLongPressDelay.FAST -> getString(R.string.keyboard_settings_long_press_delay_fast)
                KeyboardLongPressDelay.SYSTEM -> getString(R.string.keyboard_settings_long_press_delay_system)
                KeyboardLongPressDelay.RELAXED -> getString(R.string.keyboard_settings_long_press_delay_relaxed)
            }
            val item = TextView(this).apply {
                text = label
                gravity = Gravity.CENTER
                textSize = 14f
                minHeight = dp(44)
                isClickable = true
                isFocusable = true
                contentDescription = label
            }
            buttons[option] = item
            container.addView(item, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                if (option != KeyboardLongPressDelay.RELAXED) marginEnd = dp(4)
            })
        }
        fun refresh(value: KeyboardLongPressDelay) {
            buttons.forEach { (option, item) ->
                val selectedNow = option == value
                item.setTextColor(if (selectedNow) Color.WHITE else palette.onSurfaceArgb)
                item.typeface = Typeface.create("sans-serif-medium", if (selectedNow) Typeface.BOLD else Typeface.NORMAL)
                item.background = if (selectedNow) roundedDrawable(accentColor, null, 12) else roundedDrawable(Color.TRANSPARENT, null, 12)
                item.isSelected = selectedNow
            }
        }
        buttons.forEach { (option, item) ->
            item.setOnClickListener {
                settingsStore.setLongPressDelay(option)
                refresh(option)
            }
        }
        refresh(selected)
        return container
    }

    private fun settingRow(
        title: String,
        summary: String,
        checked: Boolean,
        onChecked: (Boolean) -> Unit,
    ): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(4), 0, dp(4))

        addView(
            LinearLayout(this@KeyboardSettingsActivity).apply {
                orientation = LinearLayout.VERTICAL
                addView(TextView(this@KeyboardSettingsActivity).apply {
                    text = title
                    textSize = 16.5f
                    setTextColor(palette.onSurfaceArgb)
                    typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                }, matchWidth())
                addView(TextView(this@KeyboardSettingsActivity).apply {
                    text = summary
                    textSize = 13.5f
                    setTextColor(palette.onSurfaceMutedArgb)
                    setPadding(0, dp(4), dp(10), 0)
                }, matchWidth())
            },
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f),
        )

        addView(Switch(this@KeyboardSettingsActivity).apply {
            isChecked = checked
            showText = false
            minWidth = dp(52)
            thumbTintList = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(accentColor, palette.onSurfaceMutedArgb),
            )
            trackTintList = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(withAlpha(accentColor, 70), withAlpha(palette.onSurfaceMutedArgb, 55)),
            )
            setOnCheckedChangeListener { _, value -> onChecked(value) }
        }, LinearLayout.LayoutParams(dp(56), dp(48)))
    }

    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(18), dp(16), dp(18), dp(16))
        background = roundedDrawable(palette.surfaceArgb, palette.lineArgb, 24)
    }

    private fun LinearLayout.addDivider() {
        addView(View(this@KeyboardSettingsActivity).apply {
            setBackgroundColor(palette.lineArgb)
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)).apply {
            topMargin = dp(10)
            bottomMargin = dp(10)
        })
    }

    private fun sectionLabel(value: String): TextView = TextView(this).apply {
        text = value
        textSize = 13f
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        setTextColor(palette.onSurfaceMutedArgb)
        setPadding(dp(4), 0, 0, dp(8))
    }

    private fun actionButton(label: String, onClick: () -> Unit): Button = Button(this).apply {
        text = label
        textSize = 15f
        isAllCaps = false
        gravity = Gravity.CENTER_VERTICAL
        setTextColor(palette.onSurfaceArgb)
        background = roundedDrawable(palette.surfaceArgb, palette.lineArgb, 18)
        minHeight = dp(56)
        setPadding(dp(18), 0, dp(18), 0)
        setOnClickListener { onClick() }
    }

    private fun roundedDrawable(fill: Int, stroke: Int?, radiusDp: Int): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(radiusDp).toFloat()
            setColor(fill)
            if (stroke != null) setStroke(dp(1), stroke)
        }

    private fun currentPalette(): GlazeKeyboardTokens.Palette =
        GlazeKeyboardTokens.palette(
            if (isDarkAppearance()) GlazeKeyboardTokens.Appearance.DARK
            else GlazeKeyboardTokens.Appearance.LIGHT,
        )

    private fun isDarkAppearance(): Boolean =
        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES

    private fun withAlpha(color: Int, alpha: Int): Int =
        Color.argb(alpha.coerceIn(0, 255), Color.red(color), Color.green(color), Color.blue(color))

    private fun matchWidth(): LinearLayout.LayoutParams = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
    )

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
