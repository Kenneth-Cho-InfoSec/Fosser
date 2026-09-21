package org.fosser.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey val packageName: String,
    val name: String,
    val summary: String?,
    val description: String?,
    val iconUrl: String?,
    val screenshotsJson: String, // JSON-encoded list
    val developer: String?,
    val license: String?,
    val categoriesCsv: String, // comma-separated
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
    val catalogUpdatedAt: Long,
    val permissionsCsv: String = "",
    val apkSize: Long? = null,
)
