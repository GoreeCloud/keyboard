package com.goreecloud.keyboard

/**
 * First-party GoreeCloud vocabulary packaged with Keyboard.
 *
 * The product/system terms are derived from the canonical GoreeCloud branding catalog and remain
 * local, deterministic, read-only input data. This list is not learned from typing and is never
 * synchronized or transmitted.
 */
internal object GoreeCloudDictionary {
    const val BrandingCatalogBlob = "cda780a51f7bb5642ccd4de5c6383292245bf19c"

    val canonicalTerms = listOf(
        "GoreeCloud",
        "Everkeep",
        "Glaze",
        "Quill",
        "Wardveil",
        "Observability",
        "Mesh",
        "Policy",
        "Privacy",
        "Shield",
        "Security",
        "Backups",
        "Bookmarks",
        "Browser",
        "Calendar",
        "Care",
        "Changelogs",
        "Code",
        "Contacts",
        "DNS",
        "Documents",
        "Download",
        "Drive",
        "Feed",
        "Forms",
        "Gallery",
        "Gateway",
        "Health",
        "Home",
        "Identity",
        "Index",
        "Keyboard",
        "Launcher",
        "Location",
        "Mail",
        "Manager",
        "Maps",
        "Memos",
        "Messenger",
        "Monitor",
        "Music",
        "Network",
        "Notes",
        "Notify",
        "Office",
        "Photos",
        "Presentations",
        "Reader",
        "Router",
        "Search",
        "Since",
        "Social",
        "Spreadsheet",
        "Sync",
        "Tasks",
        "Terminal",
        "Vault",
        "Video",
        "Website",
        "Writer",
    )

    val commonContractions = listOf(
        "I'm", "I've", "I'll", "I'd",
        "don't", "doesn't", "didn't",
        "can't", "won't", "isn't", "aren't", "wasn't", "weren't",
        "haven't", "hasn't", "hadn't",
        "shouldn't", "wouldn't", "couldn't", "mustn't", "mightn't", "needn't",
        "you're", "you've", "you'll", "you'd",
        "they're", "they've", "they'll", "they'd",
        "we're", "we've", "we'll", "we'd",
        "he's", "he'll", "he'd",
        "she's", "she'll", "she'd",
        "it's", "it'll",
        "that's", "that'll", "there's", "there'll",
        "what's", "what'll", "where's", "when's", "why's", "who's",
        "could've", "would've", "should've", "might've", "must've",
        "let's",
    )

    /**
     * Common English compound spellings that Keyboard may surface as phrase-level suggestions.
     * They are suggestions rather than unconditional rewrites because many compounds are
     * hyphenated attributively but remain open when used predicatively.
     */
    val hyphenatedCompounds = listOf(
        "up-to-date",
        "built-in",
        "well-known",
        "high-quality",
        "long-term",
        "short-term",
        "real-time",
        "full-time",
        "part-time",
        "user-friendly",
        "privacy-focused",
        "open-source",
        "first-party",
        "third-party",
        "cross-platform",
        "on-device",
        "local-first",
        "self-hosted",
        "side-by-side",
        "end-to-end",
    )
}
