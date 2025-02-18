package com.d4rk.android.libs.apptoolkit.ui.components.buttons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.d4rk.android.libs.apptoolkit.ui.components.modifiers.bounceClick

@Composable
fun AnimatedButtonDirection(
    modifier : Modifier = Modifier , visible : Boolean = true , icon : ImageVector , contentDescription : String? , onClick : () -> Unit , durationMillis : Int = 500 , autoAnimate : Boolean = true , fromRight : Boolean = false
) {
    val animatedVisibility : MutableState<Boolean> = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (autoAnimate) {
            animatedVisibility.value = true
        }
    }

    AnimatedVisibility(
        visible = if (autoAnimate) animatedVisibility.value else visible ,
        enter = fadeIn(animationSpec = tween(durationMillis = durationMillis)) + slideInHorizontally(initialOffsetX = { if (fromRight) it else - it } , animationSpec = tween(durationMillis = durationMillis)) ,
        exit = fadeOut(animationSpec = tween(durationMillis = durationMillis)) + slideOutHorizontally(targetOffsetX = { if (fromRight) it else - it } , animationSpec = tween(durationMillis = durationMillis))
    ) {
        IconButton(
            modifier = modifier.bounceClick() , onClick = onClick
        ) {
            Icon(imageVector = icon , contentDescription = contentDescription)
        }
    }
}