package org.fosser.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

/**
 * Donation prompt shown on the 5th, 20th and 100th app open
 * (or forced via the fosser_force_donate launch extra for testing).
 */
@Composable
fun DonateDialog(
    onDonate: () -> Unit,
    onLater: () -> Unit,
    onNever: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onLater,
        title = { Text("Support Fosser", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Fosser is free, open-source and has no ads or tracking. " +
                        "If it helped you discover great apps, please consider a small donation " +
                        "to keep development going.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDonate) { Text("Donate") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onNever) { Text("Don't ask again") }
                TextButton(onClick = onLater) { Text("Later") }
            }
        },
    )
}
