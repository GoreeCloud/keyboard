package com.goreecloud.keyboard

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeyboardAccessibilityRuntimeTest {
    @Test
    fun renderedKeysAreVirtualButtonsAndAccessibilityClickUsesRealTextPath() {
        val view = createRenderedKeyboard()
        val targets = view.accessibilityTargets()
        assertTrue("Rendered keyboard must expose virtual controls", targets.isNotEmpty())
        assertEquals("Virtual target IDs must be unique", targets.size, targets.map { it.id }.toSet().size)

        val q = targets.first { it.label == "q" }
        assertTrue("Rendered q virtual target must have positive width", q.bounds.width() > 0f)
        assertTrue("Rendered q virtual target must have positive height", q.bounds.height() > 0f)

        // Exercise Android's actual platform provider bridge. ViewCompat's wrapper around an
        // already-created platform provider intentionally does not proxy its compat methods.
        val provider = view.accessibilityNodeProvider
        assertNotNull("ExploreByTouchHelper must expose a platform node provider", provider)
        val node = provider!!.createAccessibilityNodeInfo(q.id)
        assertNotNull("Virtual q node must be creatable from its exposed target ID", node)
        assertEquals("Virtual q node must expose its rendered label", "q", node!!.contentDescription?.toString())
        assertEquals(
            "Virtual q node must expose button semantics",
            "android.widget.Button",
            node.className?.toString(),
        )
        assertTrue("Virtual q node must expose clickable semantics", node.isClickable)

        val committed = mutableListOf<String>()
        view.listener = listener(onText = { committed += it })
        assertTrue(
            "Accessibility ACTION_CLICK must be handled by the virtual q node",
            provider.performAction(q.id, AccessibilityNodeInfo.ACTION_CLICK, null),
        )
        assertEquals(
            "Accessibility activation must use the normal listener path",
            listOf("q"),
            committed,
        )
    }

    @Test
    fun lettersExposeDirectPeriodKeyThroughTheRealTextPath() {
        val view = createRenderedKeyboard()
        val period = view.accessibilityTargets().first { it.label == "." }
        val committed = mutableListOf<String>()
        view.listener = listener(onText = { committed += it })

        assertTrue(
            "Direct period key must activate through the normal text listener",
            view.performAccessibilityTarget(period.id),
        )
        assertEquals(listOf("."), committed)
    }

    @Test
    fun longPressAlternatesAreDiscoverableAndActionableThroughNativeNodeActions() {
        val view = createRenderedKeyboard()
        val a = view.accessibilityTargets().first { it.label == "a" }
        val provider = view.accessibilityNodeProvider
        assertNotNull("ExploreByTouchHelper must expose a platform node provider", provider)

        val node = provider!!.createAccessibilityNodeInfo(a.id)
        assertNotNull("Virtual a node must be creatable", node)
        assertTrue("Keys with local alternates must expose long-click semantics", node!!.isLongClickable)
        assertTrue(
            "Keys with local alternates must expose ACTION_LONG_CLICK",
            node.actionList.any { it.id == AccessibilityNodeInfo.ACTION_LONG_CLICK },
        )

        val insertAcute = node.actionList.firstOrNull { it.label?.toString() == "Insert á" }
        assertNotNull("Local acute-a alternate must be discoverable as a custom action", insertAcute)

        val committed = mutableListOf<String>()
        view.listener = listener(onText = { committed += it })
        assertTrue(
            "Long click must provide an accessibility discovery path for alternates",
            provider.performAction(a.id, AccessibilityNodeInfo.ACTION_LONG_CLICK, null),
        )
        assertTrue(
            "Alternate custom action must be handled by the virtual key",
            provider.performAction(a.id, insertAcute!!.id, null),
        )
        assertEquals(
            "Alternate accessibility activation must use the normal text listener path",
            listOf("á"),
            committed,
        )
    }

    @Test
    fun suggestionsAndEmojiControlsRemainDiscoverableAndActionable() {
        val view = createRenderedKeyboard(listOf("hello", "help", "hero"))
        val selectedSuggestions = mutableListOf<String>()
        view.listener = listener(onSuggestion = { selectedSuggestions += it })

        var targets = view.accessibilityTargets()
        val hello = targets.first { it.label == "Suggestion hello" }
        assertTrue("Suggestion virtual target must activate", view.performAccessibilityTarget(hello.id))
        assertEquals("Suggestion activation must use the normal suggestion listener", listOf("hello"), selectedSuggestions)

        view.setLayer(KeyboardLayer.EMOJI)
        render(view)
        targets = view.accessibilityTargets()
        val search = targets.first { it.label == "Search emoji" }
        assertTrue("Search emoji virtual control must activate", view.performAccessibilityTarget(search.id))

        render(view)
        targets = view.accessibilityTargets()
        assertTrue("Emoji search must expose Clear", targets.any { it.label == "Clear emoji search" })
        assertTrue("Emoji search must expose Close", targets.any { it.label == "Close emoji search" })
        val q = targets.first { it.label == "q" }
        val provider = view.accessibilityNodeProvider
        assertNotNull("Emoji search must retain the platform accessibility provider", provider)
        val qNode = provider!!.createAccessibilityNodeInfo(q.id)
        assertNotNull("Emoji-search q node must be creatable", qNode)
        assertFalse(
            "Emoji-search query keys must not gain long-press alternate semantics",
            qNode!!.isLongClickable,
        )
        assertFalse(
            "Emoji-search query keys must not expose alternate custom actions",
            qNode.actionList.any { it.label?.toString()?.startsWith("Insert ") == true },
        )
    }

    @Test
    fun selectedVirtualStateTracksShiftAndEmojiCategoryPresentation() {
        val view = createRenderedKeyboard()
        view.setShifted(true)
        render(view)

        var targets = view.accessibilityTargets()
        val shift = targets.first { it.label == "Shift" }
        assertTrue("Shift virtual node must expose selected state", shift.selected)
        assertTrue("Shifted text keys must expose rendered uppercase labels", targets.any { it.label == "Q" })

        view.setLayer(KeyboardLayer.EMOJI)
        render(view)
        targets = view.accessibilityTargets()
        assertTrue(
            "Exactly one ordinary emoji category should expose selected state initially",
            targets.count { it.selected && it.label.endsWith(" emoji") } == 1,
        )
    }

    private fun createRenderedKeyboard(suggestions: List<String> = emptyList()): KeyboardView {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        return KeyboardView(context).apply {
            setSuggestions(suggestions)
            measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY),
            )
            layout(0, 0, measuredWidth, measuredHeight)
            render(this)
        }
    }

    private fun render(view: KeyboardView) {
        view.draw(Canvas(Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)))
    }

    private fun listener(
        onText: (String) -> Unit = {},
        onSuggestion: (String) -> Unit = {},
    ) = object : KeyboardView.Listener {
        override fun onText(value: String) = onText(value)
        override fun onSpace() = Unit
        override fun onBackspace() = Unit
        override fun onEnter() = Unit
        override fun onShift() = Unit
        override fun onSuggestion(value: String) = onSuggestion(value)
        override fun onLayerChanged(layer: KeyboardLayer) = Unit
    }
}
