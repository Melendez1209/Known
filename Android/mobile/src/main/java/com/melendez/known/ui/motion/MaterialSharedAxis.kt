package com.melendez.known.ui.motion

/*
 * Copyright 2021 SOUP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically

const val DefaultMotionDuration: Int = 300
private const val ProgressThreshold = 0.35f

// Predictive back gesture related parameters
const val PredictiveBackDuration: Int = 380
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
    val incomingDuration = if (isPredictiveBack) durationMillis.ForPredictiveIncoming else durationMillis.ForIncoming
    val outgoingDuration = if (isPredictiveBack) durationMillis.ForPredictiveOutgoing else durationMillis.ForOutgoing
    
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
    val outgoingDuration = if (isPredictiveBack) durationMillis.ForPredictiveOutgoing else durationMillis.ForOutgoing
    
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

/** [materialSharedAxisYIn] allows to switch a layout with shared Y-axis enter transition. */
fun materialSharedAxisYIn(
    initialOffsetY: (fullHeight: Int) -> Int,
    durationMillis: Int = DefaultMotionDuration,
): EnterTransition = slideInVertically(
    animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
    initialOffsetY = initialOffsetY,
) + fadeIn(
    animationSpec = tween(
        durationMillis = durationMillis.ForIncoming,
        delayMillis = durationMillis.ForOutgoing,
        easing = LinearOutSlowInEasing,
    )
)

/** [materialSharedAxisYOut] allows to switch a layout with shared Y-axis exit transition. */
fun materialSharedAxisYOut(
    targetOffsetY: (fullHeight: Int) -> Int,
    durationMillis: Int = DefaultMotionDuration,
): ExitTransition = slideOutVertically(
    animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
    targetOffsetY = targetOffsetY,
) + fadeOut(
    animationSpec = tween(
        durationMillis = durationMillis.ForOutgoing,
        delayMillis = 0,
        easing = FastOutLinearInEasing,
    )
)

/** [materialSharedAxisZIn] allows to switch a layout with shared Z-axis enter transition. */
fun materialSharedAxisZIn(
    initialScale: Float = 0.8f,
    durationMillis: Int = DefaultMotionDuration,
): EnterTransition = scaleIn(
    animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
    initialScale = initialScale,
) + fadeIn(
    animationSpec = tween(
        durationMillis = durationMillis.ForIncoming,
        delayMillis = durationMillis.ForOutgoing,
        easing = LinearOutSlowInEasing,
    )
)

/** [materialSharedAxisZOut] allows to switch a layout with shared Z-axis exit transition. */
fun materialSharedAxisZOut(
    targetScale: Float = 1.1f,
    durationMillis: Int = DefaultMotionDuration,
): ExitTransition = scaleOut(
    animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
    targetScale = targetScale,
) + fadeOut(
    animationSpec = tween(
        durationMillis = durationMillis.ForOutgoing,
        delayMillis = 0,
        easing = FastOutLinearInEasing,
    )
)
