package org.fosser.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 -> v2: card redesign needs per-app permissions and APK size.
 * Nullable-safe defaults so existing cached catalogs and user history survive.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE apps ADD COLUMN permissionsCsv TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE apps ADD COLUMN apkSize INTEGER")
    }
}
