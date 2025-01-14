package com.d4rk.android.libs.apptoolkit.ui.components.tooltips

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class , ExperimentalFoundationApi::class)
@Composable
fun TooltipIconButton(tooltipText : Int , onClick : () -> Unit, icon: ImageVector) {
    val tooltipPosition = TooltipDefaults.rememberPlainTooltipPositionProvider()
    val tooltipState = rememberBasicTooltipState(isPersistent = false)

    BasicTooltipBox(
        positionProvider = tooltipPosition,
        state = tooltipState,
        tooltip = {
            ElevatedCard {
                Text(
                    text = stringResource(id = tooltipText) ,
                    modifier = Modifier.padding(all = 2.dp) ,
                )
            }
        },
    ) {
        IconButton(
            onClick = onClick,
        ) {
            Icon(
                imageVector = icon ,
                contentDescription = null ,
            )
        }
    }
}