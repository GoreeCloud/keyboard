package com.goreecloud.keyboard

import android.text.InputType
import android.view.inputmethod.EditorInfo
import org.junit.Assert.*
import org.junit.Test

class KeyboardPolicyTest {
    @Test fun passwordFieldsAreSensitive() {
        val types = listOf(
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        )
        types.forEach { assertTrue(KeyboardPolicy.forEditor(it, 0).sensitive) }
    }

    @Test fun optOutOfSuggestionsIsRespected() {
        val p = KeyboardPolicy.forEditor(
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS, 0
        )
        assertTrue(p.sensitive)
    }

    @Test fun numberAndPhoneFieldsUseAppropriateLayout() {
        assertEquals(KeyboardKind.PHONE, KeyboardPolicy.forEditor(InputType.TYPE_CLASS_PHONE, 0).kind)
        assertEquals(KeyboardKind.NUMBERS, KeyboardPolicy.forEditor(InputType.TYPE_CLASS_NUMBER, 0).kind)
    }

    @Test fun emailAndUrlLayoutsAreDistinct() {
        assertEquals(KeyboardKind.EMAIL, KeyboardPolicy.forEditor(
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, 0
        ).kind)
        assertEquals(KeyboardKind.URL, KeyboardPolicy.forEditor(
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI, 0
        ).kind)
    }

    @Test fun editorActionIsMasked() {
        assertEquals(EditorInfo.IME_ACTION_SEARCH, KeyboardPolicy.forEditor(
            InputType.TYPE_CLASS_TEXT, EditorInfo.IME_ACTION_SEARCH or EditorInfo.IME_FLAG_NO_ENTER_ACTION
        ).action)
    }
}
