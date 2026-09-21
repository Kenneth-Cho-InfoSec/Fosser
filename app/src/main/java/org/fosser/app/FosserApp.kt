package org.fosser.app

import android.app.Application
import android.content.Context
import androidx.room.Room
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import okio.Path.Companion.toOkioPath
import org.fosser.app.data.local.AppDatabase
import org.fosser.app.data.local.MIGRATION_1_2
import org.fosser.app.data.repository.DonationRepository
import org.fosser.app.data.repository.SharedPrefsStore
import org.fosser.app.data.repository.ThemeRepository
import org.fosser.app.data.remote.fdroid.FdroidIndexFetcher
import org.fosser.app.data.repository.FdroidRepository
import org.fosser.app.di.AppContainer
import org.fosser.app.domain.recommendation.RecommendationEngine

class FosserApp : Application(), SingletonImageLoader.Factory {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val db = Room.databaseBuilder(this, AppDatabase::class.java, "fosser.db")
            .addMigrations(MIGRATION_1_2)
            .build()
        val store = SharedPrefsStore(getSharedPreferences(ThemeRepository.PREFS_NAME, Context.MODE_PRIVATE))
        val themeRepository = ThemeRepository(store)
        val donationRepository = DonationRepository(store)
        donationRepository.recordAppOpen()
        container = AppContainer(
            db,
            FdroidRepository(db, FdroidIndexFetcher(), RecommendationEngine()),
            themeRepository,
            donationRepository,
        )
    }

    /**
     * Coil 3 singleton loader: OkHttp networking (pluggable since Coil 3)
     * plus memory and disk caching for icons/screenshots.
     */
    override fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory())
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache").toOkioPath())
                    .maxSizePercent(0.02)
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
