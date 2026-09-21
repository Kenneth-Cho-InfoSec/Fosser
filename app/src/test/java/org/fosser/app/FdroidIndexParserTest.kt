package org.fosser.app

import com.google.gson.stream.JsonReader
import org.fosser.app.data.remote.fdroid.FdroidIndexParser
import org.fosser.app.data.remote.fdroid.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.StringReader

class FdroidIndexParserTest {

    private val sample = """
    {
      "repo": {"name": "F-Droid", "address": "https://f-droid.org/repo"},
      "packages": {
        "com.termux": {
          "metadata": {
            "added": 1500000000000,
            "categories": ["Development"],
            "issueTracker": "https://github.com/termux/termux-app/issues",
            "lastUpdated": 1700000000000,
            "license": "GPL-3.0-or-later",
            "sourceCode": "https://github.com/termux/termux-app",
            "webSite": "https://termux.dev",
            "authorName": "Termux",
            "name": {"en-US": "Termux"},
            "summary": {"en-US": "Terminal emulator"},
            "description": {"en-US": "Linux terminal on Android."},
            "icon": {"en-US": {"name": "/com.termux/en-US/icon_x.png"}},
            "screenshots": {"phone": {"en-US": [{"name": "/com.termux/en-US/phoneScreenshots/0.png"}]}}
          },
          "versions": {
            "abc": {"added": 1700000000000,
                    "file": {"name": "/com.termux_118.apk", "size": 114920926},
                    "manifest": {"versionName": "0.118", "versionCode": 118,
                                 "usesPermission": [{"name": "android.permission.INTERNET"},
                                                    {"name": "android.permission.WAKE_LOCK"}]}}
          }
        },
        "com.noname": {
          "metadata": {
            "categories": [],
            "name": {"en-US": ""},
            "summary": {"en-US": "x"}
          },
          "versions": {}
        }
      }
    }
    """.trimIndent()

    @Test
    fun `parses package with localized fields and version`() {
        val parsed = FdroidIndexParser.parse(ByteArrayInputStream(sample.toByteArray()))
        assertEquals(2, parsed.size)
        val termux = parsed.first { it.packageName == "com.termux" }
        assertEquals("Termux", termux.name)
        assertEquals("Terminal emulator", termux.summary)
        assertEquals("/com.termux/en-US/icon_x.png", termux.iconPath)
        assertEquals(listOf("/com.termux/en-US/phoneScreenshots/0.png"), termux.screenshotPaths)
        assertEquals("0.118", termux.versionName)
        assertEquals(118L, termux.versionCode)
        assertEquals("https://termux.dev", termux.websiteUrl)
        assertEquals(
            listOf("android.permission.INTERNET", "android.permission.WAKE_LOCK"),
            termux.permissions,
        )
        assertEquals(114920926L, termux.apkSize)
    }

    @Test
    fun `domain mapping builds absolute URLs and filters blank names`() {
        val parsed = FdroidIndexParser.parse(ByteArrayInputStream(sample.toByteArray()))
        val apps = parsed.mapNotNull { it.toDomain() }
        assertEquals(1, apps.size)
        val termux = apps.single()
        assertEquals("https://f-droid.org/packages/com.termux", termux.fDroidUrl)
        assertTrue(termux.iconUrl!!.startsWith("https://f-droid.org/repo/com.termux"))
        assertTrue(termux.screenshots.single().startsWith("https://f-droid.org/repo/com.termux"))
    }

    @Test
    fun `localized prefers en-US over other locales`() {
        val json = """{"de": "Terminal", "en-US": "Terminal emulator", "fr": "Terminal"}"""
        val reader = JsonReader(StringReader(json))
        assertEquals("Terminal emulator", FdroidIndexParser.readLocalized(reader))
    }
}
