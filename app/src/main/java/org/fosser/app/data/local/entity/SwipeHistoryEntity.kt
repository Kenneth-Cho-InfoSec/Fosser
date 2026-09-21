package org.fosser.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.fosser.app.domain.model.HistoryAction

@Entity(tableName = "swipe_history")
data class SwipeHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val action: HistoryAction,
    val timestamp: Long,
)
