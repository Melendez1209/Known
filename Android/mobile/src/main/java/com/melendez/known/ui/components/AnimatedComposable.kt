package com.melendez.known.ui.components

import android.os.Build
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.melendez.known.ui.motion.materialSharedAxisXIn
import com.melendez.known.ui.motion.materialSharedAxisXOut

const val initialOffset = 0.10f
val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 1f, 1f)

// Configuration parameters for predictive back animation
const val DEFAULT_ANIMATION_DURATION = 380
const val DEFAULT_SCALE_FACTOR = 0.9f
const val DEFAULT_TRANSITION_OFFSET = 0.15f

fun NavGraphBuilder.animatedComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    // custom animation parameters
    animationDurationMillis: Int = DEFAULT_ANIMATION_DURATION,
    scaleFactorForPredictiveBack: Float = DEFAULT_SCALE_FACTOR, 
    offsetFactorForPredictiveBack: Float = DEFAULT_TRANSITION_OFFSET,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit,
) {
    if (Build.VERSION.SDK_INT >= 34) {
        animatedComposablePredictiveBack(
            route,
            arguments,
            deepLinks,
            animationDurationMillis,
            scaleFactorForPredictiveBack,
            offsetFactorForPredictiveBack,
            content
        )
    } else {
        animatedComposableLegacy(route, arguments, deepLinks, content)
    }
}

fun NavGraphBuilder.animatedComposablePredictiveBack(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    animationDurationMillis: Int = DEFAULT_ANIMATION_DURATION,
    scaleFactor: Float = DEFAULT_SCALE_FACTOR,
    offsetFactor: Float = DEFAULT_TRANSITION_OFFSET,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit,
) =
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = { 
            materialSharedAxisXIn(initialOffsetX = { (it * offsetFactor).toInt() },
                durationMillis = animationDurationMillis,
                isPredictiveBack = true)
        },
        exitTransition = {
            materialSharedAxisXOut(targetOffsetX = { -(it * initialOffset).toInt() },
                durationMillis = animationDurationMillis,
                isPredictiveBack = true)
        },
        popEnterTransition = {
            scaleIn(
                animationSpec = tween(durationMillis = animationDurationMillis, easing = EmphasizedDecelerate),
                initialScale = scaleFactor,
            ) + materialSharedAxisXIn(
                initialOffsetX = { -(it * initialOffset).toInt() },
                durationMillis = animationDurationMillis,
                isPredictiveBack = true
            )
        },
        popExitTransition = {
            materialSharedAxisXOut(
                targetOffsetX = { (it * initialOffset).toInt() },
                durationMillis = animationDurationMillis,
                isPredictiveBack = true
            ) + scaleOut(
                targetScale = scaleFactor,
                animationSpec = tween(durationMillis = animationDurationMillis, easing = EmphasizedAccelerate),
            )
        },
        content = content,
    )

fun NavGraphBuilder.animatedComposableLegacy(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit,
) =
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = {
            materialSharedAxisXIn(initialOffsetX = { (it * initialOffset).toInt() })
        },
        exitTransition = {
            materialSharedAxisXOut(targetOffsetX = { -(it * initialOffset).toInt() })
        },
        popEnterTransition = {
            materialSharedAxisXIn(initialOffsetX = { -(it * initialOffset).toInt() })
        },
        popExitTransition = {
            materialSharedAxisXOut(targetOffsetX = { (it * initialOffset).toInt() })
        },
        content = content,
    )