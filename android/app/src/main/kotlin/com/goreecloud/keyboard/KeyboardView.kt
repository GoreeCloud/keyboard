package com.goreecloud.keyboard

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.SoundEffectConstants
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
        fun onSwipeGesture(gesture: SwipeGesture) = onSwipe(gesture.keyPath)
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
    private val longPressHintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
        textSize = 9f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }
    private val alternatePopupPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val alternateSelectedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val suggestionSurfacePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val toolbarSurfacePaint = Paint(Paint.ANTI_ALIAS_FLAG)
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
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = KeyboardFunctionalGlyphs.STROKE_DP * resources.displayMetrics.density
    }
    private val selectedIconPaint = Paint(iconPaint)

    private val hitKeys = mutableListOf<HitKey>()
    private val hitSuggestions = mutableListOf<HitSuggestion>()
    private val hitEmojiCategories = mutableListOf<HitEmojiCategory>()
    private val hitEmojiSearchResults = mutableListOf<HitEmojiSearchResult>()
    private val emojiRecentsStore = LocalEmojiRecentsStore(context)
    private val emojiCategoryStore = LocalEmojiCategoryStore(context)
    private val emojiRecents = EmojiRecents(initialValues = emojiRecentsStore.load())
    private val emojiSearchSession = EmojiSearchSession()
    private val accessibilityDelegate = KeyboardAccessibilityDelegate(this)
    private val mainHandler = Handler(Looper.getMainLooper())
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
    private var toolbarStyle = KeyboardToolbarStyle.ICONS_ONLY
    private var keyPressHapticsEnabled = true
    private var keyPressSoundEnabled = false
    private var emojiToolbarEnabled = true
    private var longPressHintsEnabled = true
    private var longPressDelayPreference = KeyboardLongPressDelay.SYSTEM
    private var numberRowVisible = true
    private var swipeTypingEnabled = false
    private var swipeTrailEnabled = true
    private var swipeGestureActive = false
    private val swipeKeyPath = mutableListOf<String>()
    private val swipeTouchPoints = mutableListOf<SwipePoint>()
    private val swipePath = Path()
    private var swipeDownX = 0f
    private var swipeDownY = 0f
    private var swipeDownTimeMs = 0L
    private var lastLetterTapUpTimeMs = Long.MIN_VALUE
    private var touchDownHit: HitKey? = null
    private var backspaceRepeatHit: HitKey? = null

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

    override fun onDetachedFromWindow() {
        cancelBackspaceRepeat()
        cancelAlternateInteraction()
        super.onDetachedFromWindow()
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

    private val backspaceRepeatRunnable = object : Runnable {
        override fun run() {
            val hit = backspaceRepeatHit ?: return
            if (hit.key.action != Action.BACKSPACE) return
            performKeyPressHaptic()
            listener?.onBackspace()
            mainHandler.postDelayed(this, BACKSPACE_REPEAT_INTERVAL_MS)
        }
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

    internal fun setSwipeTrailEnabled(enabled: Boolean) {
        if (swipeTrailEnabled == enabled) return
        swipeTrailEnabled = enabled
        if (!enabled) invalidate()
    }

    internal fun setNumberRowVisible(visible: Boolean) {
        if (numberRowVisible == visible) return
        numberRowVisible = visible
        invalidateStructure()
    }

    internal fun setEmojiToolbarEnabled(enabled: Boolean) {
        if (emojiToolbarEnabled == enabled) return
        emojiToolbarEnabled = enabled
        invalidateStructure()
    }

    internal fun setLongPressHintsEnabled(enabled: Boolean) {
        if (longPressHintsEnabled == enabled) return
        longPressHintsEnabled = enabled
        invalidate()
    }

    internal fun setLongPressDelay(value: KeyboardLongPressDelay) {
        longPressDelayPreference = value
    }

    internal fun setKeyHeightPreference(value: KeyboardKeyHeight) {
        if (keyHeightPreference == value) return
        keyHeightPreference = value
        invalidateStructure()
    }

    internal fun setToolbarStyle(value: KeyboardToolbarStyle) {
        if (toolbarStyle == value) return
        toolbarStyle = value
        invalidateStructure()
    }

    internal fun setKeyPressHapticsEnabled(enabled: Boolean) {
        keyPressHapticsEnabled = enabled
        isHapticFeedbackEnabled = enabled
    }

    internal fun setKeyPressSoundEnabled(enabled: Boolean) {
        keyPressSoundEnabled = enabled
        isSoundEffectsEnabled = enabled
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
        val toolbarHeight = if (layer == KeyboardLayer.EMOJI) 0f else topArea
        val keyboardTop = topArea + toolbarHeight + GlazeKeyboardTokens.Space2Dp * density
        val bottomSafeGap = GlazeKeyboardTokens.BottomSafeGapDp * density
        val contentBottom = max(
            keyboardTop + rows.size,
            height - bottomNavigationInsetPx.toFloat() - bottomSafeGap,
        )
        val rowGapCount = (rows.size - 1).coerceAtLeast(0)
        val rowHeight = max(1f, (contentBottom - keyboardTop - gap * rowGapCount) / rows.size)
        val keyRadius = GlazeKeyboardTokens.RadiusMediumDp * density

        drawSuggestionStrip(canvas, horizontalPadding, topArea)
        if (toolbarHeight > 0f) {
            drawUtilityToolbar(
                canvas = canvas,
                horizontalPadding = horizontalPadding,
                top = topArea,
                height = toolbarHeight,
            )
        }

        rows.forEachIndexed { rowIndex, row ->
            val totalWeight = row.sumOf { it.weight.toDouble() }.toFloat()
            val rowHorizontalPadding = horizontalPadding + centeredLetterRowInset(row, gap, horizontalPadding)
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

                drawKeyContent(canvas, key, visualBounds)
                hitKeys += HitKey(bounds, key)
                left += keyWidth + gap
            }
        }

        if (swipeGestureActive && swipeTrailEnabled) {
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
        row: List<Key>,
        gap: Float,
        horizontalPadding: Float,
    ): Float {
        if (layer != KeyboardLayer.LETTERS || row.size != 9 || row.any { it.action != Action.TEXT }) {
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
            KeyboardLayer.LETTERS -> buildList {
                if (numberRowVisible) add(DIGIT_ROW.map(::textKey))
                add(characterRows[0].map(::textKey))
                add(characterRows[1].map(::textKey))
                add(
                    listOf(Key("⇧", 1.25f, Action.SHIFT)) +
                        characterRows[2].map(::textKey) +
                        listOf(Key("⌫", 1.25f, Action.BACKSPACE)),
                )
                add(
                    listOf(
                        Key("?123", 1.2f, Action.SYMBOLS),
                        textKey(",").copy(weight = 0.9f),
                        Key("space", 4.6f, Action.SPACE),
                        textKey(".").copy(weight = 0.9f),
                        Key("↵", 1.2f, Action.ENTER),
                    ),
                )
            }
            KeyboardLayer.SYMBOLS -> listOf(
                characterRows[0].map(::textKey),
                characterRows[1].map(::textKey),
                characterRows[2].map(::textKey) + listOf(Key("⌫", 1.25f, Action.BACKSPACE)),
                listOf(Key("ABC", 1.15f, Action.LETTERS), Key("=\\<", 1.15f, Action.SYMBOLS_MORE), Key("space", 5.0f, Action.SPACE), Key("↵", 1.25f, Action.ENTER)),
            )
            KeyboardLayer.SYMBOLS_MORE -> listOf(
                characterRows[0].map(::textKey),
                characterRows[1].map(::textKey),
                characterRows[2].map(::textKey) + listOf(Key("⌫", 1.25f, Action.BACKSPACE)),
                listOf(Key("ABC", 1.15f, Action.LETTERS), Key("?123", 1.15f, Action.SYMBOLS), Key("space", 5.0f, Action.SPACE), Key("↵", 1.25f, Action.ENTER)),
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

    private fun drawKeyContent(canvas: Canvas, key: Key, bounds: RectF) {
        when (key.action) {
            Action.SHIFT -> drawShiftIcon(canvas, bounds)
            Action.BACKSPACE -> drawBackspaceIcon(canvas, bounds)
            Action.ENTER -> drawEnterIcon(canvas, bounds)
            else -> {
                val label = renderedKeyLabel(key)
                val paint = keyLabelPaint(key)
                val baseline = bounds.centerY() - (paint.descent() + paint.ascent()) / 2
                canvas.drawText(label, bounds.centerX(), baseline, paint)
                drawLongPressHint(canvas, key, bounds)
            }
        }
    }

    private fun drawLongPressHint(canvas: Canvas, key: Key, bounds: RectF) {
        if (!longPressHintsEnabled || key.action != Action.TEXT || layer == KeyboardLayer.EMOJI) return
        val hint = KeyAlternates.forKey(key.label).firstOrNull() ?: return
        val density = resources.displayMetrics.density
        val baseline = bounds.top - longPressHintPaint.ascent() + 2f * density
        canvas.drawText(hint, bounds.right - 6f * density, baseline, longPressHintPaint)
    }

    private fun drawShiftIcon(canvas: Canvas, bounds: RectF) {
        KeyboardFunctionalGlyphs.drawShift(
            canvas = canvas,
            bounds = bounds,
            paint = if (shifted) selectedIconPaint else iconPaint,
        )
    }

    private fun drawBackspaceIcon(canvas: Canvas, bounds: RectF) {
        KeyboardFunctionalGlyphs.drawBackspace(canvas, bounds, iconPaint)
    }

    private fun drawEnterIcon(canvas: Canvas, bounds: RectF) {
        KeyboardFunctionalGlyphs.drawEnter(canvas, bounds, iconPaint)
    }

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
        longPressHintPaint.color = palette.onSurfaceMutedArgb
        alternatePopupPaint.color = palette.canvasArgb
        alternateSelectedPaint.color = palette.surfaceArgb
        suggestionSurfacePaint.color = palette.surfaceArgb
        toolbarSurfacePaint.color = palette.surfaceArgb
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
        iconPaint.color = palette.onSurfaceArgb
        iconPaint.alpha = FUNCTIONAL_ICON_ALPHA
        selectedIconPaint.color = resolveThemeAccentColor(palette.onSurfaceArgb)
        selectedIconPaint.alpha = 255
    }

    private fun resolveThemeAccentColor(fallback: Int): Int {
        val value = TypedValue()
        if (!context.theme.resolveAttribute(android.R.attr.colorAccent, value, true)) return fallback
        return if (value.resourceId != 0) context.getColor(value.resourceId) else value.data
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

        if (layer != KeyboardLayer.LETTERS || suggestions.isEmpty()) {
            // Keep this region visually quiet when suggestions are unavailable. The utility toolbar
            // is rendered separately below it, so no branded/status filler message is necessary.
            return
        }

        val density = resources.displayMetrics.density
        val verticalInset = GlazeKeyboardTokens.Space1Dp * density
        val radius = GlazeKeyboardTokens.RadiusMediumDp * density
        val stripBounds = RectF(
            horizontalPadding,
            verticalInset,
            width - horizontalPadding,
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

    private fun drawUtilityToolbar(
        canvas: Canvas,
        horizontalPadding: Float,
        top: Float,
        height: Float,
    ) {
        val density = resources.displayMetrics.density
        val gap = GlazeKeyboardTokens.Space1Dp * density
        val verticalInset = GlazeKeyboardTokens.Space1Dp * density
        val outerRadius = GlazeKeyboardTokens.OpticalContainerDp * density
        val buttonRadius = GlazeKeyboardTokens.RadiusMediumDp * density
        val actions = buildList {
            if (emojiToolbarEnabled) add(Key("emoji", action = Action.EMOJI))
            add(Key("settings", action = Action.SETTINGS))
        }

        val surfaceBounds = RectF(
            horizontalPadding,
            top + verticalInset,
            width - horizontalPadding,
            top + height - verticalInset,
        )
        canvas.drawRoundRect(surfaceBounds, outerRadius, outerRadius, toolbarSurfacePaint)

        val innerPadding = GlazeKeyboardTokens.Space1Dp * density
        val availableWidth = surfaceBounds.width() - innerPadding * 2f
        val buttonWidth = when (toolbarStyle) {
            KeyboardToolbarStyle.ICONS_ONLY ->
                minOf(
                    KeyboardFunctionalGlyphs.TOOLBAR_HIT_DP * density,
                    availableWidth / actions.size.coerceAtLeast(1),
                )
            KeyboardToolbarStyle.ICONS_WITH_LABELS ->
                ((availableWidth - gap * (actions.size - 1)) / actions.size)
                    .coerceAtLeast(KeyboardFunctionalGlyphs.TOOLBAR_HIT_DP * density)
        }

        var left = surfaceBounds.left + innerPadding
        actions.forEach { key ->
            val hitBounds = RectF(
                left,
                top,
                minOf(left + buttonWidth, surfaceBounds.right),
                top + height,
            )
            val visualBounds = when (toolbarStyle) {
                KeyboardToolbarStyle.ICONS_ONLY ->
                    KeyboardFunctionalGlyphs.centeredSquare(
                        hitBounds,
                        KeyboardFunctionalGlyphs.TOOLBAR_VISUAL_DP * density,
                    )
                KeyboardToolbarStyle.ICONS_WITH_LABELS ->
                    RectF(
                        hitBounds.left,
                        surfaceBounds.top + innerPadding,
                        hitBounds.right,
                        surfaceBounds.bottom - innerPadding,
                    )
            }

            if (toolbarStyle == KeyboardToolbarStyle.ICONS_WITH_LABELS) {
                canvas.drawRoundRect(visualBounds, buttonRadius, buttonRadius, utilityKeyOverlayPaint)
            }
            if (isPressedKey(hitBounds)) {
                canvas.drawRoundRect(visualBounds, buttonRadius, buttonRadius, pressedKeyPaint)
            }

            drawToolbarContent(canvas, key, visualBounds)
            hitKeys += HitKey(hitBounds, key)
            left = hitBounds.right + gap
        }
    }

    private fun drawToolbarContent(canvas: Canvas, key: Key, bounds: RectF) {
        when (toolbarStyle) {
            KeyboardToolbarStyle.ICONS_ONLY -> drawToolbarGlyph(canvas, key, bounds)
            KeyboardToolbarStyle.ICONS_WITH_LABELS -> {
                val iconRegion = RectF(
                    bounds.left + bounds.width() * 0.04f,
                    bounds.top,
                    bounds.left + bounds.width() * 0.38f,
                    bounds.bottom,
                )
                val iconBounds = KeyboardFunctionalGlyphs.centeredSquare(
                    iconRegion,
                    minOf(
                        KeyboardFunctionalGlyphs.TOOLBAR_VISUAL_DP * resources.displayMetrics.density,
                        iconRegion.width(),
                    ),
                )
                drawToolbarGlyph(canvas, key, iconBounds)

                val label = when (key.action) {
                    Action.EMOJI -> "Emoji"
                    Action.SETTINGS -> "Settings"
                    else -> ""
                }
                val labelX = bounds.left + bounds.width() * 0.68f
                val baseline =
                    bounds.centerY() - (utilityTextPaint.descent() + utilityTextPaint.ascent()) / 2f
                canvas.drawText(label, labelX, baseline, utilityTextPaint)
            }
        }
    }

    private fun drawToolbarGlyph(canvas: Canvas, key: Key, bounds: RectF) {
        when (key.action) {
            Action.EMOJI -> KeyboardFunctionalGlyphs.drawEmoji(canvas, bounds, iconPaint)
            Action.SETTINGS -> KeyboardFunctionalGlyphs.drawSettings(canvas, bounds, iconPaint)
            else -> Unit
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
                val hit = hitKeyAt(event.x, event.y)
                touchDownHit = hit
                pressedKeyBounds = hit?.let { RectF(it.bounds) }
                swipeDownX = event.x
                swipeDownY = event.y
                swipeDownTimeMs = event.eventTime
                if (hit?.key?.action == Action.BACKSPACE) {
                    beginBackspaceRepeat(hit)
                } else if (canParticipateInSwipe(hit)) {
                    swipeKeyPath += hit!!.key.label.lowercase()
                    swipeTouchPoints += SwipePoint(event.x, event.y)
                    swipePath.moveTo(event.x, event.y)
                }
                if (hit != null && hit.key.action != Action.BACKSPACE && alternatesFor(hit).isNotEmpty()) {
                    pendingAlternateHit = hit
                    postDelayed(showAlternatesRunnable, longPressDelayMs())
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

                val hit = hitKeyAt(event.x, event.y)
                backspaceRepeatHit?.let { repeatHit ->
                    val slop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
                    val expanded = RectF(repeatHit.bounds).apply { inset(-slop, -slop) }
                    if (!expanded.contains(event.x, event.y)) cancelBackspaceRepeat()
                }
                if (swipeKeyPath.isNotEmpty()) {
                    appendSwipeMotionSamples(event)
                    maybeActivateSwipe(event)
                }

                if (swipeGestureActive) {
                    rebuildSwipePathFromSamples()
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
                touchDownHit = null
                cancelBackspaceRepeat()
                cancelAlternateInteraction()
                cancelSwipeInteraction()
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                val backspaceWasHeld = backspaceRepeatHit != null
                cancelBackspaceRepeat()
                removeCallbacks(showAlternatesRunnable)
                pendingAlternateHit = null
                pressedKeyBounds = null

                if (backspaceWasHeld) {
                    touchDownHit = null
                    cancelSwipeInteraction()
                    invalidate()
                    performClick()
                    return true
                }

                if (swipeKeyPath.isNotEmpty()) {
                    appendSwipeMotionSamples(event)
                    maybeActivateSwipe(event)
                }

                if (swipeGestureActive) {
                    val path = swipeKeyPath.toList()
                    val gesture = SwipeGesture(
                        keyPath = path,
                        points = swipeTouchPoints.toList(),
                        keyCenters = letterKeyCenters(),
                    )
                    touchDownHit = null
                    cancelSwipeInteraction()
                    if (path.size >= SWIPE_MIN_PATH_KEYS) {
                        performKeyPressHaptic()
                        listener?.onSwipeGesture(gesture)
                    }
                    invalidate()
                    performClick()
                    return true
                }

                alternatePopup?.let { popup ->
                    val value = popup.selectedIndex?.let(popup.values::getOrNull)
                    alternatePopup = null
                    if (value != null) {
                        performKeyPressHaptic()
                        listener?.onText(value)
                        announceForAccessibility("Inserted alternate character")
                    }
                    touchDownHit = null
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

        val exactHit = hitKeyAt(event.x, event.y)
        val releaseTravel = hypot(event.x - swipeDownX, event.y - swipeDownY)
        val fallbackHit = touchDownHit?.takeIf {
            releaseTravel <= ViewConfiguration.get(context).scaledTouchSlop * TAP_RELEASE_SLOP_MULTIPLIER
        }
        touchDownHit = null
        // A tap that stays inside release slop belongs to the key that received ACTION_DOWN.
        // This avoids accidental adjacent-key commits when a finger drifts across a visual gap
        // just before release. Larger motion still uses the release hit and swipe path.
        val hit = fallbackHit ?: exactHit ?: return true
        if (
            layer == KeyboardLayer.LETTERS &&
            hit.key.action == Action.TEXT &&
            hit.key.label.length == 1 &&
            hit.key.label[0].lowercaseChar() in 'a'..'z'
        ) {
            lastLetterTapUpTimeMs = event.eventTime
        }
        return activateKey(hit)
    }

    private fun hitKeyAt(x: Float, y: Float): HitKey? {
        hitKeys.lastOrNull { it.bounds.contains(x, y) }?.let { return it }

        val density = resources.displayMetrics.density
        val nearMissRadius = minOf(
            ViewConfiguration.get(context).scaledTouchSlop * 0.75f,
            TAP_NEAR_MISS_MAX_DP * density,
        )
        if (nearMissRadius <= 0f) return null

        return hitKeys.asSequence()
            .mapNotNull { hit ->
                val expanded = RectF(hit.bounds).apply {
                    inset(-nearMissRadius, -nearMissRadius)
                }
                if (!expanded.contains(x, y)) {
                    null
                } else {
                    hit to hypot(
                        (x - hit.bounds.centerX()).toDouble(),
                        (y - hit.bounds.centerY()).toDouble(),
                    )
                }
            }
            .minByOrNull { (_, distance) -> distance }
            ?.first
    }

    private fun canParticipateInSwipe(hit: HitKey?): Boolean =
        swipeTypingEnabled &&
            !glazeV16PresentationContext.screenReaderOptimized &&
            layer == KeyboardLayer.LETTERS &&
            hit?.key?.action == Action.TEXT &&
            hit.key.label.codePoints().allMatch { Character.isLetter(it) }

    private fun maybeActivateSwipe(event: MotionEvent) {
        if (swipeGestureActive || swipeKeyPath.isEmpty()) return

        val travel = hypot(event.x - swipeDownX, event.y - swipeDownY)
        val density = resources.displayMetrics.density
        val threshold = max(
            ViewConfiguration.get(context).scaledTouchSlop * SWIPE_START_SLOP_MULTIPLIER,
            SWIPE_MIN_TRAVEL_DP * density,
        )
        val elapsedMs = event.eventTime - swipeDownTimeMs
        val recentFastTyping =
            lastLetterTapUpTimeMs != Long.MIN_VALUE &&
                swipeDownTimeMs - lastLetterTapUpTimeMs <= FAST_TYPING_GUARD_WINDOW_MS
        val requiredTravel = if (recentFastTyping) {
            max(threshold, SWIPE_RECENT_TYPING_MIN_TRAVEL_DP * density)
        } else {
            threshold
        }
        val requiredDuration = if (recentFastTyping) {
            SWIPE_RECENT_TYPING_MIN_GESTURE_MS
        } else {
            SWIPE_MIN_GESTURE_MS
        }
        val firstSwipeKey = swipeKeyPath.firstOrNull()
        val movedToDifferentLetter =
            firstSwipeKey != null && swipeKeyPath.any { it != firstSwipeKey }

        if (
            travel >= requiredTravel &&
            elapsedMs >= requiredDuration &&
            movedToDifferentLetter
        ) {
            swipeGestureActive = true
            removeCallbacks(showAlternatesRunnable)
            pendingAlternateHit = null
            alternatePopup = null
        }
    }

    private fun appendSwipeMotionSamples(event: MotionEvent) {
        for (historyIndex in 0 until event.historySize) {
            appendSwipeSample(
                x = event.getHistoricalX(historyIndex),
                y = event.getHistoricalY(historyIndex),
            )
        }
        appendSwipeSample(event.x, event.y)
    }

    private fun appendSwipeSample(x: Float, y: Float) {
        appendSwipeTouchPoint(x, y)
        val hit = hitKeyAt(x, y)
        if (canParticipateInSwipe(hit)) {
            val label = hit!!.key.label.lowercase()
            if (swipeKeyPath.lastOrNull() != label) swipeKeyPath += label
        }
    }

    private fun rebuildSwipePathFromSamples() {
        swipePath.reset()
        val first = swipeTouchPoints.firstOrNull() ?: return
        swipePath.moveTo(first.x, first.y)
        swipeTouchPoints.drop(1).forEach { point ->
            swipePath.lineTo(point.x, point.y)
        }
    }

    private fun appendSwipeTouchPoint(x: Float, y: Float) {
        val point = SwipePoint(x, y)
        val previous = swipeTouchPoints.lastOrNull()
        val minimumSpacing = SWIPE_TOUCH_SAMPLE_DP * resources.displayMetrics.density
        if (
            previous == null ||
            hypot((point.x - previous.x).toDouble(), (point.y - previous.y).toDouble()) >= minimumSpacing
        ) {
            swipeTouchPoints += point
        }
    }

    private fun letterKeyCenters(): Map<String, SwipePoint> =
        hitKeys.asSequence()
            .filter { hit ->
                hit.key.action == Action.TEXT &&
                    hit.key.label.length == 1 &&
                    hit.key.label[0].lowercaseChar() in 'a'..'z'
            }
            .associate { hit ->
                hit.key.label.lowercase() to SwipePoint(
                    hit.bounds.centerX(),
                    hit.bounds.centerY(),
                )
            }

    private fun cancelSwipeInteraction() {
        swipeGestureActive = false
        swipeKeyPath.clear()
        swipeTouchPoints.clear()
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
        performKeyPressHaptic()
        emojiRecents.record(hit.result.emoji)
        emojiRecentsStore.save(emojiRecents.values())
        listener?.onText(hit.result.emoji)
        announceForAccessibility("Inserted emoji from local search")
        invalidateStructure()
        performClick()
        return true
    }

    private fun activateEmojiCategory(hit: HitEmojiCategory): Boolean {
        performKeyPressHaptic()
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
        performKeyPressHaptic()
        listener?.onSuggestion(hit.value)
        performClick()
        return true
    }

    private fun activateKey(hit: HitKey): Boolean {
        performKeyPressHaptic()
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

    private fun performKeyPressHaptic() {
        if (keyPressHapticsEnabled) {
            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
        if (keyPressSoundEnabled) {
            playSoundEffect(SoundEffectConstants.CLICK)
        }
    }

    private fun longPressDelayMs(): Long = when (longPressDelayPreference) {
        KeyboardLongPressDelay.FAST -> 300L
        KeyboardLongPressDelay.SYSTEM -> ViewConfiguration.getLongPressTimeout().toLong()
        KeyboardLongPressDelay.RELAXED -> 650L
    }

    private fun invalidateStructure() {
        invalidate()
        accessibilityDelegate.invalidateVirtualRoot()
    }

    private fun beginBackspaceRepeat(hit: HitKey) {
        cancelBackspaceRepeat()
        backspaceRepeatHit = hit
        performKeyPressHaptic()
        listener?.onBackspace()
        mainHandler.postDelayed(backspaceRepeatRunnable, BACKSPACE_REPEAT_INITIAL_DELAY_MS)
    }

    private fun cancelBackspaceRepeat() {
        mainHandler.removeCallbacks(backspaceRepeatRunnable)
        backspaceRepeatHit = null
    }

    private fun cancelAlternateInteraction() {
        removeCallbacks(showAlternatesRunnable)
        pressedKeyBounds = null
        pendingAlternateHit = null
        alternatePopup = null
    }

    private fun switchLayer(value: KeyboardLayer) {
        cancelBackspaceRepeat()
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
        const val TAP_RELEASE_SLOP_MULTIPLIER = 1.6f
        const val TAP_NEAR_MISS_MAX_DP = 10f
        const val FUNCTIONAL_ICON_ALPHA = 224
        const val SWIPE_START_SLOP_MULTIPLIER = 2.0f
        const val SWIPE_MIN_TRAVEL_DP = 20f
        const val SWIPE_MIN_GESTURE_MS = 55L
        const val SWIPE_RECENT_TYPING_MIN_GESTURE_MS = 82L
        const val FAST_TYPING_GUARD_WINDOW_MS = 240L
        const val SWIPE_RECENT_TYPING_MIN_TRAVEL_DP = 32f
        const val SWIPE_MIN_PATH_KEYS = 2
        const val SWIPE_TOUCH_SAMPLE_DP = 3.5f
        const val BACKSPACE_REPEAT_INITIAL_DELAY_MS = 360L
        const val BACKSPACE_REPEAT_INTERVAL_MS = 55L
        val DIGIT_ROW = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    }
}
