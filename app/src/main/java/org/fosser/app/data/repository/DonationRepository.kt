package org.fosser.app.data.repository

/**
 * Donation prompt state. The dialog appears on the 5th, 20th and 100th
 * cold start, unless the user already donated or asked never to be asked.
 * Tapping "Later" simply waits for the next milestone.
 */
class DonationRepository(private val store: KeyValueStore) {

    /** Records a cold start; returns the new open count. */
    fun recordAppOpen(): Int {
        val next = store.getInt(KEY_COUNT, 0) + 1
        store.putInt(KEY_COUNT, next)
        return next
    }

    fun openCount(): Int = store.getInt(KEY_COUNT, 0)

    fun shouldPrompt(): Boolean {
        if (store.getBoolean(KEY_DONATED, false)) return false
        if (store.getBoolean(KEY_NEVER, false)) return false
        return store.getInt(KEY_COUNT, 0) in MILESTONES
    }

    fun markDonated() {
        store.putBoolean(KEY_DONATED, true)
    }

    fun markNeverAsk() {
        store.putBoolean(KEY_NEVER, true)
    }

    companion object {
        val MILESTONES = setOf(5, 20, 100)
        const val KEY_COUNT = "app_open_count"
        const val KEY_DONATED = "donated"
        const val KEY_NEVER = "donate_never_ask"
        const val DONATE_URL = "https://ko-fi.com/kennethchoinfosec"
    }
}
