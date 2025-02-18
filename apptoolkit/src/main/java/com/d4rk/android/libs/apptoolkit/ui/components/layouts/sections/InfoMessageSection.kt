package com.d4rk.android.libs.apptoolkit.ui.components.layouts.sections

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.ui.components.modifiers.bounceClick
import com.d4rk.android.libs.apptoolkit.ui.components.spacers.MediumVerticalSpacer
import com.d4rk.android.libs.apptoolkit.utils.helpers.IntentsHelper

@Composable
fun InfoMessageSection(message : String , modifier : Modifier = Modifier , learnMoreText : String? = null , learnMoreUrl : String? = null) {
    val context : Context = LocalContext.current

    Column(modifier = modifier) {
        Icon(imageVector = Icons.Outlined.Info , contentDescription = stringResource(id = R.string.about))
        MediumVerticalSpacer()
        Text(text = message , style = MaterialTheme.typography.bodyMedium)

        if (! learnMoreText.isNullOrEmpty() && ! learnMoreUrl.isNullOrEmpty()) {
            val annotatedString : AnnotatedString = buildAnnotatedString {
                val startIndex = length
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary , textDecoration = TextDecoration.Underline)) {
                    append(learnMoreText)
                }
                val endIndex : Int = length

                addStringAnnotation(tag = "URL" , annotation = learnMoreUrl , start = startIndex , end = endIndex)
            }

            Text(text = annotatedString , modifier = Modifier
                    .bounceClick()
                    .clickable {
                        annotatedString.getStringAnnotations(
                            tag = "URL" , start = 0 , end = annotatedString.length
                        ).firstOrNull()?.let { annotation ->
                            IntentsHelper.openUrl(context = context , url = annotation.item)
                        }
                    })
        }
    }
}