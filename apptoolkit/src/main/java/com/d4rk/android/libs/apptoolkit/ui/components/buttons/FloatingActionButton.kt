package com.d4rk.android.libs.apptoolkit.ui.components.buttons

import android.view.SoundEffectConstants
import android.view.View
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import com.d4rk.android.libs.apptoolkit.ui.components.modifiers.bounceClick

/**
 * An animated extended floating action button that can be shown or hidden with a scaling and translation animation.
 *
 * @param visible Whether the button is currently visible. If false, the button will animate to a scaled and translated out state.
 * @param onClick The action to perform when the button is clicked.
 * @param icon The content of the button's icon slot.
 * @param text The content of the button's text slot, if any.
 * @param offsetX The horizontal offset for the button when it is hidden (not visible). Default is 50f.
 * @param offsetY The vertical offset for the button when it is hidden (not visible). Default is 50f.
 * @param scale The scale of the button when it is hidden (not visible). Default is 0.8f.
 * @param animationSpec The animation specification to use for the show/hide animation. Default is a 300ms tween with FastOutSlowInEasing.
 * @param expanded Whether the button is expanded or not. Default is true.
 * @param modifier Modifier for the button.
 */
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
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing) ,
        label = "FAB Scale"
    )

    val view : View = LocalView.current

    if (animatedScale > 0f) {
        ExtendedFloatingActionButton(
            onClick = {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                onClick()
            },
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