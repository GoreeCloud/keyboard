package com.goreecloud.keyboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build

/**
 * User-mediated Keyboard-side clipboard controller.
 *
 * This is not the future privileged GoreeCloud Secure Paste Broker. It controls what Keyboard
 * reads, stores, presents, pastes, expires from its own history, and clears after Paste Once. An
 * ordinary IME cannot revoke other applications' Android clipboard authority.
 */
internal class KeyboardClipboardController(
    context: Context,
    private val preferences: KeyboardClipboardPreferences,
    private val historyStore: EncryptedClipboardHistoryStore,
) {
    private val clipboardManager =
        context.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    private var activePackageName: String? = null
    private var sensitiveEditor = true
    private var inputViewVisible = false
    private var listenerRegistered = false
    private var currentEntry: KeyboardClipboardEntry? = null

    var onChanged: (() -> Unit)? = null

    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
        if (!inputViewVisible || !preferences.historyEnabled()) return@OnPrimaryClipChangedListener
        captureCurrentClip(userInitiated = false)
        onChanged?.invoke()
    }

    fun updateEditor(packageName: String?, sensitive: Boolean) {
        activePackageName = packageName
        sensitiveEditor = sensitive
        if (sensitive) currentEntry = null
        reconfigureListener()
    }

    fun onInputViewVisible() {
        inputViewVisible = true
        reconfigureListener()
    }

    fun onInputViewHidden() {
        inputViewVisible = false
        unregisterListener()
        currentEntry = null
    }

    fun closePanel() {
        if (currentEntry?.sensitive == true) currentEntry = null
    }

    fun openSnapshot(): KeyboardClipboardSnapshot {
        captureCurrentClip(userInitiated = true)
        return snapshot()
    }

    fun snapshot(nowMillis: Long = System.currentTimeMillis()): KeyboardClipboardSnapshot {
        val policy = effectivePolicy()
        val blockedReason = when {
            sensitiveEditor -> "Clipboard access is disabled in sensitive fields."
            policy == ClipboardAppPolicy.BLOCK ->
                "Clipboard access is blocked for this application."
            else -> null
        }
        if (blockedReason != null) {
            return KeyboardClipboardSnapshot(
                packageName = activePackageName,
                policy = policy,
                historyEnabled = preferences.historyEnabled(),
                retention = preferences.retention(),
                entries = emptyList(),
                blockedReason = blockedReason,
            )
        }

        val history =
            if (policy == ClipboardAppPolicy.ALLOW) historyStore.load(nowMillis) else emptyList()
        val current = currentEntry
        val matchingHistory =
            current?.let { item -> history.firstOrNull { it.text == item.text } }
        val displayCurrent = when {
            current == null -> null
            matchingHistory != null -> matchingHistory.copy(current = true)
            else -> current.copy(current = true)
        }

        return KeyboardClipboardSnapshot(
            packageName = activePackageName,
            policy = policy,
            historyEnabled = preferences.historyEnabled(),
            retention = preferences.retention(),
            entries = buildList {
                displayCurrent?.let(::add)
                history.filterNot { it.id == matchingHistory?.id }.forEach(::add)
            },
        )
    }

    fun setHistoryEnabled(enabled: Boolean) {
        preferences.setHistoryEnabled(enabled)
        if (!enabled) unregisterListener() else reconfigureListener()
        if (enabled) captureCurrentClip(userInitiated = true)
        onChanged?.invoke()
    }

    fun setCurrentAppPolicy(policy: ClipboardAppPolicy) {
        preferences.setPolicy(activePackageName, policy)
        if (policy == ClipboardAppPolicy.BLOCK) currentEntry = null
        reconfigureListener()
        onChanged?.invoke()
    }

    fun togglePin(id: String) {
        val now = System.currentTimeMillis()
        val retention = preferences.retention().durationMillis
        val entry = snapshot(now).entries.firstOrNull { it.id == id } ?: return
        if (entry.sensitive) return

        if (entry.id == SYSTEM_ENTRY_ID) {
            historyStore.upsert(entry.text, now, retention, pinned = true)
        } else {
            historyStore.togglePin(entry.id, now, retention)
        }
        onChanged?.invoke()
    }

    fun delete(id: String) {
        val entry = snapshot().entries.firstOrNull { it.id == id } ?: return
        historyStore.delete(entry.id)
        if (entry.current) {
            clearSystemClipboardIfMatching(entry.text)
            currentEntry = null
        }
        onChanged?.invoke()
    }

    fun clearUnpinned() {
        historyStore.clearUnpinned()
        onChanged?.invoke()
    }

    fun consume(id: String, pasteOnce: Boolean): String? {
        val entry = snapshot().entries.firstOrNull { it.id == id } ?: return null
        if (entry.sensitive && !pasteOnce) return null

        if (pasteOnce) {
            historyStore.delete(entry.id)
            if (entry.current) {
                clearSystemClipboardIfMatching(entry.text)
                currentEntry = null
            }
        }
        onChanged?.invoke()
        return entry.text
    }

    private fun captureCurrentClip(userInitiated: Boolean) {
        val policy = effectivePolicy()
        if (sensitiveEditor || policy == ClipboardAppPolicy.BLOCK) {
            currentEntry = null
            return
        }
        if (!userInitiated && !preferences.historyEnabled()) return

        val clip = clipboardManager.primaryClip ?: run {
            currentEntry = null
            return
        }
        if (clip.itemCount <= 0) {
            currentEntry = null
            return
        }

        // Deliberately accept direct text only. Never coerce a URI or launch a clipboard intent.
        val text = clip.getItemAt(0).text?.toString() ?: run {
            currentEntry = null
            return
        }
        if (text.isEmpty() || text.length > MAX_CURRENT_TEXT_CHARS) {
            currentEntry = null
            return
        }

        val sensitive =
            clip.description?.extras?.getBoolean(SENSITIVE_EXTRA, false) == true
        currentEntry = KeyboardClipboardEntry(
            id = SYSTEM_ENTRY_ID,
            text = text,
            createdAtMillis = System.currentTimeMillis(),
            expiresAtMillis = null,
            pinned = false,
            sensitive = sensitive,
            current = true,
        )

        if (
            preferences.historyEnabled() &&
            policy == ClipboardAppPolicy.ALLOW &&
            !sensitive &&
            text.length <= EncryptedClipboardHistoryStore.MAX_PERSISTED_TEXT_CHARS
        ) {
            historyStore.upsert(
                text = text,
                nowMillis = System.currentTimeMillis(),
                retentionMillis = preferences.retention().durationMillis,
            )
        }
    }

    private fun effectivePolicy(): ClipboardAppPolicy =
        if (sensitiveEditor) ClipboardAppPolicy.BLOCK else preferences.policyFor(activePackageName)

    private fun reconfigureListener() {
        val shouldListen =
            inputViewVisible &&
                !sensitiveEditor &&
                preferences.historyEnabled() &&
                effectivePolicy() == ClipboardAppPolicy.ALLOW
        if (shouldListen && !listenerRegistered) {
            clipboardManager.addPrimaryClipChangedListener(clipListener)
            listenerRegistered = true
        } else if (!shouldListen) {
            unregisterListener()
        }
    }

    private fun unregisterListener() {
        if (!listenerRegistered) return
        clipboardManager.removePrimaryClipChangedListener(clipListener)
        listenerRegistered = false
    }

    private fun clearSystemClipboardIfMatching(text: String) {
        val currentText = clipboardManager.primaryClip
            ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.text
            ?.toString()
        if (currentText != text) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            clipboardManager.clearPrimaryClip()
        } else {
            clipboardManager.setPrimaryClip(ClipData.newPlainText("", ""))
        }
    }

    private companion object {
        const val SYSTEM_ENTRY_ID = "__system_clipboard__"
        const val MAX_CURRENT_TEXT_CHARS = 131_072
        const val SENSITIVE_EXTRA = "android.content.extra.IS_SENSITIVE"
    }
}
