package com.d4rk.android.libs.apptoolkit.ui.components.modifiers

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * A modifier that adds haptic feedback when a drawer is swiped open or closed.
 *
 * This modifier uses the [DrawerState] to detect when a drawer is being opened or closed via swipe
 * gestures, and triggers a haptic feedback (long press) when the drawer animation starts.
 * The haptic feedback is only triggered once per animation, and is reset when the animation completes.
 *
 * @param drawerState The [DrawerState] of the drawer to monitor for swipe events.
 * @return A [Modifier] that applies the haptic feedback behavior.
 *
 * Example Usage:
 * ```
 * ModalNavigationDrawer(
 *      drawerState = drawerState,
 *      drawerContent = { ... },
 *      modifier = Modifier.hapticDrawerSwipe(drawerState)
 *  ) {
 *      // Content
 *  }
 * ```
 */
fun Modifier.hapticDrawerSwipe(drawerState : DrawerState) : Modifier = composed {
    val haptic : HapticFeedback = LocalHapticFeedback.current
    var hasVibrated : Boolean by remember { mutableStateOf(value = false) }

    LaunchedEffect(drawerState.currentValue , drawerState.targetValue) {
        if (drawerState.isAnimationRunning && ! hasVibrated) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            hasVibrated = true
        }

        if (! drawerState.isAnimationRunning) {
            hasVibrated = false
        }
    }

    return@composed this
}