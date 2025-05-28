package com.d4rk.android.apps.apptoolkit.app.tools.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.d4rk.android.apps.apptoolkit.app.tools.domain.data.model.ui.ToolItem
import com.d4rk.android.apps.apptoolkit.app.tools.ui.components.ToolListItem

@Composable
fun ToolsList(
    toolItems: List<ToolItem>,
    paddingValues: PaddingValues,
    onToolClick: (ToolItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
    ) {
        items(toolItems) { item ->
            ToolListItem(item = item, onClick = { onToolClick(item) }) // Pass the item
            HorizontalDivider()
        }
    }
}