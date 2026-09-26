#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
VIEW = ROOT / "android/app/src/main/kotlin/com/goreecloud/keyboard/KeyboardView.kt"
DELEGATE = ROOT / "android/app/src/main/kotlin/com/goreecloud/keyboard/KeyboardAccessibilityDelegate.kt"
TEST = ROOT / "android/app/src/androidTest/kotlin/com/goreecloud/keyboard/KeyboardAccessibilityRuntimeTest.kt"
GRADLE = ROOT / "android/app/build.gradle.kts"
ACTION_IDS = ROOT / "android/app/src/main/res/values/accessibility_ids.xml"


def fail(message: str) -> None:
    raise SystemExit(f"Keyboard accessibility boundary failed: {message}")


def require_all(label: str, text: str, markers: tuple[str, ...]) -> None:
    for marker in markers:
        if marker not in text:
            fail(f"{label} missing `{marker}`")


def main() -> None:
    for path in (VIEW, DELEGATE, TEST, GRADLE, ACTION_IDS):
        if not path.is_file():
            fail(f"missing required evidence: {path.relative_to(ROOT)}")

    view_text = VIEW.read_text(encoding="utf-8")
    delegate_text = DELEGATE.read_text(encoding="utf-8")
    test_text = TEST.read_text(encoding="utf-8")
    gradle_text = GRADLE.read_text(encoding="utf-8")
    action_ids_text = ACTION_IDS.read_text(encoding="utf-8")

    require_all(
        "KeyboardView virtual-control integration",
        view_text,
        (
            "private val accessibilityDelegate = KeyboardAccessibilityDelegate(this)",
            "ViewCompat.setAccessibilityDelegate(this, accessibilityDelegate)",
            "override fun dispatchHoverEvent(event: MotionEvent)",
            "accessibilityDelegate.dispatchHoverEvent(event)",
            "internal fun accessibilityTargets()",
            "internal fun accessibilityTarget(id: Int)",
            "internal fun performAccessibilityTarget(id: Int)",
            'Action.SHIFT -> "Shift"',
            'Action.BACKSPACE -> "Backspace"',
            '"Space, English (US)"',
            'Action.ENTER -> "Enter"',
            'Action.SETTINGS -> "Keyboard settings"',
            "label = hit.entry.accessibilityLabel",
            'label = "Suggestion ${hit.value}"',
            "accessibilityDelegate.invalidateVirtualRoot()",
        ),
    )

    require_all(
        "ExploreByTouchHelper delegate",
        delegate_text,
        (
            "class KeyboardAccessibilityDelegate(",
            ") : ExploreByTouchHelper(keyboardView)",
            "override fun getVirtualViewAt(x: Float, y: Float): Int",
            "override fun getVisibleVirtualViews(virtualViewIds: MutableList<Int>)",
            "override fun onPopulateNodeForVirtualView(",
            'node.className = "android.widget.Button"',
            "node.isClickable = true",
            "node.isSelected = target.selected",
            "AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK",
            "node.isLongClickable = true",
            'node.hintText = "Alternate characters available"',
            "AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_LONG_CLICK",
            '"Insert $value"',
            "KeyAlternates.forKey(target.label)",
            "emojiSearchKeyboardIsActive()",
            'target.label == "Clear emoji search" || target.label == "Close emoji search"',
            "override fun onPerformActionForVirtualView(",
            "AccessibilityNodeInfo.ACTION_LONG_CLICK",
            '"Alternate characters: ${alternates.joinToString(separator = ", ")}"',
            "keyboardView.listener?.onText(value)",
            "keyboardView.performAccessibilityTarget(virtualViewId)",
            "AccessibilityEvent.TYPE_VIEW_CLICKED",
        ),
    )

    require_all(
        "Android virtual-node runtime evidence",
        test_text,
        (
            "view.accessibilityNodeProvider",
            "createAccessibilityNodeInfo(q.id)",
            "AccessibilityNodeInfo.ACTION_CLICK",
            "longPressAlternatesAreDiscoverableAndActionableThroughNativeNodeActions",
            "AccessibilityNodeInfo.ACTION_LONG_CLICK",
            'it.label?.toString() == "Insert á"',
            '"Emoji-search query keys must not gain long-press alternate semantics"',
            '"Emoji-search query keys must not expose alternate custom actions"',
            "view.performAccessibilityTarget(hello.id)",
            'it.label == "Search emoji"',
            'it.label == "Clear emoji search"',
            'it.label == "Close emoji search"',
            'it.label == "Shift"',
            "utilityToolbarExposesOnlyImplementedActions",
            '"Toolbar must not expose a redundant close/hide Keyboard action"',
            "emojiExistsOnlyInTheToolbarOnLettersLayer",
        ),
    )

    require_all(
        "Android accessibility dependency",
        gradle_text,
        ('implementation("androidx.customview:customview:1.2.0")',),
    )

    for index in range(7):
        marker = f'<item name="accessibility_alternate_{index}" type="id" />'
        if marker not in action_ids_text:
            fail(f"custom alternate action IDs missing `{marker}`")

    for forbidden in (
        "InputConnection",
        "ClipboardManager",
        "getTextBeforeCursor",
        "getTextAfterCursor",
        "SharedPreferences",
        "HttpURLConnection",
        "java.net.",
    ):
        if forbidden in delegate_text:
            fail(f"accessibility delegate gained forbidden data authority `{forbidden}`")

    print(
        "Keyboard virtual accessibility boundary passed: custom-drawn keys, suggestions, emoji categories, "
        "local emoji-search results, and bounded local key alternates expose actionable native accessibility "
        "semantics without editor, clipboard, persistence, or network authority. Emoji-search query mode cannot "
        "gain alternate-commit authority. Representative TalkBack/Switch Access physical-device acceptance remains "
        "separate."
    )


if __name__ == "__main__":
    main()
