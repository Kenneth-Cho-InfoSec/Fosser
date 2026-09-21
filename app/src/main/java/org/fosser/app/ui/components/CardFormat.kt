package org.fosser.app.ui.components

import java.text.DateFormat
import java.util.Date
import java.util.Locale

/**
 * Pure card-formatting helpers (prototype rules, unit-tested):
 * - "Updated <date> · <apk size>" line
 * - first-50-words about excerpt when no screenshot exists
 * - short permission names (android.permission.INTERNET -> INTERNET)
 */
object CardFormat {

    fun formatApkSize(bytes: Long): String = when {
        bytes >= 1_048_576 -> "${"%.1f".format(Locale.US, bytes / 1_048_576.0)} MB"
        bytes >= 1_024 -> "${bytes / 1_024} KB"
        else -> "$bytes B"
    }

    fun formatUpdated(lastUpdated: Long?): String? {
        if (lastUpdated == null || lastUpdated <= 0) return null
        return DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US).format(Date(lastUpdated))
    }

    /** "Updated May 29, 2025 · 109.6 MB" — either half may be absent. Null if both are. */
    fun updatedLine(lastUpdated: Long?, apkSize: Long?): String? {
        val parts = listOfNotNull(
            formatUpdated(lastUpdated)?.let { "Updated $it" },
            apkSize?.takeIf { it > 0 }?.let { formatApkSize(it) },
        )
        return parts.joinToString(" · ").takeIf { it.isNotEmpty() }
    }

    /** First 50 words of the about text, used when the app has no screenshots. */
    fun excerpt50(description: String?): String {
        if (description.isNullOrBlank()) return ""
        val words = description.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.size <= 50) return words.joinToString(" ")
        return words.take(50).joinToString(" ") + "…"
    }

    fun shortPermissionName(fullName: String): String = fullName.substringAfterLast('.')
}
