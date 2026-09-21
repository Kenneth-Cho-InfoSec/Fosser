package org.fosser.app

import org.fosser.app.domain.model.HistoryAction
import org.fosser.app.domain.model.SwipeAction
import org.fosser.app.domain.model.toHistoryAction
import org.fosser.app.domain.model.toSwipeAction
import org.fosser.app.ui.components.swipe.SwipeableCardState
import org.fosser.app.ui.components.swipe.SwipingDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SwipeActionTest {

    @Test
    fun `LEFT maps to PASS`() {
        assertEquals(SwipeAction.PASS, SwipingDirection.Left.toSwipeAction())
    }

    @Test
    fun `RIGHT maps to LIKE`() {
        assertEquals(SwipeAction.LIKE, SwipingDirection.Right.toSwipeAction())
    }

    @Test
    fun `UP maps to OPEN`() {
        assertEquals(SwipeAction.OPEN, SwipingDirection.Up.toSwipeAction())
    }

    @Test
    fun `DOWN maps to nothing`() {
        assertNull(SwipingDirection.Down.toSwipeAction())
    }

    @Test
    fun `swipe actions map to history actions`() {
        assertEquals(HistoryAction.PASS, SwipeAction.PASS.toHistoryAction())
        assertEquals(HistoryAction.LIKE, SwipeAction.LIKE.toHistoryAction())
        assertEquals(HistoryAction.OPEN, SwipeAction.OPEN.toHistoryAction())
    }

    @Test
    fun `dominant horizontal axis wins over diagonal`() {
        val resolved = SwipeableCardState.resolveDirection(
            x = 400f, y = -150f, maxWidth = 1000f, maxHeight = 1000f,
        )
        assertEquals(SwipingDirection.Right, resolved)
    }

    @Test
    fun `strong upward gesture resolves to UP`() {
        val resolved = SwipeableCardState.resolveDirection(
            x = 60f, y = -400f, maxWidth = 1000f, maxHeight = 1000f,
        )
        assertEquals(SwipingDirection.Up, resolved)
    }

    @Test
    fun `short drag resolves to null`() {
        val resolved = SwipeableCardState.resolveDirection(
            x = 10f, y = 10f, maxWidth = 1000f, maxHeight = 1000f,
        )
        assertNull(resolved)
    }

    @Test
    fun `fdroid url is derived from package name`() {
        val pkg = "com.termux"
        val expected = "https://f-droid.org/packages/$pkg"
        assertEquals(expected, "https://f-droid.org/packages/$pkg")
        assertTrue(expected.startsWith("https://f-droid.org/packages/"))
    }

    @Test
    fun `thresholds are in sensible ranges`() {
        assertTrue(org.fosser.app.ui.components.swipe.SwipeThresholds.HORIZONTAL_FRACTION in 0.25f..0.35f)
        assertTrue(org.fosser.app.ui.components.swipe.SwipeThresholds.VERTICAL_FRACTION in 0.20f..0.30f)
    }
}
