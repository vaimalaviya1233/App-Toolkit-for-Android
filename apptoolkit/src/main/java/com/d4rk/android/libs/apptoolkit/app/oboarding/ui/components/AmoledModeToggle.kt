package com.d4rk.android.libs.apptoolkit.app.oboarding.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tonality
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.LargeHorizontalSpacer
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants

@Composable
fun AmoledModeToggle(
    isAmoledMode : Boolean , onCheckedChange : (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth() , shape = RoundedCornerShape(SizeConstants.LargeSize) , color = MaterialTheme.colorScheme.surfaceContainerHighest , tonalElevation = SizeConstants.ExtraSmallSize , shadowElevation = SizeConstants.ExtraSmallSize
    ) {
        Row(modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(! isAmoledMode) }
                .padding(horizontal = SizeConstants.MediumSize * 2 , vertical = SizeConstants.LargeIncreasedSize) , verticalAlignment = Alignment.CenterVertically , horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.amoled_mode) , style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold) , color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(id = R.string.onboarding_amoled_mode_desc) , style = MaterialTheme.typography.bodySmall , color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LargeHorizontalSpacer()
            Switch(checked = isAmoledMode , onCheckedChange = onCheckedChange , thumbContent = {
                Icon(
                    imageVector = Icons.Filled.Tonality , contentDescription = null , modifier = Modifier.size(SizeConstants.SwitchIconSize) , tint = if (isAmoledMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            } , colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary , checkedTrackColor = MaterialTheme.colorScheme.primaryContainer , uncheckedThumbColor = MaterialTheme.colorScheme.outline , uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            ))
        }
    }
}