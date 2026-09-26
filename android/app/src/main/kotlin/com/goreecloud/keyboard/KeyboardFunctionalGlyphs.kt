package com.goreecloud.keyboard

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * Shared first-party functional glyph geometry for GoreeCloud Keyboard.
 *
 * These are semantic controls rather than product identity artwork. Geometry follows the inherited
 * Glaze iconography contract: one restrained stroke family, round terminals, balanced negative
 * space, compact optical variants, monochrome default presentation, and state expressed by the
 * surrounding surface or accent paint rather than by changing the underlying symbol.
 */
internal object KeyboardFunctionalGlyphs {
    const val STROKE_DP = 1.85f
    const val TOOLBAR_HIT_DP = 48f
    const val TOOLBAR_VISUAL_DP = 40f

    fun drawShift(canvas: Canvas, bounds: RectF, paint: Paint) {
        val unit = minOf(bounds.width(), bounds.height())
        val cx = bounds.centerX()
        val top = bounds.centerY() - unit * 0.25f
        val shoulderY = bounds.centerY() - unit * 0.02f
        val stemTop = bounds.centerY() + unit * 0.02f
        val bottom = bounds.centerY() + unit * 0.24f
        val shoulder = unit * 0.22f
        val stem = unit * 0.09f

        val path = Path().apply {
            moveTo(cx, top)
            lineTo(cx + shoulder, shoulderY)
            lineTo(cx + stem, shoulderY)
            lineTo(cx + stem, bottom)
            lineTo(cx - stem, bottom)
            lineTo(cx - stem, shoulderY)
            lineTo(cx - shoulder, shoulderY)
            close()
        }
        canvas.drawPath(path, paint)
    }

    fun drawBackspace(canvas: Canvas, bounds: RectF, paint: Paint) {
        val unit = minOf(bounds.width(), bounds.height())
        val cx = bounds.centerX()
        val cy = bounds.centerY()
        val left = cx - unit * 0.15f
        val right = cx + unit * 0.24f
        val top = cy - unit * 0.16f
        val bottom = cy + unit * 0.16f
        val notch = cx - unit * 0.31f

        val shell = Path().apply {
            moveTo(notch, cy)
            lineTo(left, top)
            lineTo(right, top)
            lineTo(right, bottom)
            lineTo(left, bottom)
            close()
        }
        canvas.drawPath(shell, paint)

        val xInset = unit * 0.065f
        canvas.drawLine(cx - xInset, cy - xInset, cx + xInset, cy + xInset, paint)
        canvas.drawLine(cx + xInset, cy - xInset, cx - xInset, cy + xInset, paint)
    }

    fun drawEnter(canvas: Canvas, bounds: RectF, paint: Paint) {
        val unit = minOf(bounds.width(), bounds.height())
        val cx = bounds.centerX()
        val cy = bounds.centerY()
        val right = cx + unit * 0.22f
        val top = cy - unit * 0.19f
        val elbowY = cy + unit * 0.04f
        val left = cx - unit * 0.24f
        val arrow = unit * 0.12f

        val path = Path().apply {
            moveTo(right, top)
            lineTo(right, elbowY)
            lineTo(left, elbowY)
            moveTo(left, elbowY)
            lineTo(left + arrow, elbowY - arrow)
            moveTo(left, elbowY)
            lineTo(left + arrow, elbowY + arrow)
        }
        canvas.drawPath(path, paint)
    }

    fun drawEmoji(canvas: Canvas, bounds: RectF, strokePaint: Paint) {
        val unit = minOf(bounds.width(), bounds.height())
        val radius = unit * 0.205f
        val cx = bounds.centerX()
        val cy = bounds.centerY()

        canvas.drawCircle(cx, cy, radius, strokePaint)

        val fillPaint = Paint(strokePaint).apply { style = Paint.Style.FILL }
        val eyeY = cy - radius * 0.24f
        val eyeX = radius * 0.36f
        val eyeRadius = max(unit * 0.021f, 1f)
        canvas.drawCircle(cx - eyeX, eyeY, eyeRadius, fillPaint)
        canvas.drawCircle(cx + eyeX, eyeY, eyeRadius, fillPaint)

        val smile = Path().apply {
            moveTo(cx - radius * 0.47f, cy + radius * 0.11f)
            cubicTo(
                cx - radius * 0.26f,
                cy + radius * 0.50f,
                cx + radius * 0.26f,
                cy + radius * 0.50f,
                cx + radius * 0.47f,
                cy + radius * 0.11f,
            )
        }
        canvas.drawPath(smile, strokePaint)
    }

    fun drawSettings(canvas: Canvas, bounds: RectF, paint: Paint) {
        val unit = minOf(bounds.width(), bounds.height())
        val cx = bounds.centerX()
        val cy = bounds.centerY()
        val rootRadius = unit * 0.165f
        val toothRadius = unit * 0.235f
        val path = Path()

        repeat(16) { index ->
            val angle = -PI / 2.0 + index * (PI / 8.0)
            val radius = if (index % 2 == 0) toothRadius else rootRadius
            val x = cx + cos(angle).toFloat() * radius
            val y = cy + sin(angle).toFloat() * radius
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, paint)
        canvas.drawCircle(cx, cy, unit * 0.07f, paint)
    }

    fun centeredSquare(bounds: RectF, sizePx: Float): RectF {
        val side = minOf(sizePx, bounds.width(), bounds.height())
        return RectF(
            bounds.centerX() - side / 2f,
            bounds.centerY() - side / 2f,
            bounds.centerX() + side / 2f,
            bounds.centerY() + side / 2f,
        )
    }
}
