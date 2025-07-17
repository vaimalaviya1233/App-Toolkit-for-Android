package com.d4rk.android.libs.apptoolkit.core.ui.components.text

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.bounceClick

@Composable
fun LearnMoreText(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.learn_more),
    onClick: () -> Unit
) {
    val textColor: Color = MaterialTheme.colorScheme.primary
    val annotatedString: AnnotatedString = remember(key1 = text) {
        buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = textColor,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(text)
            }
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier
            .bounceClick()
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
    )
}