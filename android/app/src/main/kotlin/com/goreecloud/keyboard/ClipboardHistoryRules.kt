package com.goreecloud.keyboard

internal object ClipboardHistoryRules {
    const val MAX_ENTRIES = 24

    fun prune(
        entries: List<KeyboardClipboardEntry>,
        nowMillis: Long,
    ): List<KeyboardClipboardEntry> =
        entries
            .filter { entry ->
                entry.pinned || entry.expiresAtMillis == null || entry.expiresAtMillis > nowMillis
            }
            .sortedWith(
                compareByDescending<KeyboardClipboardEntry> { it.pinned }
                    .thenByDescending { it.createdAtMillis },
            )
            .take(MAX_ENTRIES)

    fun upsert(
        entries: List<KeyboardClipboardEntry>,
        newEntry: KeyboardClipboardEntry,
        nowMillis: Long,
    ): List<KeyboardClipboardEntry> {
        val existing = entries.firstOrNull { it.text == newEntry.text }
        val merged = if (existing == null) {
            newEntry
        } else {
            newEntry.copy(
                id = existing.id,
                pinned = existing.pinned || newEntry.pinned,
                expiresAtMillis =
                    if (existing.pinned || newEntry.pinned) null else newEntry.expiresAtMillis,
            )
        }
        return prune(
            entries.filterNot { it.id == merged.id || it.text == merged.text } + merged,
            nowMillis,
        )
    }

    fun togglePin(
        entries: List<KeyboardClipboardEntry>,
        id: String,
        nowMillis: Long,
        retentionMillis: Long,
    ): List<KeyboardClipboardEntry> =
        prune(
            entries.map { entry ->
                if (entry.id != id) {
                    entry
                } else if (entry.pinned) {
                    entry.copy(
                        pinned = false,
                        expiresAtMillis = nowMillis + retentionMillis,
                    )
                } else {
                    entry.copy(
                        pinned = true,
                        expiresAtMillis = null,
                    )
                }
            },
            nowMillis,
        )

    fun edit(
        entries: List<KeyboardClipboardEntry>,
        id: String,
        text: String,
        nowMillis: Long,
        retentionMillis: Long,
    ): List<KeyboardClipboardEntry> =
        prune(
            entries.map { entry ->
                if (entry.id != id) {
                    entry
                } else {
                    entry.copy(
                        text = text,
                        createdAtMillis = nowMillis,
                        expiresAtMillis =
                            if (entry.pinned) null else nowMillis + retentionMillis,
                    )
                }
            },
            nowMillis,
        )

    fun delete(entries: List<KeyboardClipboardEntry>, id: String): List<KeyboardClipboardEntry> =
        entries.filterNot { it.id == id }

    fun clearUnpinned(entries: List<KeyboardClipboardEntry>): List<KeyboardClipboardEntry> =
        entries.filter { it.pinned }
}
