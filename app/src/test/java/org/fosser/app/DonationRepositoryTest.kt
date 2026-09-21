package org.fosser.app

import org.fosser.app.data.repository.DonationRepository
import org.fosser.app.data.repository.InMemoryStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DonationRepositoryTest {

    @Test
    fun `recordAppOpen counts cold starts`() {
        val repo = DonationRepository(InMemoryStore())
        assertEquals(0, repo.openCount())
        assertEquals(1, repo.recordAppOpen())
        assertEquals(2, repo.recordAppOpen())
    }

    @Test
    fun `prompt appears exactly on milestones`() {
        val repo = DonationRepository(InMemoryStore())
        repeat(4) { repo.recordAppOpen() }
        assertFalse(repo.shouldPrompt())
        repo.recordAppOpen() // 5
        assertTrue(repo.shouldPrompt())
        repeat(14) { repo.recordAppOpen() } // 19
        assertFalse(repo.shouldPrompt())
        repo.recordAppOpen() // 20
        assertTrue(repo.shouldPrompt())
        repeat(79) { repo.recordAppOpen() } // 99
        assertFalse(repo.shouldPrompt())
        repo.recordAppOpen() // 100
        assertTrue(repo.shouldPrompt())
        repo.recordAppOpen() // 101
        assertFalse(repo.shouldPrompt())
    }

    @Test
    fun `donating suppresses future prompts`() {
        val repo = DonationRepository(InMemoryStore())
        repeat(5) { repo.recordAppOpen() }
        assertTrue(repo.shouldPrompt())
        repo.markDonated()
        assertFalse(repo.shouldPrompt())
        repeat(95) { repo.recordAppOpen() } // 100
        assertFalse(repo.shouldPrompt())
    }

    @Test
    fun `never-ask suppresses future prompts`() {
        val repo = DonationRepository(InMemoryStore())
        repeat(5) { repo.recordAppOpen() }
        assertTrue(repo.shouldPrompt())
        repo.markNeverAsk()
        assertFalse(repo.shouldPrompt())
    }

    @Test
    fun `donate url points at ko-fi`() {
        assertEquals("https://ko-fi.com/kennethchoinfosec", DonationRepository.DONATE_URL)
    }
}
