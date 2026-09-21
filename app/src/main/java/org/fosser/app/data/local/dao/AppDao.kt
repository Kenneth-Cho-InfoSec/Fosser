package org.fosser.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.fosser.app.data.local.entity.AppEntity

@Dao
interface AppDao {
    @Query("SELECT * FROM apps ORDER BY lastUpdated DESC")
    fun observeAll(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps ORDER BY lastUpdated DESC")
    suspend fun getAll(): List<AppEntity>

    @Query("SELECT * FROM apps WHERE packageName = :packageName LIMIT 1")
    fun observeByPackage(packageName: String): Flow<AppEntity?>

    @Query("SELECT * FROM apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getByPackage(packageName: String): AppEntity?

    @Query("SELECT COUNT(*) FROM apps")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(apps: List<AppEntity>)

    @Query("DELETE FROM apps")
    suspend fun clear()
}
