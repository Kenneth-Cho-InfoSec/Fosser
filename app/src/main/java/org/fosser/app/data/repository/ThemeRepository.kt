package org.fosser.app.data.repository

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.fosser.app.domain.model.AppAccent
import org.fosser.app.domain.model.ThemeMode
import org.fosser.app.domain.model.ThemeSettings

/** Minimal key-value abstraction so the repository is unit-testable without Android. */
interface KeyValueStore {
    fun getInt(key: String, def: Int): Int
    fun putInt(key: String, value: Int)
    fun getBoolean(key: String, def: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)
}

class SharedPrefsStore(private val prefs: SharedPreferences) : KeyValueStore {
    override fun getInt(key: String, def: Int): Int = prefs.getInt(key, def)
    override fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }
    override fun getBoolean(key: String, def: Boolean): Boolean = prefs.getBoolean(key, def)
    override fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
}

/** In-memory store for tests and previews. */
class InMemoryStore : KeyValueStore {
    private val map = mutableMapOf<String, Any>()
    override fun getInt(key: String, def: Int): Int = map[key] as? Int ?: def
    override fun putInt(key: String, value: Int) { map[key] = value }
    override fun getBoolean(key: String, def: Boolean): Boolean = map[key] as? Boolean ?: def
    override fun putBoolean(key: String, value: Boolean) { map[key] = value }
}

/**
 * Persists theme settings (Solipsism's "settings" prefs equivalent) and
 * exposes them as a StateFlow so Compose recomposes on change — no activity
 * recreation needed, unlike the View-based original.
 */
class ThemeRepository(private val store: KeyValueStore) {

    private val _settings = MutableStateFlow(read())
    val settings: StateFlow<ThemeSettings> = _settings.asStateFlow()

    fun setMode(mode: ThemeMode) {
        store.putInt(KEY_MODE, mode.ordinal)
        _settings.update { it.copy(mode = mode) }
    }

    fun setAccent(accent: AppAccent) {
        // Picking a swatch explicitly leaves system matching, like Solipsism.
        store.putInt(KEY_ACCENT, accent.ordinal)
        store.putBoolean(KEY_MATCH_SYSTEM, false)
        _settings.update { it.copy(accent = accent, matchSystemAccent = false) }
    }

    fun setMatchSystemAccent(match: Boolean) {
        store.putBoolean(KEY_MATCH_SYSTEM, match)
        _settings.update { it.copy(matchSystemAccent = match) }
    }

    private fun read(): ThemeSettings = ThemeSettings(
        mode = ThemeMode.fromOrdinal(store.getInt(KEY_MODE, ThemeMode.SYSTEM.ordinal)),
        accent = AppAccent.fromOrdinal(store.getInt(KEY_ACCENT, AppAccent.TEAL.ordinal)),
        matchSystemAccent = store.getBoolean(KEY_MATCH_SYSTEM, true),
    )

    companion object {
        const val PREFS_NAME = "fosser_settings"
        const val KEY_MODE = "theme_mode"
        const val KEY_ACCENT = "accent"
        const val KEY_MATCH_SYSTEM = "match_system_accent"
    }
}
