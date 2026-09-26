package com.goreecloud.keyboard

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ScrollView
import android.widget.TextView
import java.util.concurrent.TimeUnit

/**
 * First-party Glaze clipboard panel shown inside the IME window.
 *
 * Clipboard payloads arrive only through [KeyboardClipboardController]. Smart-content extraction
 * is computed locally while rendering and is never separately persisted or used for prediction.
 */
internal class KeyboardClipboardPanelView(
    context: Context,
    private val callbacks: Callbacks,
) : LinearLayout(context) {
    data class Callbacks(
        val onClose: () -> Unit,
        val onPaste: (id: String, once: Boolean) -> Unit,
        val onPasteText: (text: String) -> Unit,
        val onTogglePin: (id: String) -> Unit,
        val onDelete: (id: String) -> Unit,
        val onClearUnpinned: () -> Unit,
        val onHistoryEnabledChanged: (Boolean) -> Unit,
        val onPolicyChanged: (ClipboardAppPolicy) -> Unit,
        val onAllowOnce: () -> Unit,
    )

    private val palette = GlazeKeyboardTokens.palette(
        if (
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES
        ) GlazeKeyboardTokens.Appearance.DARK else GlazeKeyboardTokens.Appearance.LIGHT,
    )
    private val accent = resolveAccent()

    init {
        orientation = VERTICAL
        setPadding(dp(12), dp(10), dp(12), dp(10))
        setBackgroundColor(palette.canvasArgb)
        minimumHeight = dp(320)
    }

    fun render(snapshot: KeyboardClipboardSnapshot) {
        removeAllViews()

        addView(
            row().apply {
                addView(
                    chip("Keyboard", callbacks.onClose),
                    LinearLayout.LayoutParams(dp(92), dp(44)),
                )
                addView(
                    TextView(context).apply {
                        text = "Clipboard"
                        textSize = 20f
                        setTextColor(palette.onSurfaceArgb)
                        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
                        gravity = Gravity.CENTER_VERTICAL
                        setPadding(dp(14), 0, 0, 0)
                    },
                    LinearLayout.LayoutParams(0, dp(44), 1f),
                )
                addView(
                    chip("Clear", callbacks.onClearUnpinned),
                    LinearLayout.LayoutParams(dp(76), dp(44)),
                )
            },
            matchWidth(),
        )

        addView(
            TextView(context).apply {
                text = "App policy • " + (snapshot.packageName ?: "No active app")
                textSize = 12.5f
                setTextColor(palette.onSurfaceMutedArgb)
                setPadding(dp(4), dp(8), dp(4), dp(5))
            },
            matchWidth(),
        )

        addView(
            row().apply {
                ClipboardAppPolicy.entries.forEach { policy ->
                    val label = when (policy) {
                        ClipboardAppPolicy.ALLOW -> "Allow"
                        ClipboardAppPolicy.ASK -> "Ask"
                        ClipboardAppPolicy.PASTE_ONLY -> "Paste only"
                        ClipboardAppPolicy.BLOCK -> "Block"
                    }
                    addView(
                        chip(
                            label = label,
                            onClick = { callbacks.onPolicyChanged(policy) },
                            selected = snapshot.policy == policy,
                        ),
                        LinearLayout.LayoutParams(0, dp(42), 1f).apply {
                            if (policy != ClipboardAppPolicy.BLOCK) marginEnd = dp(6)
                        },
                    )
                }
            },
            matchWidth(),
        )

        addView(
            row().apply {
                addView(
                    TextView(context).apply {
                        text = if (snapshot.historyEnabled) {
                            "Encrypted local history • " + retentionLabel(snapshot.retention)
                        } else {
                            "Clipboard history is off"
                        }
                        textSize = 13f
                        setTextColor(palette.onSurfaceMutedArgb)
                        gravity = Gravity.CENTER_VERTICAL
                    },
                    LinearLayout.LayoutParams(0, dp(46), 1f),
                )
                addView(
                    chip(
                        if (snapshot.historyEnabled) "Disable" else "Enable",
                        { callbacks.onHistoryEnabledChanged(!snapshot.historyEnabled) },
                    ),
                    LinearLayout.LayoutParams(dp(86), dp(40)),
                )
            },
            matchWidth(),
        )

        snapshot.blockedReason?.let { reason ->
            addView(messageCard(reason), matchWidth().apply { topMargin = dp(8) })
            if (snapshot.requiresAuthorization) {
                addView(
                    chip("Allow once", callbacks.onAllowOnce),
                    matchWidth().apply {
                        topMargin = dp(8)
                        height = dp(44)
                    },
                )
            }
            return
        }

        val scroll = ScrollView(context)
        val list = LinearLayout(context).apply { orientation = VERTICAL }

        if (snapshot.entries.isEmpty()) {
            list.addView(
                messageCard(
                    if (snapshot.historyEnabled) {
                        "No text clips yet. Copy text while the keyboard is active, or open this panel after copying."
                    } else {
                        "Copy text, then open Clipboard. History remains off until you explicitly enable it."
                    },
                ),
                matchWidth(),
            )
        } else {
            val pinned = snapshot.entries.filter { it.pinned }
            val recent = snapshot.entries.filterNot { it.pinned }

            if (pinned.isNotEmpty()) {
                list.addView(sectionTitle("Pinned"), matchWidth())
                pinned.forEach { entry ->
                    list.addView(entryCard(entry), entryLayoutParams())
                }
            }

            if (recent.isNotEmpty()) {
                list.addView(sectionTitle("Recent"), matchWidth())
                recent.forEach { entry ->
                    list.addView(entryCard(entry), entryLayoutParams())
                }
            }
        }

        scroll.addView(
            list,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ),
        )
        addView(
            scroll,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f),
        )
    }

    private fun entryCard(entry: KeyboardClipboardEntry): LinearLayout =
        LinearLayout(context).apply {
            orientation = VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = rounded(palette.surfaceArgb, palette.lineArgb, 18)
            isClickable = true
            isFocusable = true
            contentDescription =
                if (entry.sensitive) "Sensitive clipboard item"
                else "Clipboard item: " + entry.text.take(80)

            setOnClickListener {
                callbacks.onPaste(entry.id, entry.sensitive)
            }
            setOnLongClickListener {
                showItemMenu(entry, this)
                true
            }

            addView(
                TextView(context).apply {
                    text = if (entry.sensitive) {
                        "Sensitive clipboard item"
                    } else {
                        entry.text.replace('\n', ' ').replace('\r', ' ').take(220)
                    }
                    textSize = 15f
                    setTextColor(palette.onSurfaceArgb)
                    maxLines = 3
                },
                matchWidth(),
            )

            addView(
                TextView(context).apply {
                    text = entryMetadata(entry)
                    textSize = 12f
                    setTextColor(palette.onSurfaceMutedArgb)
                    setPadding(0, dp(5), 0, dp(8))
                },
                matchWidth(),
            )

            if (!entry.sensitive) {
                val detected = ClipboardSmartContentDetector.detect(entry.text, limit = 6)
                if (detected.isNotEmpty()) {
                    addView(TextView(context).apply {
                        text = "Detected"
                        textSize = 12f
                        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
                        setTextColor(palette.onSurfaceMutedArgb)
                        setPadding(0, dp(2), 0, dp(6))
                    }, matchWidth())

                    detected.forEach { item ->
                        addView(
                            smartItem(item),
                            matchWidth().apply { bottomMargin = dp(5) },
                        )
                    }
                }
            }

            addView(
                row().apply {
                    if (!entry.sensitive) {
                        addView(
                            chip("Paste", { callbacks.onPaste(entry.id, false) }),
                            LinearLayout.LayoutParams(0, dp(40), 1f).apply {
                                marginEnd = dp(5)
                            },
                        )
                    }
                    addView(
                        chip("Paste once", { callbacks.onPaste(entry.id, true) }),
                        LinearLayout.LayoutParams(0, dp(40), 1f),
                    )
                },
                matchWidth(),
            )

            if (!entry.sensitive) {
                addView(
                    row().apply {
                        addView(
                            chip(
                                if (entry.pinned) "Unpin" else "Pin",
                                { callbacks.onTogglePin(entry.id) },
                            ),
                            LinearLayout.LayoutParams(0, dp(40), 1f).apply {
                                marginEnd = dp(5)
                            },
                        )
                        addView(
                            chip("Delete", { callbacks.onDelete(entry.id) }),
                            LinearLayout.LayoutParams(0, dp(40), 1f),
                        )
                    },
                    matchWidth().apply { topMargin = dp(6) },
                )
            }
        }

    private fun smartItem(item: ClipboardSmartContent): TextView =
        TextView(context).apply {
            text = item.type.label + " • " + item.value
            textSize = 12.5f
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true
            contentDescription = "Paste detected " + item.type.label + ": " + item.value
            setTextColor(palette.onSurfaceArgb)
            setPadding(dp(10), dp(7), dp(10), dp(7))
            background = rounded(palette.canvasArgb, palette.lineArgb, 12)
            setOnClickListener { callbacks.onPasteText(item.value) }
        }

    private fun showItemMenu(entry: KeyboardClipboardEntry, anchor: View) {
        val popup = PopupMenu(context, anchor)
        if (!entry.sensitive) {
            popup.menu.add(if (entry.pinned) "Unpin" else "Pin").setOnMenuItemClickListener {
                callbacks.onTogglePin(entry.id)
                true
            }
        }
        popup.menu.add("Delete").setOnMenuItemClickListener {
            callbacks.onDelete(entry.id)
            true
        }
        popup.show()
    }

    private fun sectionTitle(value: String): TextView =
        TextView(context).apply {
            text = value
            textSize = 13f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(palette.onSurfaceMutedArgb)
            setPadding(dp(4), dp(10), 0, dp(7))
        }

    private fun entryLayoutParams(): LinearLayout.LayoutParams =
        matchWidth().apply { bottomMargin = dp(8) }

    private fun entryMetadata(entry: KeyboardClipboardEntry): String {
        val parts = mutableListOf<String>()
        if (entry.current) parts += "Current"
        if (entry.sensitive) parts += "Sensitive • not stored"
        if (entry.pinned) {
            parts += "Pinned"
        } else if (entry.expiresAtMillis != null) {
            val remaining =
                (entry.expiresAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(remaining).coerceAtLeast(1L)
            parts += "Expires in " + minutes + "m"
        }
        return parts.joinToString(" • ").ifBlank { "Local history" }
    }

    private fun retentionLabel(value: ClipboardRetention): String = when (value) {
        ClipboardRetention.TEN_MINUTES -> "10 min"
        ClipboardRetention.ONE_HOUR -> "1 hour"
        ClipboardRetention.ONE_DAY -> "24 hours"
    }

    private fun messageCard(value: String): TextView =
        TextView(context).apply {
            text = value
            textSize = 14f
            setTextColor(palette.onSurfaceMutedArgb)
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = rounded(palette.surfaceArgb, palette.lineArgb, 18)
        }

    private fun chip(
        label: String,
        onClick: () -> Unit,
        selected: Boolean = false,
    ): TextView =
        TextView(context).apply {
            text = label
            textSize = 13f
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            contentDescription = label
            setTextColor(if (selected) Color.WHITE else palette.onSurfaceArgb)
            typeface = Typeface.create(
                "sans-serif-medium",
                if (selected) Typeface.BOLD else Typeface.NORMAL,
            )
            background = rounded(
                if (selected) accent else palette.surfaceArgb,
                palette.lineArgb,
                14,
            )
            setOnClickListener { onClick() }
        }

    private fun row(): LinearLayout = LinearLayout(context).apply {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    private fun rounded(fill: Int, stroke: Int, radiusDp: Int): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(radiusDp).toFloat()
            setColor(fill)
            setStroke(dp(1), stroke)
        }

    private fun resolveAccent(): Int {
        val value = android.util.TypedValue()
        return if (context.theme.resolveAttribute(android.R.attr.colorAccent, value, true)) {
            if (value.resourceId != 0) context.getColor(value.resourceId) else value.data
        } else {
            palette.onSurfaceArgb
        }
    }

    private fun matchWidth(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
