package org.fosser.app.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.fosser.app.data.repository.FdroidRepository
import org.fosser.app.data.seed.SeedApps
import org.fosser.app.domain.model.FdroidApp
import org.fosser.app.domain.model.HistoryAction
import org.fosser.app.domain.model.SwipeAction

data class HomeUiState(
    val apps: List<FdroidApp> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val isOffline: Boolean = false,
    val lastOpenRequest: String? = null, // fDroidUrl to open via ACTION_VIEW
    val webViewFallbackUrl: String? = null, // open in-app when the browser is unavailable
    val openError: String? = null,
)

class HomeViewModel(
    private val repository: FdroidRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    // Stable-deck bookkeeping (fixes the card-swap bug): the visible deck is a
    // snapshot that only changes on user actions. Background syncs write to the
    // database but never reorder the active deck mid-session.
    private val knownPackages = mutableSetOf<String>()
    private var sessionSwiped = 0
    private var deckIsSeed = false
    private var toppingUp = false

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        try {
            repository.ensureSeeded()
            deckIsSeed = repository.catalogCount() <= SeedApps.apps.size
            adoptDeck(repository.currentDeck())
            _uiState.update { it.copy(isLoading = false) }
            // Refresh from network in background; keep cached deck on failure.
            refreshCatalog(quiet = true)
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    /** Replace the visible deck with a fresh snapshot, remembering every card shown. */
    private fun adoptDeck(fresh: List<FdroidApp>) {
        knownPackages.addAll(fresh.map { it.packageName })
        _uiState.update { it.copy(apps = fresh) }
    }

    fun refreshCatalog(quiet: Boolean = false) = viewModelScope.launch {
        if (!quiet) _uiState.update { it.copy(isRefreshing = true, error = null) }
        when (val result = repository.refreshCatalog()) {
            is FdroidRepository.RefreshResult.Updated -> {
                _uiState.update { it.copy(isRefreshing = false, isOffline = false, error = null) }
                if (!quiet) {
                    // Explicit refresh: user asked for new cards, restart the deck.
                    // History still excludes everything already swiped.
                    knownPackages.clear()
                    sessionSwiped = 0
                    deckIsSeed = false
                    adoptDeck(repository.currentDeck())
                } else if (sessionSwiped == 0 && deckIsSeed) {
                    // First-launch bootstrap: swap the tiny seed deck for the real
                    // catalog, but only while the user hasn't engaged with it yet.
                    deckIsSeed = false
                    adoptDeck(repository.currentDeck())
                }
            }
            is FdroidRepository.RefreshResult.Failed -> {
                val hasCache = result.cachedCount > 0
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        isOffline = hasCache,
                        error = if (hasCache) null else result.message,
                    )
                }
            }
        }
    }

    fun onSwipeLeft(app: FdroidApp) = act(app, SwipeAction.PASS)
    fun onSwipeRight(app: FdroidApp) = act(app, SwipeAction.LIKE)
    fun onSwipeUp(app: FdroidApp) = act(app, SwipeAction.OPEN)

    fun onLikeClicked(app: FdroidApp) = act(app, SwipeAction.LIKE)
    fun onPassClicked(app: FdroidApp) = act(app, SwipeAction.PASS)
    fun onOpenClicked(app: FdroidApp) = act(app, SwipeAction.OPEN)

    fun onCardClicked(app: FdroidApp, onNavigate: (FdroidApp) -> Unit) {
        onNavigate(app)
    }

    private fun act(app: FdroidApp, action: SwipeAction) = viewModelScope.launch {
        // Remove optimistically so the next card activates instantly.
        sessionSwiped++
        knownPackages.add(app.packageName)
        _uiState.update { s -> s.copy(apps = s.apps.filterNot { it.packageName == app.packageName }) }
        when (action) {
            SwipeAction.PASS -> repository.recordAction(app.packageName, HistoryAction.PASS)
            SwipeAction.LIKE -> repository.recordAction(app.packageName, HistoryAction.LIKE)
            SwipeAction.OPEN -> {
                repository.recordAction(app.packageName, HistoryAction.OPEN)
                _uiState.update { it.copy(lastOpenRequest = app.fDroidUrl) }
            }
        }
        // Top up the bottom of the deck when running low; the visible top is untouched.
        if (_uiState.value.apps.size < TOP_UP_THRESHOLD && !toppingUp) {
            toppingUp = true
            try {
                val more = repository.currentDeck(limit = TOP_UP_FETCH)
                    .filter { it.packageName !in knownPackages }
                    .take(TOP_UP_BATCH)
                if (more.isNotEmpty()) {
                    knownPackages.addAll(more.map { it.packageName })
                    _uiState.update { s -> s.copy(apps = more + s.apps) }
                }
            } finally {
                toppingUp = false
            }
        }
    }

    fun consumeOpenRequest() {
        _uiState.update { it.copy(lastOpenRequest = null) }
    }

    /**
     * Fallback chain step 2: the browser is missing or refused the URL,
     * so ask the UI to open it in the in-app WebView instead.
     */
    fun fallbackToWebView(url: String) {
        _uiState.update { it.copy(lastOpenRequest = null, webViewFallbackUrl = url) }
    }

    fun consumeWebViewFallback() {
        _uiState.update { it.copy(webViewFallbackUrl = null) }
    }

    fun openUrlFailed(url: String) {
        _uiState.update { it.copy(openError = "Could not open $url", lastOpenRequest = null) }
    }

    fun consumeOpenError() {
        _uiState.update { it.copy(openError = null) }
    }

    companion object {
        private const val TOP_UP_THRESHOLD = 10
        private const val TOP_UP_BATCH = 40
        private const val TOP_UP_FETCH = 200

        /** Builds the browser intent for a URL. */
        fun browserIntent(url: String): Intent =
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addCategory(Intent.CATEGORY_BROWSABLE)

        fun canOpen(context: Context, url: String): Boolean {
            return try {
                browserIntent(url).resolveActivity(context.packageManager) != null
            } catch (_: Exception) {
                false
            }
        }
    }
}
