package com.goreecloud.keyboard

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.goreecloud.ui.material3.GoreeCloudExpressiveTheme

/**
 * Actual Android IME; works offline, does not log keystrokes or inspect
 * clipboard. All text goes directly through the active editor InputConnection.
 */
class GoreeKeyboardService : InputMethodService(), LifecycleOwner,
    ViewModelStoreOwner, SavedStateRegistryOwner {

    private val stateController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = stateController.savedStateRegistry

    private lateinit var preferences: KeyboardPreferences
    private var editorContext by mutableStateOf(KeyboardContext(KeyboardKind.LETTERS, false, EditorInfo.IME_ACTION_NONE))
    private var editorGeneration by mutableIntStateOf(0)
    private var inputView: ComposeView? = null

    override fun onCreate() {
        super.onCreate()
        preferences = KeyboardPreferences(this)
        stateController.performAttach()
        stateController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    override fun onCreateInputView(): View {
        return ComposeView(this).apply {
            isFocusable = false
            isFocusableInTouchMode = false
            setViewTreeLifecycleOwner(this@GoreeKeyboardService)
            setViewTreeViewModelStoreOwner(this@GoreeKeyboardService)
            setViewTreeSavedStateRegistryOwner(this@GoreeKeyboardService)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                // A new editor resets shift and symbol modes without retaining typed text.
                key(editorGeneration) {
                    GoreeCloudExpressiveTheme(accent = preferences.accent) {
                        KeyboardSurface(
                            context = editorContext,
                            onKey = ::handleKey,
                            onSettings = ::openSettings
                        )
                    }
                }
            }
            inputView = this
        }
    }

    override fun onStartInput(attribute: EditorInfo, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        editorContext = KeyboardPolicy.forEditor(attribute.inputType, attribute.imeOptions)
        editorGeneration += 1
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        super.onFinishInputView(finishingInput)
    }

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        inputView?.disposeComposition()
        inputView = null
        store.clear()
        super.onDestroy()
    }

    private fun handleKey(action: KeyAction) {
        val connection = currentInputConnection ?: return
        if (preferences.haptics) inputView?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        when (action) {
            is KeyAction.Text -> connection.commitText(action.value, 1)
            KeyAction.Space -> connection.commitText(" ", 1)
            KeyAction.Backspace -> {
                // Code-point aware, so supplementary emoji are not split into invalid surrogates.
                if (!connection.deleteSurroundingTextInCodePoints(1, 0)) {
                    connection.deleteSurroundingText(1, 0)
                }
            }
            KeyAction.Enter -> {
                val actionId = editorContext.action
                if (actionId != EditorInfo.IME_ACTION_NONE &&
                    actionId != EditorInfo.IME_ACTION_UNSPECIFIED &&
                    currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_FLAG_NO_ENTER_ACTION) == 0
                ) {
                    if (!connection.performEditorAction(actionId)) connection.commitText("\n", 1)
                } else connection.commitText("\n", 1)
            }
            KeyAction.SwitchKeyboard -> switchToNextInputMethod(false)
        }
    }

    private fun openSettings() {
        startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
