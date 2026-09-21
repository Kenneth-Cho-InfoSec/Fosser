package org.fosser.app.ui.components.swipe

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Card-stack swipe gestures for Fosser's three-way system.
 *
 * Reused interaction foundation from the base repo's `swipableCard`:
 * detectDragGestures + Animatable offset + coerce + dominant-axis resolution.
 *
 * Changes for Fosser:
 * - Default allows LEFT/RIGHT/UP, blocks DOWN.
 * - Configurable thresholds via [SwipeThresholds].
 * - Vertical drags translate (not rotate); rotation damped in state.
 * - Exposes drag offset continuously so overlays (PASS/LIKE/OPEN) fade with distance.
 */
fun Modifier.swipableCard(
    state: SwipeableCardState,
    onSwiped: (SwipingDirection) -> Unit,
    onSwipeCancel: () -> Unit = {},
    onDrag: (Offset) -> Unit = {},
    blockedDirections: List<SwipingDirection> = listOf(SwipingDirection.Down),
) = pointerInput(state) {
    coroutineScope {
        detectDragGestures(
            onDragCancel = {
                launch {
                    state.reset()
                    onSwipeCancel()
                }
            },
            onDrag = { change, dragAmount ->
                launch {
                    val original = state.offset.targetValue
                    val summed = original + dragAmount
                    val newValue = Offset(
                        x = summed.x.coerceIn(-state.maxWidth, state.maxWidth),
                        y = summed.y.coerceIn(-state.maxHeight, state.maxHeight),
                    )
                    if (change.positionChange() != Offset.Zero) change.consume()
                    state.drag(newValue.x, newValue.y)
                    onDrag(newValue)
                }
            },
            onDragEnd = {
                launch {
                    val coercedOffset = state.offset.targetValue
                        .coerceIn(
                            blockedDirections,
                            maxHeight = state.maxHeight,
                            maxWidth = state.maxWidth,
                        )

                    val resolved = SwipeableCardState.resolveDirection(
                        x = coercedOffset.x,
                        y = coercedOffset.y,
                        maxWidth = state.maxWidth,
                        maxHeight = state.maxHeight,
                    )
                    if (resolved == null || resolved in blockedDirections) {
                        state.reset()
                        onSwipeCancel()
                    } else {
                        state.swipe(resolved)
                        onSwiped(resolved)
                    }
                }
            },
        )
    }
}.graphicsLayer {
    translationX = state.offset.value.x
    translationY = state.offset.value.y
    rotationZ = state.rotationDegrees()
}

private fun Offset.coerceIn(
    blockedDirections: List<SwipingDirection>,
    maxHeight: Float,
    maxWidth: Float,
): Offset {
    return copy(
        x = x.coerceIn(
            if (blockedDirections.contains(SwipingDirection.Left)) 0f else -maxWidth,
            if (blockedDirections.contains(SwipingDirection.Right)) 0f else maxWidth,
        ),
        y = y.coerceIn(
            if (blockedDirections.contains(SwipingDirection.Up)) 0f else -maxHeight,
            if (blockedDirections.contains(SwipingDirection.Down)) 0f else maxHeight,
        ),
    )
}
