package org.fosser.app.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.fosser.app.data.local.AppDatabase
import org.fosser.app.data.local.entity.SavedAppEntity
import org.fosser.app.data.local.entity.SwipeHistoryEntity
import org.fosser.app.data.local.toDomain
import org.fosser.app.data.local.toEntity
import org.fosser.app.data.remote.fdroid.FdroidIndexFetcher
import org.fosser.app.data.seed.SeedApps
import org.fosser.app.domain.model.FdroidApp
import org.fosser.app.domain.model.HistoryAction
import org.fosser.app.domain.recommendation.RecommendationEngine

/**
 * Local-first repository.
 * - Room is the source of truth for UI.
 * - F-Droid index-v2 is fetched on demand and cached.
 * - RecommendationEngine ranks unseen apps with diversity.
 */
class FdroidRepository(
    private val db: AppDatabase,
    private val fetcher: FdroidIndexFetcher = FdroidIndexFetcher(),
    private val engine: RecommendationEngine = RecommendationEngine(),
) {
    sealed interface RefreshResult {
        data class Updated(val count: Int) : RefreshResult
        data class Failed(val message: String, val cachedCount: Int) : RefreshResult
    }

    fun observeDeck(limit: Int = 60): Flow<List<FdroidApp>> {
        return combine(
            db.appDao().observeAll(),
            db.interactionDao().observeHistory(),
        ) { apps, history ->
            rankApps(apps.map { it.toDomain() }, history).take(limit)
        }.flowOn(Dispatchers.Default)
    }

    suspend fun currentDeck(limit: Int = 60): List<FdroidApp> = withContext(Dispatchers.Default) {
        val apps = db.appDao().getAll().map { it.toDomain() }
        val history = db.interactionDao().getHistory()
        rankApps(apps, history).take(limit)
    }

    private fun rankApps(apps: List<FdroidApp>, history: List<SwipeHistoryEntity>): List<FdroidApp> {
        if (apps.isEmpty()) return emptyList()
        val byPackage = apps.associateBy { it.packageName }
        val prefs = engine.buildPreferences(history, byPackage)
        val seen = history.map { it.packageName }.toSet()
        return engine.rank(apps, prefs, seen)
    }

    suspend fun ensureSeeded() {
        if (db.appDao().count() == 0) {
            val now = System.currentTimeMillis()
            db.appDao().upsertAll(SeedApps.apps.map { it.toEntity(now) })
        }
    }

    suspend fun refreshCatalog(): RefreshResult {
        return when (val result = fetcher.fetch()) {
            is FdroidIndexFetcher.Result.Success -> {
                val now = System.currentTimeMillis()
                // Chunked writes: one 4000-row transaction caused "Long db operation" ANRs.
                result.apps.map { it.toEntity(now) }.chunked(400).forEach { chunk ->
                    db.appDao().upsertAll(chunk)
                }
                RefreshResult.Updated(result.apps.size)
            }
            is FdroidIndexFetcher.Result.Failure -> {
                RefreshResult.Failed(result.message, db.appDao().count())
            }
        }
    }

    suspend fun recordAction(packageName: String, action: HistoryAction) {
        db.interactionDao().insertHistory(
            SwipeHistoryEntity(packageName = packageName, action = action, timestamp = System.currentTimeMillis()),
        )
        if (action == HistoryAction.LIKE) {
            db.interactionDao().saveApp(SavedAppEntity(packageName, System.currentTimeMillis()))
        }
    }

    suspend fun unsave(packageName: String) {
        db.interactionDao().removeSaved(packageName)
    }

    fun observeSavedApps(): Flow<List<FdroidApp>> {
        return combine(
            db.interactionDao().observeSaved(),
            db.appDao().observeAll(),
        ) { saved, apps ->
            val byPackage = apps.associateBy { it.packageName }
            saved.mapNotNull { byPackage[it.packageName]?.toDomain() }
        }
    }

    fun observeHistory(): Flow<List<Pair<FdroidApp?, SwipeHistoryEntity>>> {
        return combine(
            db.interactionDao().observeHistory(),
            db.appDao().observeAll(),
        ) { history, apps ->
            val byPackage = apps.associateBy { it.packageName }
            history.map { entry -> (byPackage[entry.packageName]?.toDomain()) to entry }
        }
    }

    fun observeApp(packageName: String): Flow<FdroidApp?> =
        db.appDao().observeByPackage(packageName).map { it?.toDomain() }

    suspend fun getApp(packageName: String): FdroidApp? =
        db.appDao().getByPackage(packageName)?.toDomain()

    fun observeIsSaved(packageName: String): Flow<Boolean> =
        db.interactionDao().observeIsSaved(packageName)

    suspend fun resetHistory() {
        db.interactionDao().clearHistory()
        db.interactionDao().clearSaved()
    }

    suspend fun catalogCount(): Int = db.appDao().count()

    suspend fun historyForTest(): List<SwipeHistoryEntity> = db.interactionDao().getHistory()
}
