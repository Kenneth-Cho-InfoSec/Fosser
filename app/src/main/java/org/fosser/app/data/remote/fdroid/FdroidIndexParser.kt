package org.fosser.app.data.remote.fdroid

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Streaming parser for https://f-droid.org/repo/index-v2.json
 *
 * Shape (verified Sept 2026):
 * {
 *   "repo": {..., "webBaseUrl": "https://f-droid.org/packages", ...},
 *   "packages": {
 *     "<packageName>": {
 *       "metadata": {
 *         "added": Long, "categories": [...], "changelog"?: String,
 *         "issueTracker"?: String, "lastUpdated": Long, "license"?: String,
 *         "sourceCode"?: String, "webSite"?: String,
 *         "screenshots"?: {"phone": {"en-US": [{"name": "/pkg/en-US/phoneScreenshots/x.png"}]}},
 *         "authorName"?: String, "name": {"en-US": "..."},
 *         "summary": {"en-US": "..."}, "description": {"en-US": "..."},
 *         "donate"?: [...], "icon": {"en-US": {"name": "/pkg/en-US/icon_x.png"}},
 *         ...
 *       },
 *       "versions": {
 *         "<sha>": {"added": Long, "manifest": {"versionName": String, "versionCode": Long, ...}, ...}
 *       }
 *     }, ...
 *   }
 * }
 *
 * Streams package-by-package so the 60MB index never sits fully in memory.
 * Uses Gson streaming (works on JVM unit tests and Android).
 */
object FdroidIndexParser {

    data class ParsedApp(
        val packageName: String,
        val name: String?,
        val summary: String?,
        val description: String?,
        val iconPath: String?,
        val screenshotPaths: List<String>,
        val developer: String?,
        val license: String?,
        val categories: List<String>,
        val versionName: String?,
        val versionCode: Long?,
        val sourceUrl: String?,
        val issueTrackerUrl: String?,
        val websiteUrl: String?,
        val changelogUrl: String?,
        val donateUrl: String?,
        val lastUpdated: Long?,
        val added: Long?,
        val permissions: List<String> = emptyList(),
        val apkSize: Long? = null,
    )

    /** Latest-version info used for version name/code plus permissions and APK size. */
    private data class VersionInfo(
        val name: String?,
        val code: Long?,
        val added: Long?,
        val permissions: List<String>,
        val apkSize: Long?,
    )

