package org.fosser.app.ui.components.swipe

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Swipe thresholds: horizontal ~30% of card width, vertical ~25% of card height.
 * Dominant-axis wins so diagonal drags don't misfire UP during horizontal swipes.
 */
object SwipeThresholds {
    const val HORIZONTAL_FRACTION = 0.30f
    const val VERTICAL_FRACTION = 0.25f
}

@Composable
fun rememberSwipeableCardState(): SwipeableCardState {
    val screenWidth = with(LocalDensity.current) {
        LocalConfiguration.current.screenWidthDp.dp.toPx()
    }
    val screenHeight = with(LocalDensity.current) {
        LocalConfiguration.current.screenHeightDp.dp.toPx()
    }
    return remember(screenWidth, screenHeight) {
        SwipeableCardState(screenWidth, screenHeight)
    }
}

/**
 * Reused foundation from TinderCloneCompose's SwipeableCardState:
 * Animatable Offset drag state, tween reset/swipe, rotation derived from X.
 * Extended: per-axis drag fractions for overlay opacity + dominant-axis resolve.
 */
class SwipeableCardState(
    internal val maxWidth: Float,
    internal val maxHeight: Float,
) {
    val offset = Animatable(offset(0f, 0f), Offset.VectorConverter)

    var swipedDirection: SwipingDirection? by mutableStateOf(null)
        private set

    internal suspend fun reset() {
        offset.animateTo(offset(0f, 0f), tween(400))
    }

    suspend fun swipe(direction: SwipingDirection, animationSpec: AnimationSpec<Offset> = tween(400)) {
        val endX = maxWidth * 1.5f
        val endY = maxHeight
        when (direction) {
            SwipingDirection.Left -> offset.animateTo(offset(x = -endX), animationSpec)
            SwipingDirection.Right -> offset.animateTo(offset(x = endX), animationSpec)
            SwipingDirection.Up -> offset.animateTo(offset(y = -endY), animationSpec)
            SwipingDirection.Down -> offset.animateTo(offset(y = endY), animationSpec)
        }
        this.swipedDirection = direction
    }

    private fun offset(x: Float = offset.value.x, y: Float = offset.value.y): Offset {
        return Offset(x, y)
    }

    internal suspend fun drag(x: Float, y: Float) {
        offset.animateTo(offset(x, y))
    }

    /** 0..1 overlay strength for a direction, driven by drag distance. */
    fun overlayFraction(direction: SwipingDirection): Float {
        val v = offset.value
        return when (direction) {
            SwipingDirection.Left -> (-v.x / (maxWidth * SwipeThresholds.HORIZONTAL_FRACTION)).coerceIn(0f, 1f)
            SwipingDirection.Right -> (v.x / (maxWidth * SwipeThresholds.HORIZONTAL_FRACTION)).coerceIn(0f, 1f)
            SwipingDirection.Up -> (-v.y / (maxHeight * SwipeThresholds.VERTICAL_FRACTION)).coerceIn(0f, 1f)
            SwipingDirection.Down -> (v.y / (maxHeight * SwipeThresholds.VERTICAL_FRACTION)).coerceIn(0f, 1f)
        }
    }

    /** Rotation keeps the card-stack feel for horizontal drags; vertical drags barely rotate. */
    fun rotationDegrees(): Float {
        val x = offset.value.x
        val yTravel = abs(offset.value.y)
        val dampen = if (yTravel > abs(x)) 0.25f else 1f
        return ((x / 60f) * dampen).coerceIn(-40f, 40f)
    }

    companion object {
        /** Pure helper so unit tests can verify dominant-axis resolution. */
        fun resolveDirection(
            x: Float,
            y: Float,
            maxWidth: Float,
            maxHeight: Float,
        ): SwipingDirection? {
            val horizontalTravel = abs(x)
            val verticalTravel = abs(y)
            val passedX = horizontalTravel >= maxWidth * SwipeThresholds.HORIZONTAL_FRACTION
            val passedY = verticalTravel >= maxHeight * SwipeThresholds.VERTICAL_FRACTION
            if (!passedX && !passedY) return null
            // Dominant axis wins.
            return if (horizontalTravel > verticalTravel) {
                if (x > 0) SwipingDirection.Right else SwipingDirection.Left
            } else {
                if (y < 0) SwipingDirection.Up else SwipingDirection.Down
            }
        }
    }
}
