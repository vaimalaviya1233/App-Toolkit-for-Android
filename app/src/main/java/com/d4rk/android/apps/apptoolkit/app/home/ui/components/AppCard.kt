package com.d4rk.android.apps.apptoolkit.app.home.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.d4rk.android.apps.apptoolkit.app.home.domain.model.AppInfo
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.bounceClick
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.LargeVerticalSpacer
import com.d4rk.android.libs.apptoolkit.core.utils.constants.links.AppLinks
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants

@Composable
fun AppCard(appInfo : AppInfo , modifier : Modifier) {
    val context = LocalContext.current

    Card(modifier = modifier
            .bounceClick()
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = SizeConstants.ExtraLargeSize))
            .clickable {
                if (appInfo.packageName.isNotEmpty()) {
                    "${AppLinks.MARKET_APP_PAGE}${appInfo.packageName}".toUri().let { marketUri ->
                       Intent(Intent.ACTION_VIEW , marketUri).run {
                           runCatching { context.startActivity(this) }.getOrElse {
                               val webUri : Uri = "${AppLinks.PLAY_STORE_MAIN}${appInfo.packageName}".toUri()
                               context.startActivity(Intent(Intent.ACTION_VIEW , webUri))
                           }
                       }
                    }
                }
            }) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LargeVerticalSpacer()
            AsyncImage(
                model = appInfo.iconUrl , contentDescription = appInfo.name , modifier = Modifier
                        .size(size = 72.dp)
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