    fun parse(input: InputStream, maxApps: Int = FdroidConstants.MAX_IMPORT_APPS): List<ParsedApp> {
        val out = ArrayList<ParsedApp>(512)
        val reader = JsonReader(InputStreamReader(input, Charsets.UTF_8))
        reader.isLenient = true
        try {
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "packages" -> {
                        reader.beginObject()
                        while (reader.hasNext() && out.size < maxApps) {
                            val packageName = reader.nextName()
                            val parsed = readPackage(reader, packageName)
                            if (parsed != null) out.add(parsed)
                        }
                        while (reader.hasNext()) {
                            reader.nextName()
                            reader.skipValue()
                        }
                        reader.endObject()
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
        } finally {
            try { reader.close() } catch (_: Exception) { }
        }
        return out
    }

    private fun readPackage(reader: JsonReader, packageName: String): ParsedApp? {
        var metadata: ParsedApp? = null
        var versionName: String? = null
        var versionCode: Long? = null
        var versionPermissions: List<String> = emptyList()
        var versionApkSize: Long? = null
        try {
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "metadata" -> metadata = readMetadata(reader, packageName)
                    "versions" -> {
                        val v = readLatestVersion(reader)
                        versionName = v?.name
                        versionCode = v?.code
                        versionPermissions = v?.permissions.orEmpty()
                        versionApkSize = v?.apkSize
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
        } catch (_: Exception) {
            try { reader.skipValue() } catch (_: Exception) { }
            return null
        }
        if (metadata == null) return null
        return metadata.copy(
            versionName = versionName,
            versionCode = versionCode,
            permissions = versionPermissions,
            apkSize = versionApkSize,
        )
    }

    private fun readMetadata(reader: JsonReader, packageName: String): ParsedApp {
        var name: String? = null
        var summary: String? = null
        var description: String? = null
        var iconPath: String? = null
        val screenshotPaths = mutableListOf<String>()
        var developer: String? = null
        var license: String? = null
        var categories: List<String> = emptyList()
        var sourceUrl: String? = null
        var issueTrackerUrl: String? = null
        var websiteUrl: String? = null
        var changelogUrl: String? = null
        var donateUrl: String? = null
        var lastUpdated: Long? = null
        var added: Long? = null

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "name" -> name = readLocalized(reader)
                "summary" -> summary = readLocalized(reader)
                "description" -> description = readLocalized(reader)
                "icon" -> iconPath = readIconName(reader)
                "screenshots" -> screenshotPaths.addAll(readScreenshots(reader))
                "authorName" -> developer = readNullableString(reader)
                "authorEmail" -> {
                    if (developer.isNullOrBlank()) {
                        val email = readNullableString(reader)
                        developer = email?.substringBefore('@')?.takeIf { it.isNotBlank() }
                    } else reader.skipValue()
                }
                "license" -> license = readNullableString(reader)
                "categories" -> categories = readStringArray(reader)
                "sourceCode" -> sourceUrl = readNullableString(reader)
                "issueTracker" -> issueTrackerUrl = readNullableString(reader)
                "webSite" -> websiteUrl = readNullableString(reader)
                "changelog" -> changelogUrl = readNullableString(reader)
                "donate" -> donateUrl = readDonate(reader)
                "lastUpdated" -> lastUpdated = readNullableLong(reader)
                "added" -> added = readNullableLong(reader)
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return ParsedApp(
            packageName = packageName,
            name = name, summary = summary, description = description,
            iconPath = iconPath, screenshotPaths = screenshotPaths,
            developer = developer, license = license, categories = categories,
            versionName = null, versionCode = null,
            sourceUrl = sourceUrl, issueTrackerUrl = issueTrackerUrl,
            websiteUrl = websiteUrl, changelogUrl = changelogUrl, donateUrl = donateUrl,
            lastUpdated = lastUpdated, added = added,
        )
    }

    private fun readLatestVersion(reader: JsonReader): VersionInfo? {
        var bestName: String? = null
        var bestCode: Long? = null
        var bestAdded: Long? = null
        var bestPermissions: List<String> = emptyList()
        var bestApkSize: Long? = null
        reader.beginObject()
        while (reader.hasNext()) {
            reader.nextName() // sha key
            var vName: String? = null
            var vCode: Long? = null
            var vAdded: Long? = null
            var vPermissions: List<String> = emptyList()
            var vApkSize: Long? = null
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "added" -> vAdded = readNullableLong(reader)
                    "file" -> {
                        reader.beginObject()
                        while (reader.hasNext()) {
                            when (reader.nextName()) {
                                "size" -> vApkSize = readNullableLong(reader)
                                else -> reader.skipValue()
                            }
                        }
                        reader.endObject()
                    }
                    "manifest" -> {
                        reader.beginObject()
                        while (reader.hasNext()) {
                            when (reader.nextName()) {
                                "versionName" -> vName = readNullableString(reader)
                                "versionCode" -> vCode = readNullableLong(reader)
                                "usesPermission" -> vPermissions = readPermissionNames(reader)
                                else -> reader.skipValue()
                            }
                        }
                        reader.endObject()
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            if (vCode != null && (bestCode == null || vCode > bestCode)) {
                bestCode = vCode; bestName = vName; bestAdded = vAdded
                bestPermissions = vPermissions; bestApkSize = vApkSize
            } else if (bestCode == null && vAdded != null && (bestAdded == null || vAdded > bestAdded)) {
                bestAdded = vAdded; bestName = vName
                bestPermissions = vPermissions; bestApkSize = vApkSize
            }
        }
        reader.endObject()
        if (bestName == null && bestCode == null) return null
        return VersionInfo(bestName, bestCode, bestAdded, bestPermissions, bestApkSize)
    }

    private fun readPermissionNames(reader: JsonReader): List<String> {
        val out = mutableListOf<String>()
        reader.beginArray()
        while (reader.hasNext()) {
            var name: String? = null
            if (reader.peek() == JsonToken.STRING) {
                name = readNullableString(reader)
            } else {
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "name" -> name = readNullableString(reader)
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
            }
            if (!name.isNullOrBlank()) out.add(name)
        }
        reader.endArray()
        return out.distinct()
    }

    /** {"en-US": "...", ...} -> prefer en-US, else first non-blank. */
    internal fun readLocalized(reader: JsonReader): String? {
        if (reader.peek() == JsonToken.STRING) return readNullableString(reader)
        var preferred: String? = null
        var first: String? = null
        reader.beginObject()
        while (reader.hasNext()) {
            val key = reader.nextName()
            val value = readNullableString(reader)
            if (!value.isNullOrBlank() && first == null) first = value
            if (key == "en-US" && !value.isNullOrBlank()) preferred = value
        }
        reader.endObject()
        return preferred ?: first
    }

    private fun readIconName(reader: JsonReader): String? {
        if (reader.peek() == JsonToken.STRING) return readNullableString(reader)
        var preferred: String? = null
        var first: String? = null
        reader.beginObject()
        while (reader.hasNext()) {
            reader.nextName() // locale
            var iconName: String? = null
            if (reader.peek() == JsonToken.STRING) {
                iconName = readNullableString(reader)
            } else {
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "name" -> iconName = readNullableString(reader)
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
            }
            if (!iconName.isNullOrBlank() && first == null) first = iconName
            if (preferred == null) preferred = iconName
        }
        reader.endObject()
        return preferred ?: first
    }

    private fun readScreenshots(reader: JsonReader): List<String> {
        val out = mutableListOf<String>()
        reader.beginObject()
        while (reader.hasNext()) {
            reader.nextName() // form factor
            if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                reader.beginObject()
                while (reader.hasNext()) {
                    reader.nextName() // locale
                    reader.beginArray()
                    while (reader.hasNext()) {
                        var shotName: String? = null
                        reader.beginObject()
                        while (reader.hasNext()) {
                            when (reader.nextName()) {
                                "name" -> shotName = readNullableString(reader)
                                else -> reader.skipValue()
                            }
                        }
                        reader.endObject()
                        if (!shotName.isNullOrBlank()) out.add(shotName)
                    }
                    reader.endArray()
                }
                reader.endObject()
            } else {
                reader.skipValue()
            }
        }
        reader.endObject()
        return out
    }

    private fun readDonate(reader: JsonReader): String? {
        return when (reader.peek()) {
            JsonToken.STRING -> readNullableString(reader)
            JsonToken.BEGIN_ARRAY -> {
                var first: String? = null
                reader.beginArray()
                while (reader.hasNext()) {
                    val v = readNullableString(reader)
                    if (first == null && !v.isNullOrBlank()) first = v
                }
                reader.endArray()
                first
            }
            else -> { reader.skipValue(); null }
        }
    }

    private fun readStringArray(reader: JsonReader): List<String> {
        val out = mutableListOf<String>()
        reader.beginArray()
        while (reader.hasNext()) {
            val v = readNullableString(reader)
            if (!v.isNullOrBlank()) out.add(v)
        }
        reader.endArray()
        return out
    }

    private fun readNullableString(reader: JsonReader): String? {
        return if (reader.peek() == JsonToken.NULL) { reader.nextNull(); null }
        else try { reader.nextString() } catch (_: Exception) { reader.skipValue(); null }
    }

    private fun readNullableLong(reader: JsonReader): Long? {
        return if (reader.peek() == JsonToken.NULL) { reader.nextNull(); null }
        else try { reader.nextLong() } catch (_: Exception) {
            try { reader.nextString().toLongOrNull() } catch (_: Exception) { reader.skipValue(); null }
        }
    }
}
