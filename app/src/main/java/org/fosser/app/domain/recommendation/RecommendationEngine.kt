package org.fosser.app.domain.recommendation

import org.fosser.app.data.local.entity.SwipeHistoryEntity
import org.fosser.app.domain.model.FdroidApp
import org.fosser.app.domain.model.HistoryAction

/**
 * Simple deterministic local recommendation engine.
 *
 * Signals:
 *  LIKE = +3, OPEN = +1, PASS = -2
 *
 * Features: category, developer, license, description keywords.
 * Isolated so it can be improved later without touching UI or data layers.
 */
class RecommendationEngine {

    data class Preferences(
        val categoryScores: Map<String, Double> = emptyMap(),
        val developerScores: Map<String, Double> = emptyMap(),
        val licenseScores: Map<String, Double> = emptyMap(),
        val keywordScores: Map<String, Double> = emptyMap(),
    ) {
        /** Cold start (no history): skip per-app scoring entirely, sort by recency. */
        fun isEmpty(): Boolean =
            categoryScores.isEmpty() && developerScores.isEmpty() &&
                licenseScores.isEmpty() && keywordScores.isEmpty()
    }

    fun buildPreferences(
        history: List<SwipeHistoryEntity>,
        appsByPackage: Map<String, FdroidApp>,
    ): Preferences {
        val categoryScores = mutableMapOf<String, Double>()
        val developerScores = mutableMapOf<String, Double>()
        val licenseScores = mutableMapOf<String, Double>()
        val keywordScores = mutableMapOf<String, Double>()

        for (entry in history) {
            val app = appsByPackage[entry.packageName] ?: continue
            val weight = when (entry.action) {
                HistoryAction.LIKE -> 3.0
                HistoryAction.OPEN -> 1.0
                HistoryAction.PASS -> -2.0
            }
            // Repeat interactions count repeatedly (loop naturally accumulates).
            for (category in app.categories) {
                val key = category.lowercase()
                categoryScores[key] = (categoryScores[key] ?: 0.0) + weight
            }
            app.developer?.lowercase()?.takeIf { it.isNotBlank() }?.let { dev ->
                developerScores[dev] = (developerScores[dev] ?: 0.0) + weight * 0.8
            }
            app.license?.lowercase()?.takeIf { it.isNotBlank() }?.let { lic ->
                licenseScores[lic] = (licenseScores[lic] ?: 0.0) + weight * 0.4
            }
            for (token in keywordsOf(app)) {
                keywordScores[token] = (keywordScores[token] ?: 0.0) + weight * 0.15
            }
        }
        return Preferences(categoryScores, developerScores, licenseScores, keywordScores)
    }

    fun score(app: FdroidApp, prefs: Preferences): Double =
        score(app, prefs, keywordsOf(app))

    private fun score(app: FdroidApp, prefs: Preferences, keywords: Set<String>): Double {
        var s = 0.0
        for (category in app.categories) {
            s += prefs.categoryScores[category.lowercase()] ?: 0.0
        }
        app.developer?.lowercase()?.let { s += prefs.developerScores[it] ?: 0.0 }
        app.license?.lowercase()?.let { s += prefs.licenseScores[it] ?: 0.0 }
        for (token in keywords) {
            s += prefs.keywordScores[token] ?: 0.0
        }
        return s
    }

