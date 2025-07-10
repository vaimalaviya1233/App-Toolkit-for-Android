package com.d4rk.android.libs.apptoolkit.core.ui.components.buttons

import android.view.SoundEffectConstants
import android.view.View
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.bounceClick
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.ButtonIconSpacer
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants

@Composable
fun IconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    iconContentDescription: String? = null,
    icon: ImageVector? = null,
    painter: Painter? = null
) {
    val view: View = LocalView.current

    IconButton(onClick = {
        view.playSoundEffect(SoundEffectConstants.CLICK)
        onClick()
    }, enabled = enabled, modifier = modifier.bounceClick()) {
        icon?.let {
            Icon(
                modifier = Modifier.size(size = SizeConstants.ButtonIconSize),
                imageVector = it,
                contentDescription = iconContentDescription
            )
        } ?: painter?.let {
            Icon(
                modifier = Modifier.size(size = SizeConstants.ButtonIconSize),
                painter = it,
                contentDescription = iconContentDescription)
        }
    }
}

@Composable
fun IconButtonWithText(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    iconContentDescription: String? = null,
    label: String,
    icon: ImageVector? = null,
    painter: Painter? = null
) {
    val view: View = LocalView.current

    Button(onClick = {
        view.playSoundEffect(SoundEffectConstants.CLICK)
        onClick()
    }, enabled = enabled, modifier = modifier.bounceClick()) {
        icon?.let {
            Icon(
                modifier = Modifier.size(size = SizeConstants.ButtonIconSize),
                imageVector = it,
                contentDescription = iconContentDescription
            )
        } ?: painter?.let {
            Icon(
                modifier = Modifier.size(size = SizeConstants.ButtonIconSize),
                painter = it,
                contentDescription = iconContentDescription)
        }
        ButtonIconSpacer()
        Text(text = label)
    }
}

@Composable
fun TonalIconButtonWithText(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    iconContentDescription: String? = null,
    label: String,
    icon: ImageVector? = null,
    painter: Painter? = null
) {
    val view: View = LocalView.current

    FilledTonalButton(onClick = {
        view.playSoundEffect(SoundEffectConstants.CLICK)
        onClick()
    }, enabled = enabled, modifier = modifier.bounceClick()) {
        icon?.let {
            Icon(
                modifier = Modifier.size(size = SizeConstants.ButtonIconSize),
                imageVector = it,
                contentDescription = iconContentDescription
            )
        } ?: painter?.let {
            Icon(
                modifier = Modifier.size(size = SizeConstants.ButtonIconSize),
                painter = it,
                contentDescription = iconContentDescription)
        }
        ButtonIconSpacer()
        Text(text = label)
    }
}