package org.fosser.app

import org.fosser.app.ui.components.CardFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CardFormatTest {

    @Test
    fun `apk size formats to megabytes with one decimal`() {
        assertEquals("109.6 MB", CardFormat.formatApkSize(114920926))
        assertEquals("11.0 MB", CardFormat.formatApkSize(11507122))
    }

    @Test
    fun `apk size formats small values as KB and bytes`() {
        assertEquals("3 KB", CardFormat.formatApkSize(3956))
        assertEquals("512 B", CardFormat.formatApkSize(512))
    }

    @Test
    fun `updated line combines date and size`() {
        // 1748513413000 = May 29, 2025 (UTC)
        assertEquals(
            "Updated May 29, 2025 · 109.6 MB",
            CardFormat.updatedLine(1748513413000, 114920926),
        )
    }

    @Test
    fun `updated line tolerates missing halves`() {
        assertEquals("Updated May 29, 2025", CardFormat.updatedLine(1748513413000, null))
        assertEquals("11.0 MB", CardFormat.updatedLine(null, 11507122))
        assertNull(CardFormat.updatedLine(null, null))
        assertNull(CardFormat.updatedLine(0, 0))
    }

    @Test
    fun `excerpt keeps first fifty words with ellipsis`() {
        val words = (1..70).map { "word$it" }
        val result = CardFormat.excerpt50(words.joinToString(" "))
        assertEquals((1..50).joinToString(" ") { "word$it" } + "…", result)
    }

    @Test
    fun `excerpt returns short text untouched`() {
        assertEquals("just a few words", CardFormat.excerpt50("just a few words"))
        assertEquals("", CardFormat.excerpt50(null))
    }

    @Test
    fun `permission names shorten to last segment`() {
        assertEquals("INTERNET", CardFormat.shortPermissionName("android.permission.INTERNET"))
        assertEquals("RUN_COMMAND", CardFormat.shortPermissionName("com.termux.permission.RUN_COMMAND"))
    }
}
