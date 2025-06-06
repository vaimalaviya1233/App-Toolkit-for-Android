package com.d4rk.android.apps.apptoolkit.app.apps.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.SoundEffectConstants
import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.d4rk.android.apps.apptoolkit.app.apps.domain.model.AppInfo
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.bounceClick
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.LargeVerticalSpacer
import com.d4rk.android.libs.apptoolkit.core.utils.constants.links.AppLinks
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.AppInfoHelper

@Composable
fun AppCard(appInfo : AppInfo , modifier : Modifier) {
    val context : Context = LocalContext.current
    val view: View = LocalView.current
    Card(modifier = modifier
            .bounceClick()
            .fillMaxSize()
            .aspectRatio(ratio = 1f)
            .clip(shape = RoundedCornerShape(size = SizeConstants.ExtraLargeSize))
            .clickable {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                if (appInfo.packageName.isNotEmpty()) {
                    if (AppInfoHelper().isAppInstalled(context = context , packageName = appInfo.packageName)) {
                        if (! AppInfoHelper().openApp(context = context , packageName = appInfo.packageName)) {
                            "${AppLinks.MARKET_APP_PAGE}${appInfo.packageName}".toUri().let { marketUri : Uri ->
                                context.startActivity(Intent(Intent.ACTION_VIEW , marketUri))
                            }
                        }
                    }
                    else {
                        "${AppLinks.MARKET_APP_PAGE}${appInfo.packageName}".toUri().let { marketUri : Uri ->
                            context.startActivity(Intent(Intent.ACTION_VIEW , marketUri))
                        }
                    }
                }
            }) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LargeVerticalSpacer()
            AsyncImage(
                model = appInfo.iconUrl , contentDescription = appInfo.name , modifier = Modifier
                        .size(size = SizeConstants.ExtraExtraLargeSize + SizeConstants.LargeSize + SizeConstants.SmallSize)
                        .clip(shape = RoundedCornerShape(size = SizeConstants.ExtraLargeSize)) , contentScale = ContentScale.Fit
            )
            LargeVerticalSpacer()
            Text(
                text = appInfo.name , modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SizeConstants.LargeSize) , textAlign = TextAlign.Center , fontWeight = FontWeight.Bold
            )
            LargeVerticalSpacer()
        }
    }
}