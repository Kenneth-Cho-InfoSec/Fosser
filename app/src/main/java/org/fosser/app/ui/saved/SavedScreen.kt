package org.fosser.app.ui.saved

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.fosser.app.ui.theme.FosserMist

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(viewModel: SavedViewModel, onOpenDetails: (String) -> Unit, onBack: () -> Unit) {
    val apps by viewModel.savedApps.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.semantics { contentDescription = "Back" }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (apps.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No saved apps yet.", color = FosserMist.copy(alpha = 0.8f))
                Text("Swipe right on anything you like.", style = MaterialTheme.typography.bodySmall, color = FosserMist.copy(alpha = 0.6f))
            }
            return@Scaffold
        }
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            items(apps, key = { it.packageName }) { app ->
                Row(
                    Modifier.fillMaxWidth()
                        .clickable { onOpenDetails(app.packageName) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AsyncImage(
                        model = app.iconUrl,
                        contentDescription = "Icon for ${app.name}",
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(app.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(app.summary ?: "", style = MaterialTheme.typography.bodySmall, color = FosserMist.copy(alpha = 0.7f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(app.category ?: "", style = MaterialTheme.typography.labelSmall, color = FosserMist.copy(alpha = 0.6f))
                    }
                    IconButton(
                        onClick = { viewModel.remove(app.packageName) },
                        modifier = Modifier.semantics { contentDescription = "Remove ${app.name} from saved" },
                    ) { Icon(Icons.Filled.Delete, contentDescription = "Remove ${app.name} from saved") }
                }
            }
        }
    }
}
