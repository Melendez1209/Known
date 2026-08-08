package com.melendez.known.ui.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

const val DefaultMotionDuration: Int = 300
private const val ProgressThreshold = 0.35f

// Predictive back gesture related parameters
private const val PredictiveBackThreshold = 0.4f

private val Int.ForOutgoing: Int
    get() = (this * ProgressThreshold).toInt()

private val Int.ForIncoming: Int
    get() = this - this.ForOutgoing

private val Int.ForPredictiveOutgoing: Int
    get() = (this * PredictiveBackThreshold).toInt()

private val Int.ForPredictiveIncoming: Int
    get() = this - this.ForPredictiveOutgoing

/** [materialSharedAxisXIn] allows to switch a layout with shared X-axis enter transition. */
fun materialSharedAxisXIn(
    initialOffsetX: (fullWidth: Int) -> Int,
    durationMillis: Int = DefaultMotionDuration,
    isPredictiveBack: Boolean = false
): EnterTransition {
    if (isPredictiveBack) PredictiveBackThreshold else ProgressThreshold
    val incomingDuration =
        if (isPredictiveBack) durationMillis.ForPredictiveIncoming else durationMillis.ForIncoming
    val outgoingDuration =
        if (isPredictiveBack) durationMillis.ForPredictiveOutgoing else durationMillis.ForOutgoing

    return slideInHorizontally(
        animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
        initialOffsetX = initialOffsetX,
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = incomingDuration,
            delayMillis = outgoingDuration,
            easing = LinearOutSlowInEasing,
        )
    )
}

/** [materialSharedAxisXOut] allows to switch a layout with shared X-axis exit transition. */
fun materialSharedAxisXOut(
    targetOffsetX: (fullWidth: Int) -> Int,
    durationMillis: Int = DefaultMotionDuration,
    isPredictiveBack: Boolean = false
): ExitTransition {
    val outgoingDuration =
        if (isPredictiveBack) durationMillis.ForPredictiveOutgoing else durationMillis.ForOutgoing

    return slideOutHorizontally(
        animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
        targetOffsetX = targetOffsetX,
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = outgoingDuration,
            delayMillis = 0,
            easing = FastOutLinearInEasing,
        )
    )
}
