package com.goreecloud.keyboard

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.hypot
import kotlin.math.max

class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    interface Listener {
        fun onText(value: String)
        fun onSwipe(keyPath: List<String>)
        fun onSpace()
        fun onBackspace()
        fun onEnter()
        fun onShift()
        fun onSuggestion(value: String)
        fun onLayerChanged(layer: KeyboardLayer)
        fun onOpenSettings() = Unit
    }

    var listener: Listener? = null

    private data class Key(val label: String, val weight: Float = 1f, val action: Action)
    private enum class Action {
        TEXT,
        SHIFT,
        BACKSPACE,
        SPACE,
        ENTER,
        LETTERS,
        SYMBOLS,
        SYMBOLS_MORE,
        EMOJI,
        SETTINGS,
        EMOJI_SEARCH_CLEAR,
        EMOJI_SEARCH_CLOSE,
    }
    private data class HitKey(val bounds: RectF, val key: Key)
    private data class HitSuggestion(val bounds: RectF, val value: String)
    private data class HitEmojiCategory(val bounds: RectF, val entry: EmojiStripEntry)
    private data class HitEmojiSearchResult(val bounds: RectF, val result: EmojiSearchResult)
    private data class AlternatePopup(
        val sourceBounds: RectF,
        val values: List<String>,
        var selectedIndex: Int? = 0,
        var layout: AlternatePopupLayoutResult? = null,
    )

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pressedKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val keyStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 20f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("sans", Typeface.NORMAL)
    }
    private val utilityTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 15f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }
    private val spaceLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 12f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }
    private val suggestionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 14f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }
    private val suggestionHintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 13f * resources.displayMetrics.scaledDensity
    }
    private val alternatePopupPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val alternateSelectedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val suggestionSurfacePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val utilityKeyOverlayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectedUtilityKeyOverlayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val suggestionDividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density
    }
    private val swipeTrailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 4f * resources.displayMetrics.density
    }

    private val hitKeys = mutableListOf<HitKey>()
    private val hitSuggestions = mutableListOf<HitSuggestion>()
    private val hitEmojiCategories = mutableListOf<HitEmojiCategory>()
    private val hitEmojiSearchResults = mutableListOf<HitEmojiSearchResult>()
    private val emojiRecentsStore = LocalEmojiRecentsStore(context)
    private val emojiCategoryStore = LocalEmojiCategoryStore(context)
    private val emojiRecents = EmojiRecents(initialValues = emojiRecentsStore.load())
    private val emojiSearchSession = EmojiSearchSession()
    private val accessibilityDelegate = KeyboardAccessibilityDelegate(this)
    private var shifted = false
    private var suggestions: List<String> = emptyList()
    private var layer = KeyboardLayer.LETTERS
    private var emojiCategory = emojiCategoryStore.load()
    private var showingEmojiRecents = false
    private var pressedKeyBounds: RectF? = null
    private var pendingAlternateHit: HitKey? = null
    private var alternatePopup: AlternatePopup? = null
    private var glazeV16PresentationContext = GlazeKeyboardV16PresentationContext()
    private var bottomNavigationInsetPx = 0
    private var keyHeightPreference = KeyboardKeyHeight.COMPACT
    private var swipeTypingEnabled = false
    private var swipeGestureActive = false
    private val swipeKeyPath = mutableListOf<String>()
    private val swipePath = Path()
    private var swipeDownX = 0f
    private var swipeDownY = 0f
    private var swipeDownTimeMs = 0L

    init {
        isClickable = true
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        ViewCompat.setAccessibilityDelegate(this, accessibilityDelegate)
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            val bottomInset = maxOf(
                insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom,
                insets.getInsets(WindowInsetsCompat.Type.systemGestures()).bottom,
                insets.getInsets(WindowInsetsCompat.Type.mandatorySystemGestures()).bottom,
            )
            if (bottomNavigationInsetPx != bottomInset) {
                bottomNavigationInsetPx = bottomInset
                requestLayout()
                invalidateStructure()
            }
            insets
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ViewCompat.requestApplyInsets(this)
    }

    private val showAlternatesRunnable = Runnable {
        val hit = pendingAlternateHit ?: return@Runnable
        val values = alternatesFor(hit)
        if (values.isEmpty()) return@Runnable
        pressedKeyBounds = null
        alternatePopup = AlternatePopup(RectF(hit.bounds), values)
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        announceForAccessibility("Alternate characters available")
        invalidate()
    }

    fun setShifted(value: Boolean) {
        cancelAlternateInteraction()
        shifted = value && layer == KeyboardLayer.LETTERS
        invalidateStructure()
    }

    fun setLayer(value: KeyboardLayer) {
        cancelAlternateInteraction()
        cancelSwipeInteraction()
        layer = value
        if (layer != KeyboardLayer.LETTERS) shifted = false
        if (layer != KeyboardLayer.EMOJI) emojiSearchSession.close()
        invalidateStructure()
    }

    fun setSuggestions(values: List<String>) {
        suggestions = values.take(3)
        invalidateStructure()
    }

    fun setSwipeTypingEnabled(enabled: Boolean) {
        if (swipeTypingEnabled == enabled) return
        swipeTypingEnabled = enabled
        cancelSwipeInteraction()
        invalidate()
    }

    internal fun setKeyHeightPreference(value: KeyboardKeyHeight) {
        if (keyHeightPreference == value) return
        keyHeightPreference = value
        invalidateStructure()
    }

    internal fun setGlazeV16PresentationSignals(signals: GlazeKeyboardV16PresentationSignals) {
        val resolved = GlazeKeyboardV16AndroidPresentationContext.resolve(signals)
        if (resolved == glazeV16PresentationContext) return
        glazeV16PresentationContext = resolved
        invalidateStructure()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val density = resources.displayMetrics.density
        val preferredHeightDp = GlazeKeyboardV16PresentationPolicy
            .preferredImeHeightDp(glazeV16PresentationContext)
        val preferredHeight = (preferredHeightDp * density).toInt() + bottomNavigationInsetPx
        val height = resolveSize(preferredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        applyCurrentAppearance()
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        hitKeys.clear()
        hitSuggestions.clear()
        hitEmojiCategories.clear()
        hitEmojiSearchResults.clear()

        val rows = currentRows()
        val density = resources.displayMetrics.density
        val horizontalPadding = GlazeKeyboardTokens.Space2Dp * density
        val gap = GlazeKeyboardTokens.Space1Dp * density
        val topArea = GlazeKeyboardV16PresentationPolicy
            .interactionFloorDp(glazeV16PresentationContext) * density
        val keyboardTop = topArea + GlazeKeyboardTokens.Space2Dp * density
        val bottomSafeGap = GlazeKeyboardTokens.BottomSafeGapDp * density
        val contentBottom = max(
            keyboardTop + rows.size,
            height - bottomNavigationInsetPx.toFloat() - bottomSafeGap,
        )
        val rowGapCount = (rows.size - 1).coerceAtLeast(0)
        val rowHeight = max(1f, (contentBottom - keyboardTop - gap * rowGapCount) / rows.size)
        val keyRadius = GlazeKeyboardTokens.RadiusMediumDp * density

        drawSuggestionStrip(canvas, horizontalPadding, topArea)

        rows.forEachIndexed { rowIndex, row ->
            val totalWeight = row.sumOf { it.weight.toDouble() }.toFloat()
            val rowHorizontalPadding = horizontalPadding + centeredLetterRowInset(rowIndex, row, gap, horizontalPadding)
            val availableWidth = width - rowHorizontalPadding * 2 - gap * (row.size - 1)
            var left = rowHorizontalPadding
            val top = keyboardTop + rowIndex * (rowHeight + gap)

            row.forEach { key ->
                val keyWidth = availableWidth * (key.weight / totalWeight)
                val bounds = RectF(left, top, left + keyWidth, top + rowHeight)
                val visualInset = minOf(
                    keyCapVerticalInsetDp() * density,
                    (rowHeight - density).coerceAtLeast(0f) / 2f,
                )
                val visualBounds = RectF(bounds).apply { inset(0f, visualInset) }
                canvas.drawRoundRect(visualBounds, keyRadius, keyRadius, keyPaint)
                if (isUtilityKey(key)) {
                    canvas.drawRoundRect(
                        visualBounds,
                        keyRadius,
                        keyRadius,
                        if (isSelectedUtilityKey(key)) selectedUtilityKeyOverlayPaint else utilityKeyOverlayPaint,
                    )
                }
                if (isPressedKey(bounds)) {
                    canvas.drawRoundRect(visualBounds, keyRadius, keyRadius, pressedKeyPaint)
                }
                canvas.drawRoundRect(visualBounds, keyRadius, keyRadius, keyStrokePaint)

                val label = renderedKeyLabel(key)
                val labelPaint = keyLabelPaint(key)
                val baseline = visualBounds.centerY() - (labelPaint.descent() + labelPaint.ascent()) / 2
                canvas.drawText(label, visualBounds.centerX(), baseline, labelPaint)
                hitKeys += HitKey(bounds, key)
                left += keyWidth + gap
            }
        }

        if (swipeGestureActive) {
            canvas.drawPath(swipePath, swipeTrailPaint)
        }
        alternatePopup?.let { drawAlternatePopup(canvas, it) }
    }

    private fun keyCapVerticalInsetDp(): Float = when (keyHeightPreference) {
        KeyboardKeyHeight.COMPACT -> 5f
        KeyboardKeyHeight.STANDARD -> 2.5f
        KeyboardKeyHeight.TALL -> 0f
    }

    private fun centeredLetterRowInset(
        rowIndex: Int,
        row: List<Key>,
        gap: Float,
        horizontalPadding: Float,
    ): Float {
        if (layer != KeyboardLayer.LETTERS || rowIndex != 2 || row.any { it.action != Action.TEXT }) {
            return 0f
        }
        val topRowKeyWidth = (width - horizontalPadding * 2 - gap * 9) / 10f
        val desiredWidth = topRowKeyWidth * row.size + gap * (row.size - 1)
        return max(0f, (width - desiredWidth) / 2f - horizontalPadding)
    }

    private fun currentRows(): List<List<Key>> {
        if (layer == KeyboardLayer.EMOJI && emojiSearchSession.snapshot().active) {
            val letterRows = KeyboardLayout.characterRows(KeyboardLayer.LETTERS)
            return listOf(
                letterRows[0].map(::textKey),
                letterRows[1].map(::textKey),
                letterRows[2].map(::textKey) + listOf(Key("⌫", 1.25f, Action.BACKSPACE)),
                listOf(
                    Key("Clear", 1.35f, Action.EMOJI_SEARCH_CLEAR),
                    Key("space", 3.9f, Action.SPACE),
                    Key("Close", 1.35f, Action.EMOJI_SEARCH_CLOSE),
                ),
            )
        }

        val characterRows = if (layer == KeyboardLayer.EMOJI) {
            if (showingEmojiRecents && emojiRecents.values().isNotEmpty()) emojiRecents.rows() else KeyboardLayout.emojiRows(emojiCategory)
        } else {
            KeyboardLayout.characterRows(layer)
        }
        return when (layer) {
            KeyboardLayer.LETTERS -> listOf(
                DIGIT_ROW.map(::textKey),
                characterRows[0].map(::textKey),
                characterRows[1].map(::textKey),
                listOf(Key("⇧", 1.25f, Action.SHIFT)) + characterRows[2].map(::textKey) + listOf(Key("⌫", 1.25f, Action.BACKSPACE)),
                listOf(
                    Key("?123", 1.2f, Action.SYMBOLS),
                    Key("☺", 1.0f, Action.EMOJI),
                    textKey(",").copy(weight = 0.9f),
                    Key("space", 3.6f, Action.SPACE),
                    textKey(".").copy(weight = 0.9f),
                    Key("↵", 1.2f, Action.ENTER),
                ),
            )
            KeyboardLayer.SYMBOLS -> listOf(
                characterRows[0].map(::textKey),
                characterRows[1].map(::textKey),
                characterRows[2].map(::textKey) + listOf(Key("⌫", 1.25f, Action.BACKSPACE)),
                listOf(Key("ABC", 1.15f, Action.LETTERS), Key("=\\<", 1.15f, Action.SYMBOLS_MORE), Key("☺", 1.05f, Action.EMOJI), Key("space", 3.9f, Action.SPACE), Key("↵", 1.25f, Action.ENTER)),
            )
            KeyboardLayer.SYMBOLS_MORE -> listOf(
                characterRows[0].map(::textKey),
                characterRows[1].map(::textKey),
                characterRows[2].map(::textKey) + listOf(Key("⌫", 1.25f, Action.BACKSPACE)),
                listOf(Key("ABC", 1.15f, Action.LETTERS), Key("?123", 1.15f, Action.SYMBOLS), Key("☺", 1.05f, Action.EMOJI), Key("space", 3.9f, Action.SPACE), Key("↵", 1.25f, Action.ENTER)),
            )
            KeyboardLayer.EMOJI -> listOf(
                characterRows[0].map(::textKey),
                characterRows[1].map(::textKey),
                characterRows[2].map(::textKey) + listOf(Key("⌫", 1.25f, Action.BACKSPACE)),
                listOf(Key("ABC", 1.1f, Action.LETTERS), Key("?123", 1.1f, Action.SYMBOLS), Key("space", 4.2f, Action.SPACE), Key("↵", 1.25f, Action.ENTER)),
            )
        }
    }

    private fun textKey(value: String): Key = Key(value, action = Action.TEXT)

    private fun renderedKeyLabel(key: Key): String = when {
        key.action == Action.SPACE && layer == KeyboardLayer.LETTERS -> "English (US)"
        key.action == Action.TEXT && shifted && layer == KeyboardLayer.LETTERS -> key.label.uppercase()
        else -> key.label
    }

    private fun isUtilityKey(key: Key): Boolean =
        key.action !in setOf(Action.TEXT, Action.SPACE)

    private fun isSelectedUtilityKey(key: Key): Boolean =
        key.action == Action.SHIFT && shifted

    private fun keyLabelPaint(key: Key): Paint = when {
        key.action == Action.SPACE && layer == KeyboardLayer.LETTERS -> spaceLabelPaint
        key.action in setOf(
            Action.LETTERS,
            Action.SYMBOLS,
            Action.SYMBOLS_MORE,
            Action.EMOJI_SEARCH_CLEAR,
            Action.EMOJI_SEARCH_CLOSE,
        ) -> utilityTextPaint
        else -> textPaint
    }

    private fun alternatesFor(hit: HitKey): List<String> {
        if (hit.key.action != Action.TEXT || layer == KeyboardLayer.EMOJI || emojiSearchSession.snapshot().active) {
            return emptyList()
        }
        return KeyAlternates.forKey(renderedKeyLabel(hit.key))
    }

    private fun isPressedKey(bounds: RectF): Boolean {
        val pressed = pressedKeyBounds ?: return false
        return pressed.left == bounds.left &&
            pressed.top == bounds.top &&
            pressed.right == bounds.right &&
            pressed.bottom == bounds.bottom
    }

    private fun applyCurrentAppearance() {
        val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val appearance = if (nightMode == Configuration.UI_MODE_NIGHT_YES) GlazeKeyboardTokens.Appearance.DARK else GlazeKeyboardTokens.Appearance.LIGHT
        val palette = GlazeKeyboardTokens.palette(appearance)
        backgroundPaint.color = palette.canvasArgb
        keyPaint.color = palette.surfaceArgb
        pressedKeyPaint.color = GlazeKeyboardTokens.stateOverlayArgb(
            appearance,
            GlazeKeyboardTokens.PressedOverlayOpacity,
        )
        keyStrokePaint.color = palette.lineArgb
        textPaint.color = palette.onSurfaceArgb
        utilityTextPaint.color = palette.onSurfaceArgb
        spaceLabelPaint.color = palette.onSurfaceMutedArgb
        suggestionPaint.color = palette.onSurfaceArgb
        suggestionHintPaint.color = palette.onSurfaceMutedArgb
        alternatePopupPaint.color = palette.canvasArgb
        alternateSelectedPaint.color = palette.surfaceArgb
        suggestionSurfacePaint.color = palette.surfaceArgb
        utilityKeyOverlayPaint.color = GlazeKeyboardTokens.stateOverlayArgb(
            appearance,
            GlazeKeyboardTokens.UtilityOverlayOpacity,
        )
        selectedUtilityKeyOverlayPaint.color = GlazeKeyboardTokens.stateOverlayArgb(
            appearance,
            GlazeKeyboardTokens.SelectedOverlayOpacity,
        )
        suggestionDividerPaint.color = palette.lineArgb
        swipeTrailPaint.color = palette.onSurfaceArgb
        swipeTrailPaint.alpha = 86
    }

    private fun drawSuggestionStrip(canvas: Canvas, horizontalPadding: Float, topArea: Float) {
        if (layer == KeyboardLayer.EMOJI) {
            if (emojiSearchSession.snapshot().active) {
                drawEmojiSearchStrip(canvas, horizontalPadding, topArea)
            } else {
                drawEmojiCategoryStrip(canvas, horizontalPadding, topArea)
            }
            return
        }

        val density = resources.displayMetrics.density
        val gap = GlazeKeyboardTokens.Space1Dp * density
        val verticalInset = GlazeKeyboardTokens.Space1Dp * density
        val radius = GlazeKeyboardTokens.RadiusMediumDp * density
        val settingsHitBounds = RectF(
            width - horizontalPadding - topArea,
            0f,
            width - horizontalPadding,
            topArea,
        )
        val settingsVisualBounds = RectF(settingsHitBounds).apply { inset(0f, verticalInset) }
        canvas.drawRoundRect(settingsVisualBounds, radius, radius, keyPaint)
        canvas.drawRoundRect(settingsVisualBounds, radius, radius, utilityKeyOverlayPaint)
        canvas.drawRoundRect(settingsVisualBounds, radius, radius, keyStrokePaint)
        val settingsBaseline =
            settingsVisualBounds.centerY() - (utilityTextPaint.descent() + utilityTextPaint.ascent()) / 2
        canvas.drawText("⚙", settingsVisualBounds.centerX(), settingsBaseline, utilityTextPaint)
        hitKeys += HitKey(settingsHitBounds, Key("⚙", action = Action.SETTINGS))

        val contentRight = settingsHitBounds.left - gap
        if (layer != KeyboardLayer.LETTERS) {
            val baseline =
                topArea / 2f - (suggestionHintPaint.descent() + suggestionHintPaint.ascent()) / 2
            canvas.drawText(
                "Symbols · local",
                (horizontalPadding + contentRight) / 2f,
                baseline,
                suggestionHintPaint,
            )
            return
        }

        if (suggestions.isEmpty()) {
            val baseline =
                topArea / 2f - (suggestionHintPaint.descent() + suggestionHintPaint.ascent()) / 2
            canvas.drawText(
                "Quill · on-device",
                (horizontalPadding + contentRight) / 2f,
                baseline,
                suggestionHintPaint,
            )
            return
        }

        val stripBounds = RectF(
            horizontalPadding,
            verticalInset,
            contentRight,
            topArea - verticalInset,
        )
        canvas.drawRoundRect(stripBounds, radius, radius, suggestionSurfacePaint)

        val cellWidth = stripBounds.width() / suggestions.size
        suggestions.forEachIndexed { index, suggestion ->
            val left = stripBounds.left + cellWidth * index
            val bounds = RectF(left, stripBounds.top, left + cellWidth, stripBounds.bottom)
            if (index > 0) {
                canvas.drawLine(
                    bounds.left,
                    bounds.top + GlazeKeyboardTokens.Space2Dp * density,
                    bounds.left,
                    bounds.bottom - GlazeKeyboardTokens.Space2Dp * density,
                    suggestionDividerPaint,
                )
            }
            val baseline = bounds.centerY() - (suggestionPaint.descent() + suggestionPaint.ascent()) / 2
            canvas.drawText(suggestion, bounds.centerX(), baseline, suggestionPaint)
            hitSuggestions += HitSuggestion(
                RectF(bounds.left, 0f, bounds.right, topArea),
                suggestion,
            )
        }
    }

    private fun drawEmojiSearchStrip(canvas: Canvas, horizontalPadding: Float, topArea: Float) {
        val snapshot = emojiSearchSession.snapshot()
        val gap = GlazeKeyboardTokens.Space1Dp * resources.displayMetrics.density
        val queryWidth = (width - horizontalPadding * 2) * 0.46f
        val queryBounds = RectF(horizontalPadding, 0f, horizontalPadding + queryWidth, topArea)
        val radius = GlazeKeyboardTokens.RadiusMediumDp * resources.displayMetrics.density
        canvas.drawRoundRect(queryBounds, radius, radius, keyPaint)
        canvas.drawRoundRect(queryBounds, radius, radius, keyStrokePaint)
        val queryLabel = if (snapshot.query.isBlank()) "Search emoji locally" else "⌕ ${snapshot.query}"
        val queryBaseline = queryBounds.centerY() - (suggestionHintPaint.descent() + suggestionHintPaint.ascent()) / 2
        canvas.drawText(queryLabel, queryBounds.centerX(), queryBaseline, suggestionHintPaint)

        val visibleResults = snapshot.results.take(3)
        val resultsLeft = queryBounds.right + gap
        val resultsWidth = width - horizontalPadding - resultsLeft
        if (visibleResults.isEmpty()) {
            val message = if (snapshot.query.isBlank()) "Type a name" else "No matches"
            val bounds = RectF(resultsLeft, 0f, width - horizontalPadding, topArea)
            val baseline = bounds.centerY() - (suggestionHintPaint.descent() + suggestionHintPaint.ascent()) / 2
            canvas.drawText(message, bounds.centerX(), baseline, suggestionHintPaint)
            return
        }

        val cellWidth = (resultsWidth - gap * (visibleResults.size - 1)) / visibleResults.size
        visibleResults.forEachIndexed { index, result ->
            val left = resultsLeft + index * (cellWidth + gap)
            val bounds = RectF(left, 0f, left + cellWidth, topArea)
            canvas.drawRoundRect(bounds, radius, radius, keyPaint)
            canvas.drawRoundRect(bounds, radius, radius, keyStrokePaint)
            val baseline = bounds.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText(result.emoji, bounds.centerX(), baseline, textPaint)
            hitEmojiSearchResults += HitEmojiSearchResult(bounds, result)
        }
    }

    private fun drawEmojiCategoryStrip(canvas: Canvas, horizontalPadding: Float, topArea: Float) {
        val entries = EmojiStripModel.entries(hasRecents = emojiRecents.values().isNotEmpty())
        val gap = GlazeKeyboardTokens.Space1Dp * resources.displayMetrics.density
        val availableWidth = width - horizontalPadding * 2 - gap * (entries.size - 1)
        val cellWidth = availableWidth / entries.size
        val radius = GlazeKeyboardTokens.RadiusMediumDp * resources.displayMetrics.density
        entries.forEachIndexed { index, entry ->
            val left = horizontalPadding + index * (cellWidth + gap)
            val bounds = RectF(left, 0f, left + cellWidth, topArea)
            val selected = when {
                entry.search -> false
                entry.recent -> showingEmojiRecents
                else -> !showingEmojiRecents && entry.category == emojiCategory
            }
            if (selected) {
                canvas.drawRoundRect(bounds, radius, radius, keyPaint)
                canvas.drawRoundRect(bounds, radius, radius, keyStrokePaint)
            }
            val baseline = bounds.centerY() - (suggestionPaint.descent() + suggestionPaint.ascent()) / 2
            canvas.drawText(entry.visibleLabel, bounds.centerX(), baseline, suggestionPaint)
            hitEmojiCategories += HitEmojiCategory(bounds, entry)
        }
    }

    private fun drawAlternatePopup(canvas: Canvas, popup: AlternatePopup) {
        val density = resources.displayMetrics.density
        val cell = GlazeKeyboardV16PresentationPolicy
            .interactionFloorDp(glazeV16PresentationContext) * density
        val gap = GlazeKeyboardTokens.Space1Dp * density
        val layout = runCatching {
            AlternatePopupLayout.calculate(
                source = AlternatePopupSourceBounds(
                    left = popup.sourceBounds.left,
                    top = popup.sourceBounds.top,
                    right = popup.sourceBounds.right,
                    bottom = popup.sourceBounds.bottom,
                ),
                itemCount = popup.values.size,
                viewportWidth = width.toFloat(),
                viewportHeight = height.toFloat(),
                cellSize = cell,
                gap = gap,
            )
        }.getOrElse {
            popup.layout = null
            popup.selectedIndex = null
            return
        }
        popup.layout = layout
        val radius = GlazeKeyboardTokens.RadiusMediumDp * density
        val shell = RectF(
            layout.left - gap,
            layout.top - gap,
            layout.left + layout.contentWidth + gap,
            layout.top + layout.contentHeight + gap,
        )
        canvas.drawRoundRect(shell, radius, radius, alternatePopupPaint)
        canvas.drawRoundRect(shell, radius, radius, keyStrokePaint)

        popup.values.forEachIndexed { index, value ->
            val item = layout.itemBounds(index)
            val bounds = RectF(item.left, item.top, item.right, item.bottom)
            canvas.drawRoundRect(
                bounds,
                radius,
                radius,
                if (popup.selectedIndex == index) alternateSelectedPaint else keyPaint,
            )
            canvas.drawRoundRect(bounds, radius, radius, keyStrokePaint)
            val baseline = bounds.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText(value, bounds.centerX(), baseline, textPaint)
        }
    }

    override fun dispatchHoverEvent(event: MotionEvent): Boolean {
        if (accessibilityDelegate.dispatchHoverEvent(event)) return true
        return super.dispatchHoverEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                cancelAlternateInteraction()
                cancelSwipeInteraction()
                val hit = hitKeys.lastOrNull { it.bounds.contains(event.x, event.y) }
                pressedKeyBounds = hit?.let { RectF(it.bounds) }
                swipeDownX = event.x
                swipeDownY = event.y
                swipeDownTimeMs = event.eventTime
                if (canParticipateInSwipe(hit)) {
                    swipeKeyPath += hit!!.key.label.lowercase()
                    swipePath.moveTo(hit.bounds.centerX(), hit.bounds.centerY())
                }
                if (hit != null && alternatesFor(hit).isNotEmpty()) {
                    pendingAlternateHit = hit
                    postDelayed(showAlternatesRunnable, ViewConfiguration.getLongPressTimeout().toLong())
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val popup = alternatePopup
                if (popup != null) {
                    pressedKeyBounds = null
                    popup.selectedIndex = popup.layout?.hitTest(event.x, event.y)
                    invalidate()
                    return true
                }

                val hit = hitKeys.lastOrNull { it.bounds.contains(event.x, event.y) }
                if (!swipeGestureActive && swipeKeyPath.isNotEmpty()) {
                    val travel = hypot(event.x - swipeDownX, event.y - swipeDownY)
                    val density = resources.displayMetrics.density
                    val threshold = max(
                        ViewConfiguration.get(context).scaledTouchSlop * SWIPE_START_SLOP_MULTIPLIER,
                        SWIPE_MIN_TRAVEL_DP * density,
                    )
                    val elapsedMs = event.eventTime - swipeDownTimeMs
                    val movedToDifferentLetter =
                        canParticipateInSwipe(hit) &&
                            hit!!.key.label.lowercase() != swipeKeyPath.firstOrNull()
                    if (
                        travel >= threshold &&
                        elapsedMs >= SWIPE_MIN_GESTURE_MS &&
                        movedToDifferentLetter
                    ) {
                        swipeGestureActive = true
                        removeCallbacks(showAlternatesRunnable)
                        pendingAlternateHit = null
                        alternatePopup = null
                    }
                }

                if (swipeGestureActive) {
                    if (canParticipateInSwipe(hit)) {
                        val label = hit!!.key.label.lowercase()
                        if (swipeKeyPath.lastOrNull() != label) {
                            swipeKeyPath += label
                        }
                    }
                    swipePath.lineTo(event.x, event.y)
                    pressedKeyBounds = hit?.let { RectF(it.bounds) }
                    invalidate()
                    return true
                }

                pendingAlternateHit?.let { pending ->
                    val slop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
                    val expanded = RectF(pending.bounds).apply { inset(-slop, -slop) }
                    if (!expanded.contains(event.x, event.y)) {
                        removeCallbacks(showAlternatesRunnable)
                        pendingAlternateHit = null
                    }
                }
                val nextBounds = hit?.let { RectF(it.bounds) }
                if (nextBounds != pressedKeyBounds) {
                    pressedKeyBounds = nextBounds
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                cancelAlternateInteraction()
                cancelSwipeInteraction()
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                removeCallbacks(showAlternatesRunnable)
                pendingAlternateHit = null
                pressedKeyBounds = null

                if (swipeGestureActive) {
                    val hit = hitKeys.lastOrNull { it.bounds.contains(event.x, event.y) }
                    if (canParticipateInSwipe(hit)) {
                        val label = hit!!.key.label.lowercase()
                        if (swipeKeyPath.lastOrNull() != label) swipeKeyPath += label
                    }
                    val path = swipeKeyPath.toList()
                    cancelSwipeInteraction()
                    if (path.size >= SWIPE_MIN_PATH_KEYS) {
                        listener?.onSwipe(path)
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }
                    invalidate()
                    performClick()
                    return true
                }

                alternatePopup?.let { popup ->
                    val value = popup.selectedIndex?.let(popup.values::getOrNull)
                    alternatePopup = null
                    if (value != null) {
                        listener?.onText(value)
                        announceForAccessibility("Inserted alternate character")
                    }
                    cancelSwipeInteraction()
                    invalidateStructure()
                    performClick()
                    return true
                }
                cancelSwipeInteraction()
                invalidate()
            }
            else -> return true
        }

        hitEmojiSearchResults.lastOrNull { it.bounds.contains(event.x, event.y) }?.let { hit ->
            return activateEmojiSearchResult(hit)
        }

        hitEmojiCategories.lastOrNull { it.bounds.contains(event.x, event.y) }?.let { hit ->
            return activateEmojiCategory(hit)
        }

        hitSuggestions.lastOrNull { it.bounds.contains(event.x, event.y) }?.let { hit ->
            return activateSuggestion(hit)
        }

        val hit = hitKeys.lastOrNull { it.bounds.contains(event.x, event.y) } ?: return true
        return activateKey(hit)
    }

    private fun canParticipateInSwipe(hit: HitKey?): Boolean =
        swipeTypingEnabled &&
            !glazeV16PresentationContext.screenReaderOptimized &&
            layer == KeyboardLayer.LETTERS &&
            hit?.key?.action == Action.TEXT &&
            hit.key.label.codePoints().allMatch { Character.isLetter(it) }

    private fun cancelSwipeInteraction() {
        swipeGestureActive = false
        swipeKeyPath.clear()
        swipePath.reset()
    }

    internal fun accessibilityTargets(): List<KeyboardAccessibilityTarget> = buildList {
        hitKeys.forEachIndexed { index, hit ->
            add(
                KeyboardAccessibilityTarget(
                    id = ACCESSIBILITY_KEY_BASE + index,
                    bounds = RectF(hit.bounds),
                    label = accessibilityLabel(hit.key),
                    selected = hit.key.action == Action.SHIFT && shifted,
                )
            )
        }
        hitSuggestions.forEachIndexed { index, hit ->
            add(
                KeyboardAccessibilityTarget(
                    id = ACCESSIBILITY_SUGGESTION_BASE + index,
                    bounds = RectF(hit.bounds),
                    label = "Suggestion ${hit.value}",
                )
            )
        }
        hitEmojiCategories.forEachIndexed { index, hit ->
            val selected = when {
                hit.entry.search -> false
                hit.entry.recent -> showingEmojiRecents
                else -> !showingEmojiRecents && hit.entry.category == emojiCategory
            }
            add(
                KeyboardAccessibilityTarget(
                    id = ACCESSIBILITY_EMOJI_CATEGORY_BASE + index,
                    bounds = RectF(hit.bounds),
                    label = hit.entry.accessibilityLabel,
                    selected = selected,
                )
            )
        }
        hitEmojiSearchResults.forEachIndexed { index, hit ->
            add(
                KeyboardAccessibilityTarget(
                    id = ACCESSIBILITY_EMOJI_SEARCH_RESULT_BASE + index,
                    bounds = RectF(hit.bounds),
                    label = "Emoji ${hit.result.emoji} from local search",
                )
            )
        }
    }

    internal fun accessibilityTarget(id: Int): KeyboardAccessibilityTarget? =
        accessibilityTargets().firstOrNull { it.id == id }

    internal fun performAccessibilityTarget(id: Int): Boolean {
        cancelAlternateInteraction()
        return when {
            id in ACCESSIBILITY_KEY_BASE until ACCESSIBILITY_SUGGESTION_BASE ->
                hitKeys.getOrNull(id - ACCESSIBILITY_KEY_BASE)?.let(::activateKey) ?: false
            id in ACCESSIBILITY_SUGGESTION_BASE until ACCESSIBILITY_EMOJI_CATEGORY_BASE ->
                hitSuggestions.getOrNull(id - ACCESSIBILITY_SUGGESTION_BASE)?.let(::activateSuggestion) ?: false
            id in ACCESSIBILITY_EMOJI_CATEGORY_BASE until ACCESSIBILITY_EMOJI_SEARCH_RESULT_BASE ->
                hitEmojiCategories.getOrNull(id - ACCESSIBILITY_EMOJI_CATEGORY_BASE)?.let(::activateEmojiCategory) ?: false
            id >= ACCESSIBILITY_EMOJI_SEARCH_RESULT_BASE ->
                hitEmojiSearchResults.getOrNull(id - ACCESSIBILITY_EMOJI_SEARCH_RESULT_BASE)?.let(::activateEmojiSearchResult) ?: false
            else -> false
        }
    }

    private fun accessibilityLabel(key: Key): String = when (key.action) {
        Action.TEXT -> renderedKeyLabel(key)
        Action.SHIFT -> "Shift"
        Action.BACKSPACE -> "Backspace"
        Action.SPACE ->
            if (layer == KeyboardLayer.LETTERS) "Space, English (US)" else "Space"
        Action.ENTER -> "Enter"
        Action.LETTERS -> "Letters"
        Action.SYMBOLS -> "Symbols"
        Action.SYMBOLS_MORE -> "More symbols"
        Action.EMOJI -> "Emoji"
        Action.SETTINGS -> "Keyboard settings"
        Action.EMOJI_SEARCH_CLEAR -> "Clear emoji search"
        Action.EMOJI_SEARCH_CLOSE -> "Close emoji search"
    }

    private fun activateEmojiSearchResult(hit: HitEmojiSearchResult): Boolean {
        emojiRecents.record(hit.result.emoji)
        emojiRecentsStore.save(emojiRecents.values())
        listener?.onText(hit.result.emoji)
        announceForAccessibility("Inserted emoji from local search")
        invalidateStructure()
        performClick()
        return true
    }

    private fun activateEmojiCategory(hit: HitEmojiCategory): Boolean {
        when {
            hit.entry.search -> {
                emojiSearchSession.open()
                showingEmojiRecents = false
            }
            hit.entry.clearRecents -> {
                emojiSearchSession.close()
                emojiRecents.clear()
                emojiRecentsStore.save(emojiRecents.values())
                showingEmojiRecents = false
            }
            hit.entry.recent -> {
                emojiSearchSession.close()
                showingEmojiRecents = emojiRecents.values().isNotEmpty()
            }
            hit.entry.category != null -> {
                emojiSearchSession.close()
                emojiCategory = hit.entry.category
                emojiCategoryStore.save(emojiCategory)
                showingEmojiRecents = false
            }
        }
        announceForAccessibility(hit.entry.accessibilityLabel)
        invalidateStructure()
        performClick()
        return true
    }

    private fun activateSuggestion(hit: HitSuggestion): Boolean {
        listener?.onSuggestion(hit.value)
        performClick()
        return true
    }

    private fun activateKey(hit: HitKey): Boolean {
        val searchActive = layer == KeyboardLayer.EMOJI && emojiSearchSession.snapshot().active
        when (hit.key.action) {
            Action.TEXT -> {
                if (searchActive) {
                    emojiSearchSession.append(hit.key.label.lowercase())
                } else {
                    if (layer == KeyboardLayer.EMOJI) {
                        emojiRecents.record(hit.key.label)
                        emojiRecentsStore.save(emojiRecents.values())
                    }
                    listener?.onText(hit.key.label)
                }
                if (layer == KeyboardLayer.EMOJI) invalidateStructure()
            }
            Action.SHIFT -> listener?.onShift()
            Action.BACKSPACE -> {
                if (searchActive) {
                    emojiSearchSession.backspace()
                    invalidateStructure()
                } else {
                    listener?.onBackspace()
                }
            }
            Action.SPACE -> {
                if (searchActive) {
                    emojiSearchSession.append(" ")
                    invalidateStructure()
                } else {
                    listener?.onSpace()
                }
            }
            Action.ENTER -> listener?.onEnter()
            Action.LETTERS -> switchLayer(KeyboardLayer.LETTERS)
            Action.SYMBOLS -> switchLayer(KeyboardLayer.SYMBOLS)
            Action.SYMBOLS_MORE -> switchLayer(KeyboardLayer.SYMBOLS_MORE)
            Action.EMOJI -> switchLayer(KeyboardLayer.EMOJI)
            Action.SETTINGS -> listener?.onOpenSettings()
            Action.EMOJI_SEARCH_CLEAR -> {
                emojiSearchSession.clear()
                announceForAccessibility("Emoji search cleared")
                invalidateStructure()
            }
            Action.EMOJI_SEARCH_CLOSE -> {
                emojiSearchSession.close()
                announceForAccessibility("Emoji search closed")
                invalidateStructure()
            }
        }
        performClick()
        return true
    }

    private fun invalidateStructure() {
        invalidate()
        accessibilityDelegate.invalidateVirtualRoot()
    }

    private fun cancelAlternateInteraction() {
        removeCallbacks(showAlternatesRunnable)
        pressedKeyBounds = null
        pendingAlternateHit = null
        alternatePopup = null
    }

    private fun switchLayer(value: KeyboardLayer) {
        cancelAlternateInteraction()
        layer = value
        shifted = false
        if (layer != KeyboardLayer.EMOJI) emojiSearchSession.close()
        listener?.onLayerChanged(layer)
        invalidateStructure()
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private companion object {
        const val ACCESSIBILITY_KEY_BASE = 1_000
        const val ACCESSIBILITY_SUGGESTION_BASE = 2_000
        const val ACCESSIBILITY_EMOJI_CATEGORY_BASE = 3_000
        const val ACCESSIBILITY_EMOJI_SEARCH_RESULT_BASE = 4_000
        const val SWIPE_START_SLOP_MULTIPLIER = 2.0f
        const val SWIPE_MIN_TRAVEL_DP = 20f
        const val SWIPE_MIN_GESTURE_MS = 55L
        const val SWIPE_MIN_PATH_KEYS = 3
        val DIGIT_ROW = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    }
}
