package org.fosser.app.ui.home

import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import org.fosser.app.domain.model.FdroidApp
import org.fosser.app.domain.model.toSwipeAction
import org.fosser.app.ui.components.AppCard
import org.fosser.app.ui.components.swipe.SwipingDirection
import org.fosser.app.ui.components.swipe.rememberSwipeableCardState
import org.fosser.app.ui.components.swipe.swipableCard
import org.fosser.app.ui.theme.FosserMist

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenDetails: (String) -> Unit,
    onOpenSaved: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenWebView: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var showMenu by remember { mutableStateOf(false) }

    // Open fallback chain:
    // 1) system browser via ACTION_VIEW -> 2) in-app WebView -> 3) error message.
    LaunchedEffect(state.lastOpenRequest) {
        val url = state.lastOpenRequest ?: return@LaunchedEffect
        var opened = false
        if (HomeViewModel.canOpen(context, url)) {
            try {
                context.startActivity(HomeViewModel.browserIntent(url))
                opened = true
            } catch (_: Exception) {
                opened = false
            }
        }
        if (opened) {
            viewModel.consumeOpenRequest()
        } else {
            viewModel.fallbackToWebView(url)
        }
    }
    LaunchedEffect(state.webViewFallbackUrl) {
        val url = state.webViewFallbackUrl ?: return@LaunchedEffect
        onOpenWebView(url)
        viewModel.consumeWebViewFallback()
    }
    LaunchedEffect(state.openError) {
        if (state.openError != null) {
            snackbar.showSnackbar(state.openError!!)
            viewModel.consumeOpenError()
        }
    }
    LaunchedEffect(state.isOffline) {
        if (state.isOffline) snackbar.showSnackbar("You're offline. Showing apps already downloaded to Fosser.")
    }

    // Warm the image cache for cards about to promote: without this the incoming
    // card composes with cold network loads and visibly pops in ~0.5s later.
    // Runs at most once per deck change (the deck is a snapshot, not a live flow).
    LaunchedEffect(state.apps) {
        val upcoming = state.apps.takeLast(7).dropLast(4)
        if (upcoming.isEmpty()) return@LaunchedEffect
        try {
            val loader = SingletonImageLoader.get(context)
            upcoming.forEach { app ->
                app.iconUrl?.let { loader.enqueue(ImageRequest.Builder(context).data(it).build()) }
                app.screenshots.firstOrNull()?.let {
                    loader.enqueue(ImageRequest.Builder(context).data(it).build())
                }
            }
        } catch (_: Exception) {
            // Prefetch is best-effort; cards load normally on compose regardless.
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Fosser", fontWeight = FontWeight.Black)
                        Text("Discover open-source apps", style = MaterialTheme.typography.bodySmall, color = FosserMist.copy(alpha = 0.7f))
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.semantics { contentDescription = "More options" },
                    ) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Refresh F-Droid data") },
                            leadingIcon = { Icon(Icons.Filled.Refresh, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                viewModel.refreshCatalog()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Saved apps") },
                            leadingIcon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onOpenSaved()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("History") },
                            leadingIcon = { Icon(Icons.Filled.History, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onOpenHistory()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onOpenSettings()
                            },
                        )
                    }
                },
            )
        },
        bottomBar = {
            DeckControls(
                hasCards = state.apps.isNotEmpty(),
                onPass = { state.apps.lastOrNull()?.let { viewModel.onPassClicked(it) } },
                onLike = { state.apps.lastOrNull()?.let { viewModel.onLikeClicked(it) } },
                onOpen = { state.apps.lastOrNull()?.let { viewModel.onOpenClicked(it) } },
            )
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isLoading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("Loading open-source apps…", color = FosserMist.copy(alpha = 0.7f))
                    }
                }
                state.error != null && state.apps.isEmpty() -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error!!, color = FosserMist.copy(alpha = 0.8f))
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.refreshCatalog() }) { Text("Retry") }
                    }
                }
                state.apps.isEmpty() -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("You've explored everything for now.", color = FosserMist.copy(alpha = 0.8f))
                        Spacer(Modifier.height(8.dp))
                        FilledTonalButton(onClick = { viewModel.refreshCatalog() }) { Text("Refresh F-Droid data") }
                        Spacer(Modifier.height(8.dp))
                        FilledTonalButton(onClick = onOpenSaved) { Text("View saved apps") }
                    }
                }
                else -> {
                    CardStack(
                        apps = state.apps,
                        onSwiped = { app, direction ->
                            when (direction.toSwipeAction()) {
                                org.fosser.app.domain.model.SwipeAction.PASS -> viewModel.onSwipeLeft(app)
                                org.fosser.app.domain.model.SwipeAction.LIKE -> viewModel.onSwipeRight(app)
                                org.fosser.app.domain.model.SwipeAction.OPEN -> viewModel.onSwipeUp(app)
                                null -> Unit
                            }
                        },
                        onTap = { onOpenDetails(it.packageName) },
                    )
                }
            }
        }
    }
}

