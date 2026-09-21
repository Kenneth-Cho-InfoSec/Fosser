package org.fosser.app

import org.fosser.app.data.local.toDomain
import org.fosser.app.data.local.toEntity
import org.fosser.app.domain.model.FdroidApp
import org.junit.Assert.assertEquals
import org.junit.Test

class MappersTest {

    private fun sample() = FdroidApp(
        packageName = "com.termux",
        name = "Termux",
        summary = "Terminal",
        description = "Linux terminal",
        iconUrl = "https://f-droid.org/repo/com.termux/en-US/icon.png",
        screenshots = listOf("https://f-droid.org/repo/a.png", "https://f-droid.org/repo/b.png"),
        developer = "Termux",
        license = "GPL-3.0-or-later",
        categories = listOf("Development", "System"),
        versionName = "0.118",
        versionCode = 118,
        fDroidUrl = "https://f-droid.org/packages/com.termux",
        sourceUrl = "https://github.com/termux/termux-app",
        issueTrackerUrl = null,
        websiteUrl = "https://termux.dev",
        changelogUrl = null,
        donateUrl = null,
        lastUpdated = 1700000000000,
        added = 1500000000000,
        permissions = listOf("android.permission.INTERNET", "android.permission.WAKE_LOCK"),
        apkSize = 114920926,
    )

    @Test
    fun `entity round trip preserves fields`() {
        val now = 12345L
        val domain = sample().toEntity(now).toDomain()
        val original = sample()
        assertEquals(original.packageName, domain.packageName)
        assertEquals(original.name, domain.name)
        assertEquals(original.summary, domain.summary)
        assertEquals(original.description, domain.description)
        assertEquals(original.iconUrl, domain.iconUrl)
        assertEquals(original.screenshots, domain.screenshots)
        assertEquals(original.developer, domain.developer)
        assertEquals(original.license, domain.license)
        assertEquals(original.categories, domain.categories)
        assertEquals(original.versionName, domain.versionName)
        assertEquals(original.versionCode, domain.versionCode)
        assertEquals(original.fDroidUrl, domain.fDroidUrl)
        assertEquals(original.lastUpdated, domain.lastUpdated)
        assertEquals(original.permissions, domain.permissions)
        assertEquals(original.apkSize, domain.apkSize)
    }

    @Test
    fun `empty lists survive round trip`() {
        val app = sample().copy(screenshots = emptyList(), categories = emptyList())
        val back = app.toEntity(0).toDomain()
        assertEquals(emptyList<String>(), back.screenshots)
        assertEquals(emptyList<String>(), back.categories)
    }
}
