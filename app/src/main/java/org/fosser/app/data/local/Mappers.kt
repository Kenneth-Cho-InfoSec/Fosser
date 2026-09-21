package org.fosser.app.data.local

import org.fosser.app.data.local.entity.AppEntity
import org.fosser.app.domain.model.FdroidApp

private const val SEP = "‚‗‚" // unlikely separator for JSON lists

fun FdroidApp.toEntity(now: Long): AppEntity = AppEntity(
    packageName = packageName,
    name = name,
    summary = summary,
    description = description,
    iconUrl = iconUrl,
    screenshotsJson = screenshots.joinToString(SEP),
    developer = developer,
    license = license,
    categoriesCsv = categories.joinToString(SEP),
    versionName = versionName,
    versionCode = versionCode,
    fDroidUrl = fDroidUrl,
    sourceUrl = sourceUrl,
    issueTrackerUrl = issueTrackerUrl,
    websiteUrl = websiteUrl,
    changelogUrl = changelogUrl,
    donateUrl = donateUrl,
    lastUpdated = lastUpdated,
    added = added,
    catalogUpdatedAt = now,
    permissionsCsv = permissions.joinToString(SEP),
    apkSize = apkSize,
)

fun AppEntity.toDomain(): FdroidApp = FdroidApp(
    packageName = packageName,
    name = name,
    summary = summary,
    description = description,
    iconUrl = iconUrl,
    screenshots = if (screenshotsJson.isBlank()) emptyList() else screenshotsJson.split(SEP).filter { it.isNotBlank() },
    developer = developer,
    license = license,
    categories = if (categoriesCsv.isBlank()) emptyList() else categoriesCsv.split(SEP).filter { it.isNotBlank() },
    versionName = versionName,
    versionCode = versionCode,
    fDroidUrl = fDroidUrl,
    sourceUrl = sourceUrl,
    issueTrackerUrl = issueTrackerUrl,
    websiteUrl = websiteUrl,
    changelogUrl = changelogUrl,
    donateUrl = donateUrl,
    lastUpdated = lastUpdated,
    added = added,
    permissions = if (permissionsCsv.isBlank()) emptyList() else permissionsCsv.split(SEP).filter { it.isNotBlank() },
    apkSize = apkSize,
)
