package org.fosser.app.domain.model

/**
 * F-Droid application metadata, adapted from the real index-v2.json shape.
 * Source: https://f-droid.org/repo/index-v2.json
 * Image URLs are absolute https://f-droid.org/repo/... URLs built by the parser.
 */
data class FdroidApp(
    val packageName: String,
    val name: String,
    val summary: String?,
    val description: String?,
    val iconUrl: String?,
    val screenshots: List<String> = emptyList(),
    val developer: String?,
    val license: String?,
    val categories: List<String> = emptyList(),
    val versionName: String?,
    val versionCode: Long?,
    val fDroidUrl: String,
    val sourceUrl: String?,
    val issueTrackerUrl: String?,
    val websiteUrl: String?,
    val changelogUrl: String?,
    val donateUrl: String?,
    val lastUpdated: Long?,
    val added: Long?,
    /** Full permission names (e.g. android.permission.INTERNET) of the latest version. */
    val permissions: List<String> = emptyList(),
    /** APK size in bytes of the latest version, if known. */
    val apkSize: Long? = null,
) {
    val id: String get() = packageName
    val category: String? get() = categories.firstOrNull()
}
