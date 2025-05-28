package com.d4rk.android.apps.apptoolkit.app.tools.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d4rk.android.apps.apptoolkit.app.tools.domain.data.model.ui.ToolItem

@Composable
fun ToolListItem(item : ToolItem , onClick : () -> Unit) {
    Row(
        modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp , vertical = 12.dp) , verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(item.iconBackgroundColor) , contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon , contentDescription = item.title , tint = Color.White , modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = item.title , fontWeight = FontWeight.Medium , fontSize = 16.sp , color = Color.Black
            )
            Text(
                text = item.subtitle , fontSize = 14.sp , color = Color.Gray
            )
        }
    }
}