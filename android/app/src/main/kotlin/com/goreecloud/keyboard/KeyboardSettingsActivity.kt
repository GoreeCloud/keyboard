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
    private lateinit var palette: GlazeKeyboardTokens.Palette
    private var accentColor: Int = 0xFF2563EB.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsStore = KeyboardSettingsStore(this)
        learningStore = KeyboardLearningStore(this)
        palette = currentPalette()

        window.statusBarColor = palette.canvasArgb
        window.navigationBarColor = palette.canvasArgb
        window.decorView.systemUiVisibility =
            if (isDarkAppearance()) 0
            else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        setContentView(buildContent())
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
            addView(
                settingRow(
                    title = getString(R.string.keyboard_settings_swipe),
                    summary = getString(R.string.keyboard_settings_swipe_summary),
                    checked = current.swipeTypingEnabled,
                    onChecked = settingsStore::setSwipeTypingEnabled,
                ),
                matchWidth(),
            )
            addDivider()
            addView(
                settingRow(
                    title = getString(R.string.keyboard_settings_suggestions),
                    summary = getString(R.string.keyboard_settings_suggestions_summary),
                    checked = current.suggestionsEnabled,
                    onChecked = settingsStore::setSuggestionsEnabled,
                ),
                matchWidth(),
            )
            addDivider()
            addView(
                settingRow(
                    title = getString(R.string.keyboard_settings_autocorrect),
                    summary = getString(R.string.keyboard_settings_autocorrect_summary),
                    checked = current.autocorrectEnabled,
                    onChecked = settingsStore::setAutocorrectEnabled,
                ),
                matchWidth(),
            )
            addDivider()
            addView(
                settingRow(
                    title = getString(R.string.keyboard_settings_predictions),
                    summary = getString(R.string.keyboard_settings_predictions_summary),
                    checked = current.predictionsEnabled,
                    onChecked = settingsStore::setPredictionsEnabled,
                ),
                matchWidth(),
            )
            addDivider()
            addView(
                settingRow(
                    title = getString(R.string.keyboard_settings_auto_capitalize),
                    summary = getString(R.string.keyboard_settings_auto_capitalize_summary),
                    checked = current.autoCapitalizeEnabled,
                    onChecked = settingsStore::setAutoCapitalizeEnabled,
                ),
                matchWidth(),
            )
            addDivider()
            addView(
                settingRow(
                    title = getString(R.string.keyboard_settings_haptics),
                    summary = getString(R.string.keyboard_settings_haptics_summary),
                    checked = current.hapticFeedbackEnabled,
                    onChecked = settingsStore::setHapticFeedbackEnabled,
                ),
                matchWidth(),
            )
        }
        root.addView(typingCard, matchWidth().apply { bottomMargin = dp(22) })

        root.addView(sectionLabel(getString(R.string.keyboard_settings_appearance_section)), matchWidth())
        val appearanceCard = card().apply {
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
            addDivider()
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
        }
        root.addView(appearanceCard, matchWidth().apply { bottomMargin = dp(22) })

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

        root.addView(sectionLabel(getString(R.string.keyboard_settings_more_section)), matchWidth())

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
