package com.goreecloud.keyboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardImeComponentPolicyTest {
    @Test
    fun enabledListMatchesOnlyExactPackageComponent() {
        val enabled = listOf(
            "com.example.keyboard/.ImeService",
            "com.goreecloud.keyboard.dev.v23/com.goreecloud.keyboard.KeyboardService",
        ).joinToString(":")

        assertTrue(
            KeyboardImeComponentPolicy.enabledListContainsPackage(
                enabled,
                "com.goreecloud.keyboard.dev.v23",
            ),
        )
        assertFalse(
            KeyboardImeComponentPolicy.enabledListContainsPackage(
                enabled,
                "com.goreecloud.keyboard",
            ),
        )
        assertFalse(
            KeyboardImeComponentPolicy.enabledListContainsPackage(
                enabled,
                "com.goreecloud.keyboard.dev.v2",
            ),
        )
    }

    @Test
    fun enabledListRejectsSiblingVersionPrefixMatches() {
        val enabled =
            "com.goreecloud.keyboard.dev.v230/com.goreecloud.keyboard.KeyboardService"

        assertFalse(
            KeyboardImeComponentPolicy.enabledListContainsPackage(
                enabled,
                "com.goreecloud.keyboard.dev.v23",
            ),
        )
    }

    @Test
    fun defaultMethodRequiresExactPackageComponent() {
        val selected =
            "com.goreecloud.keyboard.dev.v23/com.goreecloud.keyboard.KeyboardService"

        assertTrue(
            KeyboardImeComponentPolicy.defaultMethodMatchesPackage(
                selected,
                "com.goreecloud.keyboard.dev.v23",
            ),
        )
        assertFalse(
            KeyboardImeComponentPolicy.defaultMethodMatchesPackage(
                selected,
                "com.goreecloud.keyboard",
            ),
        )
        assertFalse(
            KeyboardImeComponentPolicy.defaultMethodMatchesPackage(
                selected,
                "com.goreecloud.keyboard.dev.v2",
            ),
        )
    }

    @Test
    fun malformedOrEmptyComponentsFailClosed() {
        assertFalse(KeyboardImeComponentPolicy.enabledListContainsPackage(null, "com.goreecloud.keyboard"))
        assertFalse(KeyboardImeComponentPolicy.enabledListContainsPackage("not-a-component", "com.goreecloud.keyboard"))
        assertFalse(KeyboardImeComponentPolicy.defaultMethodMatchesPackage("", "com.goreecloud.keyboard"))
        assertFalse(KeyboardImeComponentPolicy.defaultMethodMatchesPackage("/.ImeService", "com.goreecloud.keyboard"))
    }
}
