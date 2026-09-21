package org.fosser.app.data.remote.fdroid

object FdroidConstants {
    const val REPO_BASE = "https://f-droid.org/repo"
    const val INDEX_V2_URL = "https://f-droid.org/repo/index-v2.json"
    const val PACKAGES_BASE = "https://f-droid.org/packages"
    const val CONNECT_TIMEOUT_SECONDS = 20L
    const val READ_TIMEOUT_SECONDS = 90L
    /** Cap imports so first launch stays snappy; full 60MB index has ~4500 apps. */
    const val MAX_IMPORT_APPS = 4000
    /** Skip apps with no name/summary to keep deck quality high. */
    const val REQUIRE_SUMMARY = false
}
