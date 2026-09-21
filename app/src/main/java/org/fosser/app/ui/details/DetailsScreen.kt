package org.fosser.app.ui.details

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.fosser.app.ui.home.HomeViewModel
import org.fosser.app.ui.theme.FosserAmber
import org.fosser.app.ui.theme.FosserMist
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    packageName: String,
    viewModel: DetailsViewModel,
    onBack: () -> Unit,
    onOpenWebView: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(packageName) { viewModel.load(packageName) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.app?.name ?: "Details") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.semantics { contentDescription = "Back" }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleSave() },
                        modifier = Modifier.semantics { contentDescription = if (state.isSaved) "Remove from saved" else "Save app" },
                    ) {
                        Icon(
                            if (state.isSaved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (state.isSaved) "Remove from saved" else "Save app",
                        )
                    }
                },
            )
        },
    ) { padding ->
        val app = state.app
        if (app == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (app.iconUrl != null) {
                    AsyncImage(
                        model = app.iconUrl,
                        contentDescription = "Icon for ${app.name}",
                        modifier = Modifier.size(72.dp).clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.08f)),
                        contentScale = ContentScale.Fit,
                    )
                } else {
                    Box(
                        Modifier.size(72.dp).clip(RoundedCornerShape(18.dp)).background(FosserAmber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) { Text(app.name.take(1).uppercase(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = FosserAmber) }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(app.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("by ${app.developer ?: "Unknown developer"}", style = MaterialTheme.typography.bodyMedium, color = FosserMist.copy(alpha = 0.7f))
                    app.summary?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text("Open Source") })
                app.license?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                app.category?.let { AssistChip(onClick = {}, label = { Text(it) }) }
            }

            Button(
                onClick = {
                    viewModel.markOpened()
                    // Same fallback chain as swipe-up: browser -> in-app WebView -> message.
                    var opened = false
                    if (HomeViewModel.canOpen(context, app.fDroidUrl)) {
                        try {
                            context.startActivity(HomeViewModel.browserIntent(app.fDroidUrl))
                            opened = true
                        } catch (_: Exception) {
                            opened = false
                        }
                    }
                    if (!opened) onOpenWebView(app.fDroidUrl)
                },
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Open in F-Droid / Browser" },
            ) {
                Icon(Icons.Filled.OpenInNew, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Open in F-Droid / Browser")
            }
            OutlinedButton(
                onClick = { viewModel.toggleSave() },
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = if (state.isSaved) "Remove from saved" else "Save app" },
            ) { Text(if (state.isSaved) "Remove from saved" else "Save app") }

            app.description?.let {
                Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }

            if (app.screenshots.isNotEmpty()) {
                Text("Screenshots", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(app.screenshots) { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "Screenshot of ${app.name}",
                            modifier = Modifier.height(220.dp).clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }

            MetaRow("Version", app.versionName ?: "—")
            MetaRow("Package ID", app.packageName)
            MetaRow("License", app.license ?: "—")
            MetaRow("Categories", if (app.categories.isEmpty()) "—" else app.categories.joinToString(", "))
            MetaRow("Last updated", app.lastUpdated?.let { DateFormat.getDateInstance().format(Date(it)) } ?: "—")
            MetaRow("F-Droid link", app.fDroidUrl)
            MetaRow("Source code", app.sourceUrl ?: "—")
            MetaRow("Issue tracker", app.issueTrackerUrl ?: "—")
            MetaRow("Website", app.websiteUrl ?: "—")
        }
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = FosserMist.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
