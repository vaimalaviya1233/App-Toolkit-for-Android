package com.d4rk.android.libs.apptoolkit.core.ui.components.buttons.chip

import android.view.SoundEffectConstants
import android.view.View
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.bounceClick

@Composable
fun CommonFilterChip(selected : Boolean , onClick : () -> Unit , label : String , modifier : Modifier = Modifier , leadingIcon : (@Composable (() -> Unit))? = null) {
    val view : View = LocalView.current
    val interactionSource : MutableInteractionSource = remember { MutableInteractionSource() }

    FilterChip(
        selected = selected ,
        onClick = {
            view.playSoundEffect(SoundEffectConstants.CLICK)
            onClick()
        } ,
        label = { Text(text = label) } ,
        leadingIcon = {
            if (leadingIcon != null) {
                leadingIcon()
            }
            else {
                AnimatedContent(
                    targetState = selected,
                    transitionSpec = { SelectAllTransitions.fadeScale } , label = "Checkmark Animation"
                ) { targetChecked ->
                    if (targetChecked) {
                        Icon(imageVector = Icons.Filled.Check , contentDescription = null)
                    }
                }
            }
        } ,
        modifier = modifier.bounceClick() ,
        interactionSource = interactionSource ,
    )
}

object SelectAllTransitions {
    private const val DURATION = 300
    private val fadeScaleSpec = tween<Float>(DURATION)

    val fadeScale: ContentTransform by lazy {
        (fadeIn(animationSpec = fadeScaleSpec) + scaleIn(animationSpec = fadeScaleSpec))
                .togetherWith(fadeOut(animationSpec = fadeScaleSpec) + scaleOut(animationSpec = fadeScaleSpec))
    }
}