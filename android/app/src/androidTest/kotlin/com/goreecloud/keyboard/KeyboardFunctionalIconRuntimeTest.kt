package com.goreecloud.keyboard

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeyboardFunctionalIconRuntimeTest {
    @Test
    fun defaultToolbarPreservesTheFullGlazeInteractionFloor() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val view = KeyboardView(context).apply {
            measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY),
            )
            layout(0, 0, measuredWidth, measuredHeight)
            draw(
                Canvas(
                    Bitmap.createBitmap(
                        measuredWidth,
                        measuredHeight,
                        Bitmap.Config.ARGB_8888,
                    ),
                ),
            )
        }

        val minimum =
            GlazeKeyboardTokens.GeneralInteractionFloorDp * view.resources.displayMetrics.density
        val targets = view.accessibilityTargets()
        val emoji = targets.first { it.label == "Emoji" }
        val settings = targets.first { it.label == "Keyboard settings" }

        assertTrue(emoji.bounds.width() + 1f >= minimum)
        assertTrue(emoji.bounds.height() + 1f >= minimum)
        assertTrue(settings.bounds.width() + 1f >= minimum)
        assertTrue(settings.bounds.height() + 1f >= minimum)
    }

    @Test
    fun functionalIconMetricsKeepCompactGlyphsInsideLargerTouchTargets() {
        assertTrue(
            KeyboardFunctionalGlyphs.STROKE_DP in 1.5f..2.25f,
        )
        assertTrue(
            KeyboardFunctionalGlyphs.TOOLBAR_HIT_DP >=
                GlazeKeyboardTokens.GeneralInteractionFloorDp,
        )
        assertTrue(
            KeyboardFunctionalGlyphs.TOOLBAR_VISUAL_DP <
                KeyboardFunctionalGlyphs.TOOLBAR_HIT_DP,
        )
    }

    @Test
    fun coreFunctionalGlyphsRenderRealGeometry() {
        val density = ApplicationProvider
            .getApplicationContext<android.content.Context>()
            .resources
            .displayMetrics
            .density
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = KeyboardFunctionalGlyphs.STROKE_DP * density
        }
        val bounds = RectF(24f, 24f, 104f, 104f)

        val drawers = listOf<(Canvas) -> Unit>(
            { canvas -> KeyboardFunctionalGlyphs.drawShift(canvas, bounds, paint) },
            { canvas -> KeyboardFunctionalGlyphs.drawBackspace(canvas, bounds, paint) },
            { canvas -> KeyboardFunctionalGlyphs.drawEnter(canvas, bounds, paint) },
            { canvas -> KeyboardFunctionalGlyphs.drawEmoji(canvas, bounds, paint) },
            { canvas -> KeyboardFunctionalGlyphs.drawSettings(canvas, bounds, paint) },
        )

        drawers.forEach { draw ->
            val bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            var painted = 0
            for (y in 0 until bitmap.height) {
                for (x in 0 until bitmap.width) {
                    if (Color.alpha(bitmap.getPixel(x, y)) != 0) painted += 1
                }
            }
            bitmap.recycle()
            assertTrue("Every functional glyph must render visible geometry", painted > 20)
        }
    }
}
