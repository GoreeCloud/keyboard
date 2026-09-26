package com.goreecloud.keyboard

internal enum class ClipboardAppPolicy {
    ALLOW,
    ASK,
    PASTE_ONLY,
    BLOCK,
}

internal enum class ClipboardRetention(val durationMillis: Long) {
    TEN_MINUTES(10L * 60L * 1_000L),
    ONE_HOUR(60L * 60L * 1_000L),
    ONE_DAY(24L * 60L * 60L * 1_000L),
}

internal data class KeyboardClipboardEntry(
    val id: String,
    val text: String,
    val createdAtMillis: Long,
    val expiresAtMillis: Long?,
    val pinned: Boolean,
    val sensitive: Boolean,
    val current: Boolean = false,
)

internal data class KeyboardClipboardSnapshot(
    val packageName: String?,
    val policy: ClipboardAppPolicy,
    val historyEnabled: Boolean,
    val retention: ClipboardRetention,
    val entries: List<KeyboardClipboardEntry>,
    val blockedReason: String? = null,
    val requiresAuthorization: Boolean = false,
)
