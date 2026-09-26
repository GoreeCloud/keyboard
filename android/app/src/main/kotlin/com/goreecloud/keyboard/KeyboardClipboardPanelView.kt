package com.goreecloud.keyboard

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.concurrent.TimeUnit

/**
 * First-party Glaze clipboard panel shown inside the IME window.
 *
 * Clipboard payloads arrive only through [KeyboardClipboardController]. This view has no direct
 * Android clipboard authority and never persists content.
 */
internal class KeyboardClipboardPanelView(
    context: Context,
    private val callbacks: Callbacks,
) : LinearLayout(context) {
    data class Callbacks(
        val onClose: () -> Unit,
        val onPaste: (id: String, once: Boolean) -> Unit,
        val onTogglePin: (id: String) -> Unit,
        val onDelete: (id: String) -> Unit,
        val onClearUnpinned: () -> Unit,
        val onHistoryEnabledChanged: (Boolean) -> Unit,
        val onPolicyChanged: (ClipboardAppPolicy) -> Unit,
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

        val packageLabel = snapshot.packageName ?: "No active app"
        addView(
            TextView(context).apply {
                text = "App policy • " + packageLabel
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
            return
        }

        val scroll = ScrollView(context).apply {
            isFillViewport = false
        }
        val list = LinearLayout(context).apply {
            orientation = VERTICAL
        }

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
            snapshot.entries.forEach { entry ->
                list.addView(
                    entryCard(entry),
                    matchWidth().apply { bottomMargin = dp(8) },
                )
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
                        LinearLayout.LayoutParams(0, dp(40), 1f).apply {
                            marginEnd = dp(5)
                        },
                    )
                    if (!entry.sensitive) {
                        addView(
                            chip(
                                if (entry.pinned) "Unpin" else "Pin",
                                { callbacks.onTogglePin(entry.id) },
                            ),
                            LinearLayout.LayoutParams(0, dp(40), 1f).apply {
                                marginEnd = dp(5)
                            },
                        )
                    }
                    addView(
                        chip("Delete", { callbacks.onDelete(entry.id) }),
                        LinearLayout.LayoutParams(0, dp(40), 1f),
                    )
                },
                matchWidth(),
            )
        }

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
