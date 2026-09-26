package com.goreecloud.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipboardHistoryRulesTest {
    @Test
    fun expiredUnpinnedItemsAreRemovedButPinnedItemsRemain() {
        val now = 10_000L
        val result = ClipboardHistoryRules.prune(
            listOf(
                entry("expired", expires = now - 1),
                entry("pinned", expires = now - 1, pinned = true),
                entry("active", expires = now + 1_000),
            ),
            now,
        )
        assertEquals(listOf("pinned", "active"), result.map { it.text })
    }

    @Test
    fun duplicateTextRefreshesWithoutCreatingAnotherEntry() {
        val now = 50_000L
        val original = entry("same", id = "old", created = 1_000L, expires = 2_000L)
        val refreshed = entry("same", id = "new", created = now, expires = now + 5_000L)
        val result = ClipboardHistoryRules.upsert(listOf(original), refreshed, now)
        assertEquals(1, result.size)
        assertEquals("old", result.single().id)
        assertEquals(now, result.single().createdAtMillis)
    }

    @Test
    fun pinRemovesExpirationAndUnpinRestoresIt() {
        val now = 100_000L
        val retention = 60_000L
        val initial = listOf(entry("clip", id = "x", expires = now + 1_000L))
        val pinned = ClipboardHistoryRules.togglePin(initial, "x", now, retention).single()
        assertTrue(pinned.pinned)
        assertEquals(null, pinned.expiresAtMillis)

        val unpinned =
            ClipboardHistoryRules.togglePin(listOf(pinned), "x", now, retention).single()
        assertFalse(unpinned.pinned)
        assertEquals(now + retention, unpinned.expiresAtMillis)
    }

    @Test
    fun editingPinnedClipPreservesPinAndNoExpiry() {
        val now = 200_000L
        val result = ClipboardHistoryRules.edit(
            entries = listOf(entry("old", id = "x", pinned = true, expires = null)),
            id = "x",
            text = "new",
            nowMillis = now,
            retentionMillis = 60_000L,
        ).single()

        assertEquals("new", result.text)
        assertTrue(result.pinned)
        assertEquals(null, result.expiresAtMillis)
        assertEquals(now, result.createdAtMillis)
    }

    @Test
    fun editingTemporaryClipRefreshesItsExpiry() {
        val now = 300_000L
        val retention = 90_000L
        val result = ClipboardHistoryRules.edit(
            entries = listOf(entry("old", id = "x", expires = 1L)),
            id = "x",
            text = "new",
            nowMillis = now,
            retentionMillis = retention,
        ).single()

        assertEquals("new", result.text)
        assertFalse(result.pinned)
        assertEquals(now + retention, result.expiresAtMillis)
    }

    @Test
    fun perAppAskPolicyIsAvailableAsAFirstClassState() {
        assertEquals("ASK", ClipboardAppPolicy.ASK.name)
    }

    @Test
    fun clearUnpinnedPreservesPinnedEntries() {
        val result = ClipboardHistoryRules.clearUnpinned(
            listOf(entry("a"), entry("b", pinned = true)),
        )
        assertEquals(listOf("b"), result.map { it.text })
    }

    private fun entry(
        text: String,
        id: String = text,
        created: Long = 1_000L,
        expires: Long? = 100_000L,
        pinned: Boolean = false,
    ) = KeyboardClipboardEntry(
        id = id,
        text = text,
        createdAtMillis = created,
        expiresAtMillis = expires,
        pinned = pinned,
        sensitive = false,
    )
}
