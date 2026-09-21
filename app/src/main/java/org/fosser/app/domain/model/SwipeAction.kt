package org.fosser.app.domain.model

import org.fosser.app.ui.components.swipe.SwipingDirection

/**
 * Reusable gesture -> domain mapping, independent of any dating model.
 * Adapted from TinderCloneCompose's SwipingDirection (LEFT/RIGHT/UP),
 * remapped to Fosser semantics.
 *
 * LEFT  -> PASS (dismiss)
 * RIGHT -> LIKE (save)
 * UP    -> OPEN (open F-Droid page in browser)
 */
enum class SwipeAction {
    PASS,
    LIKE,
    OPEN,
}

fun SwipingDirection.toSwipeAction(): SwipeAction? = when (this) {
    SwipingDirection.Left -> SwipeAction.PASS
    SwipingDirection.Right -> SwipeAction.LIKE
    SwipingDirection.Up -> SwipeAction.OPEN
    SwipingDirection.Down -> null
}

fun SwipeAction.toHistoryAction(): HistoryAction = when (this) {
    SwipeAction.PASS -> HistoryAction.PASS
    SwipeAction.LIKE -> HistoryAction.LIKE
    SwipeAction.OPEN -> HistoryAction.OPEN
}

enum class HistoryAction {
    LIKE,
    PASS,
    OPEN,
}
