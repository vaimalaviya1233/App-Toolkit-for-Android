package com.d4rk.android.apps.apptoolkit.app.apps.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.d4rk.android.apps.apptoolkit.R
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.LargeVerticalSpacer
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.NavigationBarsVerticalSpacer
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.MediumVerticalSpacer
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants

@Composable
fun AppDetailsBottomSheet(
    appInfo: AppInfo,
    onShareClick: () -> Unit,
    onOpenInPlayStoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = SizeConstants.LargeSize, vertical = SizeConstants.ExtraLargeSize),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = appInfo.iconUrl,
            contentDescription = appInfo.name,
            modifier = Modifier.size(SizeConstants.ExtraExtraLargeSize * 2)
        )
        LargeVerticalSpacer()
        Text(
            text = appInfo.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        MediumVerticalSpacer()
        Text(
            text = appInfo.packageName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        LargeVerticalSpacer()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                space = SizeConstants.MediumSize,
                alignment = Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = stringResource(R.string.app_details_share_content_description)
                )
            }
            OutlinedButton(onClick = onOpenInPlayStoreClick) {
                Icon(
                    imageVector = Icons.Outlined.OpenInNew,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(SizeConstants.SmallSize))
                Text(text = stringResource(R.string.app_details_view_on_play_store))
            }
        }
        LargeVerticalSpacer()
        NavigationBarsVerticalSpacer()
    }
}

@Preview
private fun AppDetailsBottomSheetPreview() {
    AppDetailsBottomSheet(
        appInfo = AppInfo(
            name = "Example App",
            packageName = "com.example.app",
            iconUrl = ""
        ),
        onShareClick = {},
        onOpenInPlayStoreClick = {}
    )
}
