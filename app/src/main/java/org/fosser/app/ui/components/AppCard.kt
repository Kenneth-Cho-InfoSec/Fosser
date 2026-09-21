package org.fosser.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.fosser.app.R
import org.fosser.app.domain.model.FdroidApp
import org.fosser.app.ui.components.swipe.SwipeableCardState
import org.fosser.app.ui.components.swipe.SwipingDirection
import org.fosser.app.ui.theme.FosserAmber
import org.fosser.app.ui.theme.FosserLike
import org.fosser.app.ui.theme.FosserMist
import org.fosser.app.ui.theme.FosserOpen
import org.fosser.app.ui.theme.FosserPass
import kotlin.math.abs

private val CARD_TEXTURES = listOf(
    R.drawable.card_tex_wave,
    R.drawable.card_tex_beach,
    R.drawable.card_tex_coast,
)

/**
 * Fosser app card, matching the approved HTML prototype:
 * icon top-left | name + summary + updated line top-right | three pills |
 * permissions bottom-left | first screenshot (or 50-word about excerpt) right.
 * One of the three bundled textures sits behind the content at low opacity.
 * Overlays PASS / LIKE / OPEN fade in with drag distance (driven by [cardState]).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppCard(
    app: FdroidApp,
    cardState: SwipeableCardState?,
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {},
) {
    // Stable texture per app (not re-randomized on every recomposition).
    val texture = remember(app.packageName) {
        CARD_TEXTURES[abs(app.packageName.hashCode()) % CARD_TEXTURES.size]
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "App card for ${app.name}" }
            .clickable(onClick = onTap),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            // Card texture, faint and dark so content stays readable.
            AsyncImage(
                model = texture,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.10f),
                contentScale = ContentScale.Crop,
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                    Color.Black.copy(alpha = 0.55f),
                    blendMode = androidx.compose.ui.graphics.BlendMode.Multiply,
                ),
            )
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(20.dp),
            ) {
                // Header: icon top-left, name + summary + updated top-right.
                Row(verticalAlignment = Alignment.Top) {
                    if (app.iconUrl != null) {
                        AsyncImage(
                            model = app.iconUrl,
                            contentDescription = "Icon for ${app.name}",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White.copy(alpha = 0.08f)),
                            contentScale = ContentScale.Fit,
                        )
                    } else {
                        Box(
                            Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(FosserAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = app.name.take(1).uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = FosserAmber,
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = app.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = FosserMist,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (!app.summary.isNullOrBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = app.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = FosserMist.copy(alpha = 0.8f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        CardFormat.updatedLine(app.lastUpdated, app.apkSize)?.let { line ->
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = line,
                                style = MaterialTheme.typography.labelSmall,
                                color = FosserMist.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                // Three pills.
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Chip("Open Source")
                    app.license?.let { Chip(it) }
                    app.category?.let { Chip(it) }
                }

                // Body: permissions bottom-left, screenshot/excerpt right.
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1.15f)) {
                        Text(
                            text = "Permissions",
                            style = MaterialTheme.typography.labelSmall,
                            color = FosserMist.copy(alpha = 0.6f),
                        )
                        Spacer(Modifier.height(6.dp))
                        val perms = app.permissions
                        if (perms.isEmpty()) {
                            Text(
                                text = "No permissions listed",
                                style = MaterialTheme.typography.bodySmall,
                                color = FosserMist.copy(alpha = 0.6f),
                            )
                        } else {
                            perms.take(5).forEach { perm ->
                                Text(
                                    text = CardFormat.shortPermissionName(perm),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                    ),
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = FosserMist.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .padding(bottom = 4.dp)
                                        .background(
                                            Color.Black.copy(alpha = 0.3f),
                                            RoundedCornerShape(8.dp),
                                        )
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                )
                            }
                            if (perms.size > 5) {
                                Text(
                                    text = "+${perms.size - 5} more",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FosserMist.copy(alpha = 0.6f),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Preview",
                            style = MaterialTheme.typography.labelSmall,
                            color = FosserMist.copy(alpha = 0.6f),
                        )
                        Spacer(Modifier.height(6.dp))
                        val shot = app.screenshots.firstOrNull()
                        if (shot != null) {
                            AsyncImage(
                                model = shot,
                                contentDescription = "Screenshot of ${app.name}",
                                modifier = Modifier
                                    .height(260.dp)
                                    .aspectRatio(9f / 20f)
                                    .align(Alignment.CenterHorizontally)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.4f)),
                                contentScale = ContentScale.Fit,
                            )
                        } else {
                            Box(
                                Modifier
                                    .height(260.dp)
                                    .aspectRatio(9f / 20f)
                                    .align(Alignment.CenterHorizontally)
                                    .border(
                                        1.dp,
                                        FosserMist.copy(alpha = 0.25f),
                                        RoundedCornerShape(12.dp),
                                    )
                                    .padding(10.dp),
                            ) {
                                Column {
                                    Text(
                                        text = "No screenshots — about preview",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = FosserMist.copy(alpha = 0.6f),
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = CardFormat.excerpt50(app.description),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontStyle = FontStyle.Italic,
                                        color = FosserMist.copy(alpha = 0.85f),
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "by ${app.developer ?: "Unknown developer"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = FosserMist.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    app.versionName?.let {
                        Text(
                            text = "v$it",
                            style = MaterialTheme.typography.bodySmall,
                            color = FosserMist.copy(alpha = 0.6f),
                        )
                    }
                }
            }

            // Drag overlays.
            if (cardState != null) {
                SwipeOverlay(
                    text = "PASS",
                    color = FosserPass,
                    alpha = cardState.overlayFraction(SwipingDirection.Left),
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp),
                    rotation = -18f,
                )
                SwipeOverlay(
                    text = "LIKE",
                    color = FosserLike,
                    alpha = cardState.overlayFraction(SwipingDirection.Right),
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
                    rotation = 18f,
                )
                SwipeOverlay(
                    text = "↑ OPEN F-DROID",
                    color = FosserOpen,
                    alpha = cardState.overlayFraction(SwipingDirection.Up),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 18.dp),
                    rotation = 0f,
                )
            }
        }
    }
}

@Composable
private fun Chip(text: String) {
    AssistChip(
        onClick = {},
        label = { Text(text, style = MaterialTheme.typography.labelSmall) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = Color.White.copy(alpha = 0.08f),
            labelColor = FosserMist,
        ),
    )
}

@Composable
private fun SwipeOverlay(
    text: String,
    color: Color,
    alpha: Float,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
) {
    if (alpha <= 0.01f) return
    Box(
        modifier = modifier
            .alpha(alpha.coerceIn(0f, 1f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .background(color.copy(alpha = 0.12f * alpha), CircleShape)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}
