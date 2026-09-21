package org.fosser.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.fosser.app.data.local.entity.SavedAppEntity
import org.fosser.app.data.local.entity.SwipeHistoryEntity

@Dao
interface InteractionDao {
    // --- history ---
    @Insert
    suspend fun insertHistory(entry: SwipeHistoryEntity)

    @Query("SELECT * FROM swipe_history ORDER BY timestamp DESC")
    fun observeHistory(): Flow<List<SwipeHistoryEntity>>

    @Query("SELECT * FROM swipe_history ORDER BY timestamp DESC")
    suspend fun getHistory(): List<SwipeHistoryEntity>

    @Query("SELECT DISTINCT packageName FROM swipe_history")
    suspend fun getSeenPackages(): List<String>

    @Query("DELETE FROM swipe_history")
    suspend fun clearHistory()

    // --- saved ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveApp(entry: SavedAppEntity)

    @Query("DELETE FROM saved_apps WHERE packageName = :packageName")
    suspend fun removeSaved(packageName: String)

    @Query("SELECT * FROM saved_apps ORDER BY savedAt DESC")
    fun observeSaved(): Flow<List<SavedAppEntity>>

    @Query("SELECT * FROM saved_apps ORDER BY savedAt DESC")
    suspend fun getSaved(): List<SavedAppEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_apps WHERE packageName = :packageName)")
    fun observeIsSaved(packageName: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_apps WHERE packageName = :packageName)")
    suspend fun isSaved(packageName: String): Boolean

    @Query("DELETE FROM saved_apps")
    suspend fun clearSaved()
}
