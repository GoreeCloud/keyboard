package com.goreecloud.keyboard

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PackagedEnglishDictionaryRuntimeTest {
    @Test
    fun packagedDictionaryContainsBroadCommonEnglishCoverage() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val words = PackagedEnglishDictionary(context).words
        val normalized = words.asSequence().map { it.lowercase() }.toHashSet()

        assertTrue("Expanded dictionary should contain more than forty thousand words", words.size > 40_000)
        assertTrue("Common swipe word jump must be present", "jump" in normalized)
        assertTrue("Common performance word lag must be present", "lag" in normalized)
        assertTrue("Common inflected word lagging must be present", "lagging" in normalized)
        assertTrue("Existing common word hill must remain present", "hill" in normalized)
        assertEquals(46_691, PackagedEnglishDictionary.EXPECTED_FALLBACK_WORDS)
        assertEquals(
            "FrequencyWords / OpenSubtitles 2018 English",
            PackagedEnglishDictionary.SOURCE_PROJECT,
        )

        val firstFrequencyWords = context.assets
            .open(PackagedEnglishDictionary.ASSET_PATH)
            .bufferedReader(Charsets.UTF_8)
            .useLines { lines -> lines.take(5).toList() }
        assertEquals(
            "Frequency-ranked fallback must preserve the upstream common-word order",
            listOf("you", "the", "to", "it", "and"),
            firstFrequencyWords,
        )
    }
}
