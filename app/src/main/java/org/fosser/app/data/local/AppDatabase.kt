package org.fosser.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.fosser.app.data.local.dao.AppDao
import org.fosser.app.data.local.dao.InteractionDao
import org.fosser.app.data.local.entity.AppEntity
import org.fosser.app.data.local.entity.SavedAppEntity
import org.fosser.app.data.local.entity.SwipeHistoryEntity
import org.fosser.app.domain.model.HistoryAction

@Database(
    entities = [AppEntity::class, SwipeHistoryEntity::class, SavedAppEntity::class],
    version = 2,
    exportSchema = false,
)
@TypeConverters(HistoryActionConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun interactionDao(): InteractionDao
}
