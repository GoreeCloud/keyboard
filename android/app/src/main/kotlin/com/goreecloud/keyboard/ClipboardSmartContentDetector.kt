package com.goreecloud.keyboard

internal enum class ClipboardSmartContentType(val label: String) {
    PHONE("Phone"),
    EMAIL("Email"),
    WEB_LINK("Link"),
    ADDRESS("Address"),
    DATE("Date"),
    TIME("Time"),
}

internal data class ClipboardSmartContent(
    val type: ClipboardSmartContentType,
    val value: String,
    val start: Int,
    val endExclusive: Int,
)

/**
 * Conservative, device-local extraction of reusable substrings from copied text.
 *
 * Detection does not persist derived data, call external services, geocode addresses, resolve
 * links, inspect contacts, or feed extracted values into language learning.
 */
internal object ClipboardSmartContentDetector {
    private data class PatternSpec(
        val type: ClipboardSmartContentType,
        val regex: Regex,
    )

    private val specs = listOf(
        PatternSpec(
            ClipboardSmartContentType.EMAIL,
            Regex("""\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}\b""", RegexOption.IGNORE_CASE),
        ),
        PatternSpec(
            ClipboardSmartContentType.WEB_LINK,
            Regex("""\b(?:https?://|www\.)[^\s<>()]+""", RegexOption.IGNORE_CASE),
        ),
        PatternSpec(
            ClipboardSmartContentType.PHONE,
            Regex("""(?<!\w)(?:\+?1[\s.-]?)?(?:\(?\d{3}\)?[\s.-]?)\d{3}[\s.-]\d{4}(?!\w)"""),
        ),
        PatternSpec(
            ClipboardSmartContentType.ADDRESS,
            Regex(
                """\b\d{1,6}\s+[A-Z0-9.'-]+(?:\s+[A-Z0-9.'-]+){0,5}\s+(?:Street|St|Road|Rd|Avenue|Ave|Boulevard|Blvd|Lane|Ln|Drive|Dr|Court|Ct|Parkway|Pkwy|Highway|Hwy|Way)\b""",
                RegexOption.IGNORE_CASE,
            ),
        ),
        PatternSpec(
            ClipboardSmartContentType.DATE,
            Regex(
                """\b(?:(?:Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?)\s+\d{1,2}(?:,\s*\d{4})?|\d{1,2}[/-]\d{1,2}[/-]\d{2,4})\b""",
                RegexOption.IGNORE_CASE,
            ),
        ),
        PatternSpec(
            ClipboardSmartContentType.TIME,
            Regex(
                """\b(?:(?:[01]?\d|2[0-3]):[0-5]\d(?:\s?[AP]M)?|(?:1[0-2]|0?[1-9])(?::[0-5]\d)?\s?[AP]M)\b""",
                RegexOption.IGNORE_CASE,
            ),
        ),
    )

    fun detect(text: String, limit: Int = 8): List<ClipboardSmartContent> {
        if (text.isBlank() || limit <= 0) return emptyList()

        val matches = specs.flatMap { spec ->
            spec.regex.findAll(text).mapNotNull { match ->
                val cleaned = match.value.trimEnd('.', ',', ';', ':', '!', '?', ')', ']')
                if (cleaned.isBlank()) {
                    null
                } else {
                    ClipboardSmartContent(
                        type = spec.type,
                        value = cleaned,
                        start = match.range.first,
                        endExclusive = match.range.first + cleaned.length,
                    )
                }
            }.toList()
        }

        val accepted = mutableListOf<ClipboardSmartContent>()
        matches
            .sortedWith(
                compareBy<ClipboardSmartContent> { it.start }
                    .thenByDescending { it.endExclusive - it.start }
                    .thenBy { it.type.ordinal },
            )
            .forEach { candidate ->
                if (accepted.size >= limit) return@forEach
                val overlaps = accepted.any { existing ->
                    candidate.start < existing.endExclusive &&
                        existing.start < candidate.endExclusive
                }
                val duplicate = accepted.any {
                    it.type == candidate.type && it.value.equals(candidate.value, ignoreCase = true)
                }
                if (!overlaps && !duplicate) accepted += candidate
            }

        return accepted.sortedBy { it.start }
    }
}
