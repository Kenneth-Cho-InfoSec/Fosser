package org.fosser.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.fosser.app.data.repository.FdroidRepository
import org.fosser.app.data.repository.ThemeRepository
import org.fosser.app.domain.model.AppAccent
import org.fosser.app.domain.model.ThemeMode

data class SettingsUiState(
    val catalogCount: Int = 0,
    val message: String? = null,
    val working: Boolean = false,
)

class SettingsViewModel(
    private val repository: FdroidRepository,
    private val themeRepository: ThemeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    val themeSettings = themeRepository.settings

    fun setThemeMode(mode: ThemeMode) = themeRepository.setMode(mode)
    fun setAccent(accent: AppAccent) = themeRepository.setAccent(accent)
    fun setMatchSystemAccent(match: Boolean) = themeRepository.setMatchSystemAccent(match)

    init { refreshCount() }

    fun refreshCount() = viewModelScope.launch {
        _uiState.update { it.copy(catalogCount = repository.catalogCount()) }
    }

    fun resetHistory() = viewModelScope.launch {
        repository.resetHistory()
        _uiState.update { it.copy(message = "History cleared. Discovery starts fresh.") }
    }

    fun refreshCatalog() = viewModelScope.launch {
        _uiState.update { it.copy(working = true, message = null) }
        when (val r = repository.refreshCatalog()) {
            is FdroidRepository.RefreshResult.Updated ->
                _uiState.update { it.copy(working = false, message = "F-Droid data refreshed (${r.count} apps).", catalogCount = r.count) }
            is FdroidRepository.RefreshResult.Failed ->
                _uiState.update { it.copy(working = false, message = r.message) }
        }
    }

    fun consumeMessage() = _uiState.update { it.copy(message = null) }
}
