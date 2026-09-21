package org.fosser.app

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.fosser.app.data.repository.InMemoryStore
import org.fosser.app.data.repository.ThemeRepository
import org.fosser.app.domain.model.AppAccent
import org.fosser.app.domain.model.ThemeMode
import org.fosser.app.domain.model.ThemeSettings
import org.fosser.app.ui.theme.customScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeRepositoryTest {

    @Test
    fun `defaults are system theme with teal accent and system match on`() = runTest {
        val repo = ThemeRepository(InMemoryStore())
        assertEquals(
            ThemeSettings(ThemeMode.SYSTEM, AppAccent.TEAL, true),
            repo.settings.first(),
        )
    }

    @Test
    fun `setters persist and emit`() = runTest {
        val repo = ThemeRepository(InMemoryStore())
        repo.setMatchSystemAccent(true)
        repo.setMode(ThemeMode.BLACK)
        // Picking a swatch leaves system matching, like Solipsism.
        repo.setAccent(AppAccent.PURPLE)
        assertEquals(
            ThemeSettings(ThemeMode.BLACK, AppAccent.PURPLE, false),
            repo.settings.first(),
        )
        repo.setMatchSystemAccent(true)
        assertEquals(true, repo.settings.first().matchSystemAccent)
    }

    @Test
    fun `unknown ordinals fall back safely`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromOrdinal(99))
        assertEquals(AppAccent.TEAL, AppAccent.fromOrdinal(99))
    }

    @Test
    fun `system mode follows OS night flag`() {
        assertTrue(ThemeMode.SYSTEM.isDark(systemDark = true))
        assertFalse(ThemeMode.SYSTEM.isDark(systemDark = false))
        assertTrue(ThemeMode.BLACK.isDark(systemDark = false))
        assertFalse(ThemeMode.LIGHT.isDark(systemDark = true))
    }
}

class CustomSchemeTest {

    @Test
    fun `accent seed becomes primary`() {
        val seed = Color(0xFF7C3AED)
        assertEquals(seed, customScheme(seed, dark = true, black = false).primary)
        assertEquals(seed, customScheme(seed, dark = false, black = false).primary)
    }

    @Test
    fun `black mode uses true black surfaces`() {
        val scheme = customScheme(Color(0xFF0E7C6B), dark = true, black = true)
        assertEquals(Color.Black, scheme.background)
        assertEquals(Color.Black, scheme.surface)
    }

    @Test
    fun `dark mode keeps Fosser ink surfaces`() {
        val scheme = customScheme(Color(0xFF0E7C6B), dark = true, black = false)
        assertEquals(Color(0xFF10181A), scheme.background)
    }
}
