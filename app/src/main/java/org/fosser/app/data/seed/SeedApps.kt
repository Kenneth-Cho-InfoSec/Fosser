package org.fosser.app.data.seed

import org.fosser.app.data.remote.fdroid.FdroidConstants
import org.fosser.app.domain.model.FdroidApp

/**
 * Small bundled catalog of well-known F-Droid apps.
 * Used ONLY as an offline bootstrap until the first real index-v2 download
 * succeeds. Production data always comes from https://f-droid.org/repo/index-v2.json.
 */
object SeedApps {
    private fun app(
        packageName: String,
        name: String,
        summary: String,
        description: String,
        developer: String?,
        license: String?,
        categories: List<String>,
        versionName: String?,
        sourceUrl: String?,
        websiteUrl: String? = null,
    ): FdroidApp = FdroidApp(
        packageName = packageName,
        name = name,
        summary = summary,
        description = description,
        iconUrl = null, // icons load from network on refresh; seed stays lightweight
        screenshots = emptyList(),
        developer = developer,
        license = license,
        categories = categories,
        versionName = versionName,
        versionCode = null,
        fDroidUrl = "${FdroidConstants.PACKAGES_BASE}/$packageName",
        sourceUrl = sourceUrl,
        issueTrackerUrl = sourceUrl?.trimEnd('/')?.plus("/issues"),
        websiteUrl = websiteUrl,
        changelogUrl = null,
        donateUrl = null,
        lastUpdated = null,
        added = null,
    )

