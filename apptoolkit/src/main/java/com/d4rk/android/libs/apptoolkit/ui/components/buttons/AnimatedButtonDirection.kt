package com.d4rk.android.libs.apptoolkit.ui.components.buttons

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import com.d4rk.android.libs.apptoolkit.ui.components.modifiers.bounceClick

@Composable
fun AnimatedExtendedFloatingActionButton(
    visible: Boolean = true,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    text: (@Composable () -> Unit)? = null,
    expanded: Boolean = true,
    modifier: Modifier = Modifier
) {
    val animatedScale : Float by animateFloatAsState(
        targetValue = if (visible) 1f else 0f ,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing) ,
        label = "FAB Scale"
    )

    if (animatedScale > 0f) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            icon = icon,
            text = text ?: {},
            expanded = expanded,
            modifier = modifier
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                        transformOrigin = TransformOrigin(pivotFractionX = 1f , pivotFractionY = 1f)
                    }
                    .bounceClick()
        )
    }
}