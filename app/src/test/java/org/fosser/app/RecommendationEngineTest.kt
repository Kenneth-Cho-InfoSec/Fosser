package org.fosser.app

import org.fosser.app.data.local.entity.SwipeHistoryEntity
import org.fosser.app.domain.model.FdroidApp
import org.fosser.app.domain.model.HistoryAction
import org.fosser.app.domain.recommendation.RecommendationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private fun app(
    pkg: String,
    categories: List<String> = emptyList(),
    developer: String? = null,
    license: String? = null,
    summary: String = "summary",
    lastUpdated: Long = 0L,
) = FdroidApp(
    packageName = pkg, name = pkg, summary = summary, description = "desc $pkg",
    iconUrl = null, screenshots = emptyList(), developer = developer, license = license,
    categories = categories, versionName = "1.0", versionCode = 1,
    fDroidUrl = "https://f-droid.org/packages/$pkg",
    sourceUrl = null, issueTrackerUrl = null, websiteUrl = null,
    changelogUrl = null, donateUrl = null, lastUpdated = lastUpdated, added = null,
)

class RecommendationEngineTest {

    private val engine = RecommendationEngine()

    @Test
    fun `positive interactions outweigh negative ones`() {
        val liked = app("a.liked", listOf("Multimedia"))
        val passed = app("a.passed", listOf("Multimedia"))
        val byPkg = mapOf(liked.packageName to liked, passed.packageName to passed)
        val history = listOf(
            SwipeHistoryEntity(packageName = liked.packageName, action = HistoryAction.LIKE, timestamp = 1),
            SwipeHistoryEntity(packageName = passed.packageName, action = HistoryAction.PASS, timestamp = 2),
        )
        val prefs = engine.buildPreferences(history, byPkg)
        // LIKE +3 vs PASS -2 on same category => net positive
        assertEquals(1.0, prefs.categoryScores["multimedia"] ?: 0.0, 0.001)
    }

    @Test
    fun `category preference boosts same-category candidates`() {
        val termux = app("com.termux", listOf("Development"), developer = "Termux")
        val k9 = app("com.fsck.k9", listOf("Internet"))
        val byPkg = mapOf(termux.packageName to termux, k9.packageName to k9)
        val prefs = engine.buildPreferences(
            listOf(SwipeHistoryEntity(packageName = termux.packageName, action = HistoryAction.LIKE, timestamp = 1)),
            byPkg,
        )
        val devTool = app("com.other.term", listOf("Development"))
        val mailApp = app("com.other.mail", listOf("Internet"))
        assertTrue(engine.score(devTool, prefs) > engine.score(mailApp, prefs))
    }

    @Test
    fun `repeated interactions accumulate`() {
        val a = app("a.x", listOf("Games"))
        val byPkg = mapOf(a.packageName to a)
        val history = List(3) {
            SwipeHistoryEntity(packageName = a.packageName, action = HistoryAction.LIKE, timestamp = it.toLong())
        }
        val prefs = engine.buildPreferences(history, byPkg)
        assertEquals(9.0, prefs.categoryScores["games"] ?: 0.0, 0.001)
    }

    @Test
    fun `unseen apps are prioritized and seen excluded`() {
        val seen = app("seen.app", listOf("Tools"))
        val fresh1 = app("fresh.one", listOf("Tools"), lastUpdated = 200)
        val fresh2 = app("fresh.two", listOf("Games"), lastUpdated = 100)
        val prefs = RecommendationEngine.Preferences()
        val ranked = engine.rank(listOf(seen, fresh1, fresh2), prefs, setOf(seen.packageName))
        assertTrue(ranked.none { it.packageName == seen.packageName })
        assertEquals(2, ranked.size)
    }

    @Test
    fun `open counts as weak positive`() {
        assertEquals(1.0, RecommendationEngine.actionWeight(HistoryAction.OPEN), 0.001)
        assertEquals(3.0, RecommendationEngine.actionWeight(HistoryAction.LIKE), 0.001)
        assertEquals(-2.0, RecommendationEngine.actionWeight(HistoryAction.PASS), 0.001)
    }

    @Test
    fun `diversity avoids long same-category runs`() {
        val apps = listOf(
            app("g.1", listOf("Games"), lastUpdated = 70),
            app("g.2", listOf("Games"), lastUpdated = 60),
            app("g.3", listOf("Games"), lastUpdated = 50),
            app("m.1", listOf("Multimedia"), lastUpdated = 40),
            app("m.2", listOf("Multimedia"), lastUpdated = 30),
            app("m.3", listOf("Multimedia"), lastUpdated = 20),
            app("t.1", listOf("Tools"), lastUpdated = 10),
        )
        val ranked = engine.rank(apps, RecommendationEngine.Preferences())
        var run = 1
        var maxRun = 1
        for (i in 1 until ranked.size) {
            if (ranked[i].category == ranked[i - 1].category) { run += 1; maxRun = maxOf(maxRun, run) }
            else run = 1
        }
        assertTrue("expected diversity, got ${ranked.map { it.packageName }}", maxRun <= 2)
    }
}
