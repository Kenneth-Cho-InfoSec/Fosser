package org.fosser.app.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.fosser.app.data.repository.FdroidRepository
import org.fosser.app.domain.model.FdroidApp
import org.fosser.app.domain.model.HistoryAction

data class DetailsUiState(
    val app: FdroidApp? = null,
    val isSaved: Boolean = false,
)

class DetailsViewModel(private val repository: FdroidRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState = _uiState.asStateFlow()

    fun load(packageName: String) = viewModelScope.launch {
        launch {
            repository.observeApp(packageName).collect { app ->
                _uiState.update { it.copy(app = app) }
            }
        }
        launch {
            repository.observeIsSaved(packageName).collect { saved ->
                _uiState.update { it.copy(isSaved = saved) }
            }
        }
    }

    fun toggleSave() = viewModelScope.launch {
        val app = _uiState.value.app ?: return@launch
        if (_uiState.value.isSaved) repository.unsave(app.packageName)
        else repository.recordAction(app.packageName, HistoryAction.LIKE)
    }

    fun markOpened() = viewModelScope.launch {
        val app = _uiState.value.app ?: return@launch
        repository.recordAction(app.packageName, HistoryAction.OPEN)
    }
}
