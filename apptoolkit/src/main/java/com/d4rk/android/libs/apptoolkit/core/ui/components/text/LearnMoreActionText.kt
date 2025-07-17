package com.d4rk.android.libs.apptoolkit.core.ui.components.text

import android.view.SoundEffectConstants
import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.bounceClick

/**
 * Displays a clickable text styled like a "Learn More" link.
 *
 * This composable mirrors the appearance of [LearnMoreText] but instead of
 * opening a URL it executes the provided [onClick] action when tapped.
 *
 * @param modifier Modifier applied to the text.
 * @param text The text to display. Defaults to the localized "Learn more".
 * @param onClick Callback invoked when the text is clicked.
 */
@Composable
fun LearnMoreActionText(modifier : Modifier = Modifier , text : String = stringResource(R.string.learn_more) , onClick : () -> Unit) {
    val view : View = LocalView.current
    val textColor : Color = MaterialTheme.colorScheme.primary
    val annotatedString : AnnotatedString = remember(key1 = text) {
        buildAnnotatedString {
            withStyle(style = SpanStyle(color = textColor , textDecoration = TextDecoration.Underline)) {
                append(text)
            }
        }
    }

    Text(
        text = annotatedString , modifier = modifier
            .bounceClick()
            .clickable {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                onClick()
            }
    )
}

