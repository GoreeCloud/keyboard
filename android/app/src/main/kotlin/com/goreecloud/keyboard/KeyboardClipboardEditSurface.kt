package com.goreecloud.keyboard

import android.content.Context
import android.content.res.Configuration
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

/**
 * IME-local editor for saved GoreeCloud Clipboard history.
 *
 * The surface reuses the existing KeyboardView for input, so no nested system IME or exported
 * editor activity is required. The edit buffer remains in memory until Save.
 */
internal class KeyboardClipboardEditSurface(
    context: Context,
    keyboardView: KeyboardView,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) : LinearLayout(context) {
    private val palette = GlazeKeyboardTokens.palette(
        if (
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES
        ) GlazeKeyboardTokens.Appearance.DARK else GlazeKeyboardTokens.Appearance.LIGHT,
    )
    private val preview = TextView(context).apply {
        textSize = 16f
        setTextColor(palette.onSurfaceArgb)
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        setPadding(dp(14), dp(12), dp(14), dp(12))
        background = rounded(palette.surfaceArgb, palette.lineArgb, 16)
        maxLines = 5
    }

    init {
        orientation = VERTICAL
        setPadding(dp(10), dp(8), dp(10), dp(8))
        setBackgroundColor(palette.canvasArgb)

        addView(
            LinearLayout(context).apply {
                orientation = HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                addView(action("Cancel", onCancel), LayoutParams(0, dp(44), 1f).apply {
                    marginEnd = dp(8)
                })
                addView(TextView(context).apply {
                    text = "Edit clip"
                    textSize = 18f
                    gravity = Gravity.CENTER
                    setTextColor(palette.onSurfaceArgb)
                    typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
                }, LayoutParams(0, dp(44), 1f))
                addView(action("Save", onSave), LayoutParams(0, dp(44), 1f).apply {
                    marginStart = dp(8)
                })
            },
            matchWidth(),
        )

        addView(TextView(context).apply {
            text = "Saved locally in GoreeCloud Clipboard history"
            textSize = 12.5f
            setTextColor(palette.onSurfaceMutedArgb)
            setPadding(dp(4), dp(6), dp(4), dp(6))
        }, matchWidth())

        addView(preview, matchWidth().apply {
            bottomMargin = dp(8)
        })

        (keyboardView.parent as? ViewGroup)?.removeView(keyboardView)
        addView(
            keyboardView,
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f,
            ),
        )
    }

    fun updateText(value: String) {
        preview.text = if (value.isEmpty()) " " else value
        preview.contentDescription = "Clipboard edit text: " + value.take(120)
    }

    private fun action(label: String, onClick: () -> Unit): TextView =
        TextView(context).apply {
            text = label
            textSize = 13.5f
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            contentDescription = label
            setTextColor(palette.onSurfaceArgb)
            background = rounded(palette.surfaceArgb, palette.lineArgb, 14)
            setOnClickListener { onClick() }
        }

    private fun rounded(fill: Int, stroke: Int, radiusDp: Int): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(radiusDp).toFloat()
            setColor(fill)
            setStroke(dp(1), stroke)
        }

    private fun matchWidth(): LayoutParams =
        LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