/**
 * Stacked deck: underneath cards partially visible (scale/offset),
 * top card is swipeable LEFT/RIGHT/UP. Next card activates after swipe
 * because ViewModel removes the swiped app from state.
 */
@Composable
private fun CardStack(
    apps: List<FdroidApp>,
    onSwiped: (FdroidApp, SwipingDirection) -> Unit,
    onTap: (FdroidApp) -> Unit,
) {
    val scope = rememberCoroutineScope()
    // Show last 4 as stack; last element is the top. The extra buried card keeps
    // its images warm so promoting it doesn't flash blank content.
    val visible = apps.takeLast(4)
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        visible.forEachIndexed { stackIndex, app ->
            // Keyed per app so promotion MOVES the card's composition instead of
            // swapping content by position. Every card measures identically
            // (fillMaxWidth x 600dp) so promotion never resizes anything; depth
            // is shown only by a vertical offset, which animates smoothly.
            // The offset is layout-neutral (not padding), so it can't relayout.
            key(app.packageName) {
                val isTop = stackIndex == visible.lastIndex
                val depth = (visible.lastIndex - stackIndex).toFloat()
                val offsetY by animateDpAsState(
                    targetValue = (depth * 12).dp,
                    label = "stackDepth",
                )
                if (isTop) {
                    val cardState = rememberSwipeableCardState()
                    AppCard(
                        app = app,
                        cardState = cardState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(600.dp)
                            .offset(y = offsetY)
                            .swipableCard(
                                state = cardState,
                                onSwiped = { onSwiped(app, it) },
                            ),
                        onTap = { onTap(app) },
                    )
                } else {
                    AppCard(
                        app = app,
                        cardState = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(600.dp)
                            .offset(y = offsetY),
                        onTap = { onTap(app) },
                    )
                }
            }
        }
    }
    // Keep coroutine scope referenced for future programmatic swipes from buttons.
    scope.hashCode()
}

@Composable
private fun DeckControls(
    hasCards: Boolean,
    onPass: () -> Unit,
    onLike: () -> Unit,
    onOpen: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth()
            .navigationBarsPadding()
            .padding(12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DeckButton(label = "Save", iconDescription = "Save", enabled = hasCards, onClick = onLike) {
                Icon(Icons.Filled.Favorite, contentDescription = "Save", modifier = Modifier.size(28.dp))
            }
            DeckButton(label = "Open", iconDescription = "Open F-Droid", enabled = hasCards, onClick = onOpen) {
                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Open F-Droid", modifier = Modifier.size(28.dp))
            }
            DeckButton(label = "Pass", iconDescription = "Pass", enabled = hasCards, onClick = onPass) {
                Icon(Icons.Filled.Close, contentDescription = "Pass", modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
private fun DeckButton(
    label: String,
    iconDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .size(56.dp)
                .semantics { contentDescription = iconDescription },
        ) { icon() }
        Text(label, style = MaterialTheme.typography.labelSmall, color = FosserMist.copy(alpha = 0.7f))
    }
}
