package com.goreecloud.keyboard

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Encrypted device-local text clipboard history.
 *
 * Android backup is disabled for the application. Persisted clipboard text is encrypted with a
 * non-exportable Android Keystore AES-GCM key. Corrupt or undecryptable state fails closed.
 */
internal class EncryptedClipboardHistoryStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    @Synchronized
    fun load(nowMillis: Long = System.currentTimeMillis()): List<KeyboardClipboardEntry> {
        val encoded = preferences.getString(PAYLOAD, null) ?: return emptyList()
        val decoded = runCatching {
            deserialize(decrypt(Base64.decode(encoded, Base64.NO_WRAP)))
        }.getOrElse {
            preferences.edit().remove(PAYLOAD).apply()
            return emptyList()
        }
        val pruned = ClipboardHistoryRules.prune(decoded, nowMillis)
        if (pruned != decoded) save(pruned)
        return pruned
    }

    @Synchronized
    fun upsert(
        text: String,
        nowMillis: Long,
        retentionMillis: Long,
        pinned: Boolean = false,
    ): KeyboardClipboardEntry? {
        if (text.isBlank() || text.length > MAX_PERSISTED_TEXT_CHARS) return null
        val entry = KeyboardClipboardEntry(
            id = UUID.randomUUID().toString(),
            text = text,
            createdAtMillis = nowMillis,
            expiresAtMillis = if (pinned) null else nowMillis + retentionMillis,
            pinned = pinned,
            sensitive = false,
        )
        val updated = ClipboardHistoryRules.upsert(load(nowMillis), entry, nowMillis)
        save(updated)
        return updated.firstOrNull { it.text == text }
    }

    @Synchronized
    fun togglePin(id: String, nowMillis: Long, retentionMillis: Long) {
        save(ClipboardHistoryRules.togglePin(load(nowMillis), id, nowMillis, retentionMillis))
    }

    @Synchronized
    fun edit(
        id: String,
        text: String,
        nowMillis: Long,
        retentionMillis: Long,
    ): KeyboardClipboardEntry? {
        if (text.isBlank() || text.length > MAX_PERSISTED_TEXT_CHARS) return null
        val current = load(nowMillis)
        val existing = current.firstOrNull { it.id == id } ?: return null
        val updated = ClipboardHistoryRules.edit(
            entries = current,
            id = id,
            text = text,
            nowMillis = nowMillis,
            retentionMillis = retentionMillis,
        )
        save(updated)
        return updated.firstOrNull { it.id == existing.id }
    }

    @Synchronized
    fun delete(id: String) {
        save(ClipboardHistoryRules.delete(load(), id))
    }

    @Synchronized
    fun clearUnpinned() {
        save(ClipboardHistoryRules.clearUnpinned(load()))
    }

    @Synchronized
    fun clearAll() {
        preferences.edit().remove(PAYLOAD).apply()
    }

    @Synchronized
    private fun save(entries: List<KeyboardClipboardEntry>) {
        if (entries.isEmpty()) {
            preferences.edit().remove(PAYLOAD).apply()
            return
        }
        val ciphertext = encrypt(serialize(entries))
        preferences.edit()
            .putString(PAYLOAD, Base64.encodeToString(ciphertext, Base64.NO_WRAP))
            .apply()
    }

    private fun serialize(entries: List<KeyboardClipboardEntry>): ByteArray {
        val buffer = ByteArrayOutputStream()
        DataOutputStream(buffer).use { out ->
            out.writeInt(FORMAT_VERSION)
            out.writeInt(entries.size)
            entries.forEach { entry ->
                out.writeUTF(entry.id)
                out.writeLong(entry.createdAtMillis)
                out.writeLong(entry.expiresAtMillis ?: -1L)
                out.writeBoolean(entry.pinned)
                val bytes = entry.text.toByteArray(Charsets.UTF_8)
                out.writeInt(bytes.size)
                out.write(bytes)
            }
        }
        return buffer.toByteArray()
    }

    private fun deserialize(bytes: ByteArray): List<KeyboardClipboardEntry> {
        DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            require(input.readInt() == FORMAT_VERSION)
            val count = input.readInt()
            require(count in 0..ClipboardHistoryRules.MAX_ENTRIES)
            return buildList {
                repeat(count) {
                    val id = input.readUTF()
                    val created = input.readLong()
                    val expiresRaw = input.readLong()
                    val pinned = input.readBoolean()
                    val size = input.readInt()
                    require(size in 0..MAX_PERSISTED_TEXT_BYTES)
                    val textBytes = ByteArray(size)
                    input.readFully(textBytes)
                    add(
                        KeyboardClipboardEntry(
                            id = id,
                            text = String(textBytes, Charsets.UTF_8),
                            createdAtMillis = created,
                            expiresAtMillis = expiresRaw.takeIf { it >= 0L },
                            pinned = pinned,
                            sensitive = false,
                        ),
                    )
                }
            }
        }
    }

    private fun encrypt(plain: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey())
        val encrypted = cipher.doFinal(plain)
        val output = ByteArrayOutputStream()
        DataOutputStream(output).use { out ->
            out.writeInt(cipher.iv.size)
            out.write(cipher.iv)
            out.writeInt(encrypted.size)
            out.write(encrypted)
        }
        return output.toByteArray()
    }

    private fun decrypt(payload: ByteArray): ByteArray {
        DataInputStream(ByteArrayInputStream(payload)).use { input ->
            val ivSize = input.readInt()
            require(ivSize in 12..32)
            val iv = ByteArray(ivSize)
            input.readFully(iv)
            val cipherSize = input.readInt()
            require(cipherSize in 1..MAX_ENCRYPTED_BYTES)
            val encrypted = ByteArray(cipherSize)
            input.readFully(encrypted)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey(), GCMParameterSpec(128, iv))
            return cipher.doFinal(encrypted)
        }
    }

    private fun encryptionKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return generator.generateKey()
    }

    companion object {
        const val MAX_PERSISTED_TEXT_CHARS = 32_768
        private const val MAX_PERSISTED_TEXT_BYTES = 131_072
        private const val MAX_ENCRYPTED_BYTES = 4 * 1024 * 1024
        private const val FORMAT_VERSION = 1
        private const val PREFERENCES_NAME = "goreecloud_keyboard_clipboard_history"
        private const val PAYLOAD = "encrypted_history"
        private const val KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "goreecloud_keyboard_clipboard_history_aes"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
