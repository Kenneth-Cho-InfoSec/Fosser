package org.fosser.app.data.remote.fdroid

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.fosser.app.domain.model.FdroidApp
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Downloads https://f-droid.org/repo/index-v2.json with OkHttp streaming
 * and parses it incrementally. No Fosser backend required.
 */
class FdroidIndexFetcher(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(FdroidConstants.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(FdroidConstants.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build(),
) {
    sealed interface Result {
        data class Success(val apps: List<FdroidApp>) : Result
        data class Failure(val message: String, val cause: Throwable? = null) : Result
    }

    suspend fun fetch(): Result = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(FdroidConstants.INDEX_V2_URL).get().build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.Failure("F-Droid responded with HTTP ${response.code}")
                }
                val body = response.body ?: return@withContext Result.Failure("Empty response from F-Droid")
                val parsed = body.byteStream().use { stream ->
                    FdroidIndexParser.parse(stream)
                }
                val apps = parsed.mapNotNull { it.toDomain() }
                if (apps.isEmpty()) Result.Failure("F-Droid index contained no usable apps")
                else Result.Success(apps)
            }
        } catch (e: IOException) {
            Result.Failure("Could not reach F-Droid. Check your connection.", e)
        } catch (e: Exception) {
            Result.Failure("Could not read F-Droid data: ${e.message}", e)
        }
    }
}

fun FdroidIndexParser.ParsedApp.toDomain(): FdroidApp? {
    val cleanName = name?.trim().orEmpty()
    if (cleanName.isBlank()) return null
    fun abs(path: String?): String? {
        if (path.isNullOrBlank()) return null
        return if (path.startsWith("http")) path
        else FdroidConstants.REPO_BASE + (if (path.startsWith("/")) path else "/$path")
    }
    val shots = screenshotPaths.mapNotNull { abs(it) }.distinct().take(8)
    return FdroidApp(
        packageName = packageName,
        name = cleanName,
        summary = summary?.trim()?.takeIf { it.isNotBlank() },
        description = description?.trim()?.takeIf { it.isNotBlank() },
        iconUrl = abs(iconPath),
        screenshots = shots,
        developer = developer?.trim()?.takeIf { it.isNotBlank() },
        license = license?.trim()?.takeIf { it.isNotBlank() },
        categories = categories.filter { it.isNotBlank() }.distinct().take(4),
        versionName = versionName,
        versionCode = versionCode,
        fDroidUrl = "${FdroidConstants.PACKAGES_BASE}/$packageName",
        sourceUrl = sourceUrl?.trim()?.takeIf { it.isNotBlank() },
        issueTrackerUrl = issueTrackerUrl?.trim()?.takeIf { it.isNotBlank() },
        websiteUrl = websiteUrl?.trim()?.takeIf { it.isNotBlank() },
        changelogUrl = changelogUrl?.trim()?.takeIf { it.isNotBlank() },
        donateUrl = donateUrl?.trim()?.takeIf { it.isNotBlank() },
        lastUpdated = lastUpdated,
        added = added,
        permissions = permissions.filter { it.isNotBlank() }.distinct(),
        apkSize = apkSize?.takeIf { it > 0 },
    )
}
