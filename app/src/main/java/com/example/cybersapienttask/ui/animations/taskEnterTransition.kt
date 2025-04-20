package com.example.cybersapienttask.ui.animations

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset

// Task slide-in animation
fun taskEnterTransition(delay: Int = 0): EnterTransition {
    return slideInHorizontally(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
            visibilityThreshold = IntOffset.VisibilityThreshold
        ),
        initialOffsetX = { fullWidth -> fullWidth },
    ) + fadeIn(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
}

// Task slide-out animation
fun taskExitTransition(): ExitTransition {
    return slideOutHorizontally(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
            visibilityThreshold = IntOffset.VisibilityThreshold
        ),
        targetOffsetX = { fullWidth -> -fullWidth }
    ) + fadeOut()
}

// Circular reveal animation for task details
fun circularRevealEnterTransition(): EnterTransition {
    return expandIn(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        expandFrom = androidx.compose.ui.Alignment.Center
    ) + fadeIn(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
}

// Circular conceal animation for exiting task details
fun circularConcealExitTransition(): ExitTransition {
    return shrinkOut(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        shrinkTowards = androidx.compose.ui.Alignment.Center
    ) + fadeOut()
}

// FAB bounce animation
fun fabBounceEnterTransition(): EnterTransition {
    return scaleIn(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ) + fadeIn()
}