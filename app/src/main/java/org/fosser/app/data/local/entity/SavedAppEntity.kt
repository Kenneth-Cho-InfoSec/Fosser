package org.fosser.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_apps")
data class SavedAppEntity(
    @PrimaryKey val packageName: String,
    val savedAt: Long,
)
