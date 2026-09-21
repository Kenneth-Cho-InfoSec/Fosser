package org.fosser.app.ui.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.fosser.app.data.repository.DonationRepository
import org.fosser.app.domain.model.AppAccent
import org.fosser.app.domain.model.ThemeMode
import org.fosser.app.ui.theme.FosserMist
import org.fosser.app.util.Browser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val theme by viewModel.themeSettings.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showReset by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }
    var showAccentPicker by remember { mutableStateOf(false) }
    val systemAccentAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()

    LaunchedEffect(state.message) {
        if (state.message != null) {
            snackbar.showSnackbar(state.message!!)
            viewModel.consumeMessage()
        }
    }

    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            title = { Text("Reset your Fosser history?") },
            text = { Text("This will clear your local swipe history and recommendation data.") },
            confirmButton = {
                TextButton(onClick = { viewModel.resetHistory(); showReset = false }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showReset = false }) { Text("Cancel") }
            },
        )
    }

    if (showThemePicker) {
        AlertDialog(
            onDismissRequest = { showThemePicker = false },
            title = { Text("Theme") },
            text = {
                Column {
                    ThemeMode.entries.forEach { mode ->
                        Row(
                            Modifier.fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(mode)
                                    showThemePicker = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = theme.mode == mode,
                                onClick = {
                                    viewModel.setThemeMode(mode)
                                    showThemePicker = false
                                },
                            )
                            Text(mode.displayName(), modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemePicker = false }) { Text("Close") }
            },
        )
    }

    if (showAccentPicker) {
        AlertDialog(
            onDismissRequest = { showAccentPicker = false },
            title = { Text("Accent colour") },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.height(190.dp),
                ) {
                    items(AppAccent.entries) { accent ->
                        val selected = theme.accent == accent && !theme.matchSystemAccent
                        Box(
                            Modifier.padding(8.dp).size(64.dp)
                                .background(Color(accent.seedArgb), CircleShape)
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.outline,
                                    shape = CircleShape,
                                )
                                .clickable {
                                    viewModel.setAccent(accent)
                                    showAccentPicker = false
                                }
                                .semantics { contentDescription = accent.displayName() },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccentPicker = false }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.semantics { contentDescription = "Back" }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            SettingsRow(
                title = "Theme",
                summary = theme.mode.displayName(),
                onClick = { showThemePicker = true },
            )
            SettingsRow(
                title = "Accent colour",
                summary = if (theme.matchSystemAccent) "Match system accent"
                else theme.accent.displayName(),
                onClick = { showAccentPicker = true },
            )
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Match system accent", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        if (systemAccentAvailable) "Follow your wallpaper colours (Android 12+)"
                        else "Requires Android 12 or later",
                        style = MaterialTheme.typography.bodySmall,
                        color = FosserMist.copy(alpha = 0.7f),
                    )
                }
                Switch(
                    checked = theme.matchSystemAccent,
                    enabled = systemAccentAvailable,
                    onCheckedChange = { viewModel.setMatchSystemAccent(it) },
                    modifier = Modifier.semantics { contentDescription = "Match system accent" },
                )
            }

            Spacer(Modifier.height(8.dp))
            Text("Discovery", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Your discovery history is stored only on this device. No account, no tracking.", style = MaterialTheme.typography.bodySmall, color = FosserMist.copy(alpha = 0.7f))
            Text("Cached apps: ${state.catalogCount}", style = MaterialTheme.typography.bodyMedium)

            Button(
                onClick = { viewModel.refreshCatalog() },
                enabled = !state.working,
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Refresh F-Droid data" },
            ) { Text(if (state.working) "Refreshing…" else "Refresh F-Droid data") }

            OutlinedButton(
                onClick = { showReset = true },
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Reset swipe history" },
            ) { Text("Reset swipe history") }

            Spacer(Modifier.height(8.dp))
            Text("About Fosser", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Fosser is a privacy-friendly, open-source app discovery experience powered by F-Droid. " +
                    "Swipe left to pass, right to save, up to open in F-Droid. " +
                    "Data source: https://f-droid.org/repo/index-v2.json, cached locally with Room. " +
                    "No account required. No analytics or ads.",
                style = MaterialTheme.typography.bodySmall,
                color = FosserMist.copy(alpha = 0.8f),
            )
            Text("Open-source licenses: Fosser reuses the swipe-card interaction pattern from TinderCloneCompose (MIT-licensed base) with all dating, Firebase, chat and branding removed.", style = MaterialTheme.typography.bodySmall, color = FosserMist.copy(alpha = 0.7f))
            SettingsRow(
                title = "Developer",
                summary = "Kenneth-Cho-Infosec — tap to support via ko-fi",
                onClick = {
                    if (!Browser.open(context, DonationRepository.DONATE_URL)) {
                        uiScope.launch { snackbar.showSnackbar("Could not open the donation page.") }
                    }
                },
            )
            Text("Version 1.0 • org.fosser.app", style = MaterialTheme.typography.labelSmall, color = FosserMist.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun SettingsRow(title: String, summary: String, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium)
        Text(summary, style = MaterialTheme.typography.bodySmall, color = FosserMist.copy(alpha = 0.7f))
    }
}
