package org.fosser.app.ui.components.swipe

/**
 * Adapted from TinderCloneCompose's SwipingDirection.
 * Kept as the reusable gesture primitive; Fosser maps it to [org.fosser.app.domain.model.SwipeAction]:
 * LEFT -> PASS, RIGHT -> LIKE, UP -> OPEN. DOWN is blocked.
 */
enum class SwipingDirection {
    Left, Right, Up, Down
}
