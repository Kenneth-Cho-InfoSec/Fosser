package org.fosser.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.fosser.app.data.repository.FdroidRepository

class HistoryViewModel(private val repository: FdroidRepository) : ViewModel() {
    val history = repository.observeHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun reset() = viewModelScope.launch {
        repository.resetHistory()
    }
}
