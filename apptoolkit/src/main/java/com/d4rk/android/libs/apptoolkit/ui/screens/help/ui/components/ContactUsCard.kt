package com.d4rk.android.libs.apptoolkit.ui.screens.help.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Support
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants

@Composable
fun ContactUsCard(onClick : () -> Unit) {
    Card(modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 12.dp))
            .clickable {
                onClick()
            }) {
        Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = SizeConstants.LargeSize) , verticalAlignment = Alignment.CenterVertically , horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Outlined.Support , contentDescription = null , modifier = Modifier.padding(end = SizeConstants.LargeSize))
            Column(
                modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
            ) {
                Text(text = stringResource(id = R.string.contact_us))
                Text(text = stringResource(id = R.string.contact_us_description))
            }
        }
    }
}