    /**
     * Rank unseen apps by score, then apply diversity so the same category
     * does not repeat more than twice in a row, then by recency.
     *
     * NOTE: keyword sets are computed once per app (not once per score call)
     * to keep ranking ~4000-app catalogs off the critical path.
     */
    fun rank(
        candidates: List<FdroidApp>,
        prefs: Preferences,
        seenPackages: Set<String> = emptySet(),
    ): List<FdroidApp> {
        val unseen = if (seenPackages.isEmpty()) candidates
        else candidates.filter { it.packageName !in seenPackages }
        if (unseen.isEmpty()) return emptyList()
        // Cold start: no signals yet, so skip the O(n) keyword extraction and
        // score loop completely — recency order is the correct result anyway.
        val sorted = if (prefs.isEmpty()) {
            unseen.sortedWith(
                compareByDescending<FdroidApp> { it.lastUpdated ?: 0L }
                    .thenBy { it.packageName },
            )
        } else {
            // Precompute keyword sets once per app (was: once per score call -> ANR).
            val keywordCache = HashMap<String, Set<String>>(unseen.size)
            fun keywordsFor(app: FdroidApp): Set<String> =
                keywordCache.getOrPut(app.packageName) { keywordsOf(app) }
            unseen.map { it to score(it, prefs, keywordsFor(it)) }
                .sortedWith(
                    compareByDescending<Pair<FdroidApp, Double>> { it.second }
                        .thenByDescending { it.first.lastUpdated ?: 0L }
                        .thenBy { it.first.packageName },
                )
                .map { it.first }
        }
        return diversify(sorted)
    }

    /**
     * Spread same-category runs (max 2 in a row) in O(n): deferred apps wait in
     * per-category queues instead of a single queue that was rescanned and
     * spliced on every step (O(n^2) worst case on skewed catalogs).
     */
    internal fun diversify(sorted: List<FdroidApp>): List<FdroidApp> {
        if (sorted.size < 4) return sorted
        val result = ArrayList<FdroidApp>(sorted.size)
        val pending = LinkedHashMap<String?, ArrayDeque<FdroidApp>>()
        var lastCategory: String? = null
        var runLength = 0

        fun emit(app: FdroidApp) {
            result.add(app)
            val cat = app.category?.lowercase()
            if (cat == lastCategory) runLength += 1 else { lastCategory = cat; runLength = 1 }
        }

        // Pull one waiting app of a different category, first-seen-bucket first.
        fun drainOne(): Boolean {
            for ((cat, queue) in pending) {
                if (queue.isNotEmpty() && cat != lastCategory) {
                    emit(queue.removeFirst())
                    return true
                }
            }
            return false
        }

        for (app in sorted) {
            val cat = app.category?.lowercase()
            if (cat != null && cat == lastCategory && runLength >= 2) {
                pending.getOrPut(cat) { ArrayDeque() }.addLast(app)
                continue
            }
            emit(app)
            drainOne()
        }
        // Leftovers keep their relative (sorted) order per bucket.
        for ((_, queue) in pending) {
            while (queue.isNotEmpty()) emit(queue.removeFirst())
        }
        return result
    }

    internal fun keywordsOf(app: FdroidApp): Set<String> {
        // Truncate long descriptions: keywords don't need the full text and
        // full-text regex splits on ~4000 apps caused the main-thread ANR.
        val desc = app.description?.take(600) ?: ""
        val text = buildString {
            append(app.name); append(' ')
            append(app.summary ?: ""); append(' ')
            append(desc); append(' ')
            append(app.categories.joinToString(" "))
        }.lowercase()
        // Lazy token scan: stops as soon as 24 keywords are collected instead of
        // materializing every token in the text first.
        val out = LinkedHashSet<String>()
        for (match in TOKEN.findAll(text)) {
            if (out.size >= MAX_KEYWORDS) break
            val token = match.value
            if (token.length >= 4 && token !in STOPWORDS) out.add(token)
        }
        return out
    }

    companion object {
        private const val MAX_KEYWORDS = 24
        private val TOKEN = Regex("[a-z0-9]+")
        private val STOPWORDS = setOf(
            "with", "from", "this", "that", "your", "have", "more",
            "free", "open", "source", "android", "application", "allows",
            "using", "based", "also", "into", "such", "like",
        )

        fun actionWeight(action: HistoryAction): Double = when (action) {
            HistoryAction.LIKE -> 3.0
            HistoryAction.OPEN -> 1.0
            HistoryAction.PASS -> -2.0
        }
    }
}