    val apps: List<FdroidApp> = listOf(
        app("org.fdroid.fdroid", "F-Droid", "The catalogue of free and open source apps", "F-Droid is an installable catalogue of FOSS applications for Android.", "F-Droid Limited", "GPL-3.0-or-later", listOf("System"), "1.23", "https://gitlab.com/fdroid/fdroidclient", "https://f-droid.org"),
        app("org.mozilla.fennec_fdroid", "Fennec F-Droid", "Privacy-conscious browsing based on Firefox", "Fennec is a build of Firefox for Android with proprietary bits removed.", "Mozilla", "MPL-2.0", listOf("Internet"), "124", "https://hg.mozilla.org/mozilla-central"),
        app("de.danoeh.antennapod", "AntennaPod", "Podcast manager and player", "AntennaPod is a podcast manager with streaming, download and queue support.", "AntennaPod Team", "MIT", listOf("Multimedia"), "3.4", "https://github.com/AntennaPod/AntennaPod", "https://antennapod.org"),
        app("free.rm.skytube.oss", "SkyTube", "YouTube client without tracking", "SkyTube lets you watch YouTube videos without a Google account.", "SkyTube", "GPL-3.0-or-later", listOf("Multimedia"), "3.0", "https://github.com/ram-on/SkyTube"),
        app("org.schabi.newpipe", "NewPipe", "Lightweight YouTube frontend", "NewPipe is a lightweight YouTube experience with downloads and background play.", "NewPipe Team", "GPL-3.0-or-later", listOf("Multimedia"), "0.27", "https://github.com/TeamNewPipe/NewPipe"),
        app("com.fsck.k9", "K-9 Mail", "Full-featured email client", "K-9 Mail is an email client with push, PGP and multi-account support.", "K-9 Team", "Apache-2.0", listOf("Internet"), "6.7", "https://github.com/thundernest/k-9"),
        app("org.thoughtcrime.securesms", "Signal is not on F-Droid; use Molly", "Placeholder removed", "Molly is a hardened Signal fork distributed via F-Droid repos.", "Molly", "GPL-3.0-or-later", listOf("Internet"), "7.0", "https://github.com/mollyim/mollyim-android"),
        app("im.vector.app", "Element", "Matrix chat client", "Element is a secure messenger built on the Matrix protocol.", "Element", "Apache-2.0", listOf("Internet"), "1.6", "https://github.com/element-hq/element-android"),
        app("org.telegram.messenger", "Telegram FOSS", "Fast messaging client", "Telegram-FOSS is a build of Telegram without proprietary blobs.", "Telegram", "GPL-2.0-or-later", listOf("Internet"), "10.0", "https://github.com/Telegram-FOSS-Team/Telegram-FOSS"),
        app("com.termux", "Termux", "Terminal emulator with Linux packages", "Termux brings a full Linux terminal environment to Android.", "Termux", "GPL-3.0-or-later", listOf("Development"), "0.118", "https://github.com/termux/termux-app", "https://termux.dev"),
        app("org.videolan.vlc", "VLC", "Plays everything", "VLC is a media player that handles local files and network streams.", "VideoLAN", "GPL-2.0-or-later", listOf("Multimedia"), "3.6", "https://github.com/videolan/vlc", "https://videolan.org"),
        app("org.wikipedia", "Wikipedia", "The free encyclopedia", "Official Wikipedia app with offline reading lists and nearby articles.", "Wikimedia", "Apache-2.0", listOf("Reading"), "2.7", "https://github.com/wikimedia/apps-android-wikipedia"),
        app("net.osmand.plus", "OsmAnd", "Offline maps and navigation", "OsmAnd offers offline maps, navigation and trip recording from OpenStreetMap.", "OsmAnd", "GPL-3.0-or-later", listOf("Navigation"), "4.8", "https://github.com/osmandapp/OsmAnd", "https://osmand.net"),
        app("com.nextcloud.client", "Nextcloud", "Self-hosted file sync and share", "Nextcloud Android syncs files, calendars and contacts with your server.", "Nextcloud", "GPL-2.0-or-later", listOf("Internet"), "3.28", "https://github.com/nextcloud/android", "https://nextcloud.com"),
        app("org.keepassdroid", "KeePassDroid", "Password manager", "KeePassDroid stores passwords securely in KeePass databases.", "KeePassDroid", "GPL-3.0-or-later", listOf("Security"), "2.7", "https://github.com/BrianTheCoder/KeePassDroid"),
        app("com.owncloud.android", "ownCloud", "File sync client", "ownCloud app connects to your ownCloud server for files and sharing.", "ownCloud", "GPL-2.0-or-later", listOf("Internet"), "4.2", "https://github.com/owncloud/android"),
        app("org.tasks", "Tasks.org", "To-do lists and reminders", "Tasks.org is a powerful to-do manager with sync and location reminders.", "Alex Baker", "GPL-3.0-or-later", listOf("Productivity"), "13.0", "https://github.com/tasks/tasks"),
        app("org.lectureapp", "Placeholder-removed", "Lecture notes", "Lecture notes placeholder replaced at runtime.", "Example", "GPL-3.0-or-later", listOf("Productivity"), "1.0", null),
        app("fr.neamar.kiss", "KISS Launcher", "Blazing fast launcher", "KISS is a minimal launcher with instant search for apps and contacts.", "Neamar", "GPL-3.0-or-later", listOf("System"), "3.19", "https://github.com/Neamar/KISS"),
        app("eu.faircode.netguard", "NetGuard", "Firewall without root", "NetGuard blocks internet access per app without requiring root.", "FairCode", "GPL-3.0-or-later", listOf("Security"), "2.3", "https://github.com/M66B/NetGuard"),
        app("org.blokada.alarm", "Blokada", "Ad blocker", "Blokada blocks ads and trackers across all your apps.", "Blokada", "GPL-3.0-or-later", listOf("Security"), "6.0", "https://github.com/blokadaorg/blokada"),
        app("com.simplemobiletools.gallery.pro", "Simple Gallery", "Photo gallery without ads", "Simple Gallery is a fast photo viewer with editor and vault.", "Simple Mobile Tools", "GPL-3.0-or-later", listOf("Multimedia"), "6.0", "https://github.com/SimpleMobileTools/Simple-Gallery"),
        app("org.lineageos.jelly", "Jelly", "Lightweight browser", "Jelly is a minimal browser for LineageOS with privacy defaults.", "LineageOS", "Apache-2.0", listOf("Internet"), "2.4", "https://github.com/LineageOS/android_packages_apps_Jelly"),
        app("org.fossify.gallery", "Fossify Gallery", "Community gallery fork", "Fossify Gallery continues the Simple Gallery legacy as a community fork.", "Fossify", "GPL-3.0-or-later", listOf("Multimedia"), "1.0", "https://github.com/FossifyOrg/Gallery"),
    ).filter { it.packageName != "org.lectureapp" }
}
