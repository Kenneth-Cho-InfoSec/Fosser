package org.fosser.app.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.fosser.app.data.repository.FdroidRepository

class SavedViewModel(private val repository: FdroidRepository) : ViewModel() {
    val savedApps = repository.observeSavedApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun remove(packageName: String) = viewModelScope.launch {
        repository.unsave(packageName)
    }
}
