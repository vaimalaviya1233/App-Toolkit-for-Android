package com.d4rk.android.libs.apptoolkit.app.oboarding.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class , ExperimentalMaterial3Api::class)
@Composable
fun OnboardingBottomNavigation(
    pagerState : androidx.compose.foundation.pager.PagerState , pageCount : Int , onNextClicked : () -> Unit , onSkipClicked : () -> Unit
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface , // Or transparent
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp , vertical = 8.dp) , horizontalArrangement = Arrangement.SpaceBetween , verticalAlignment = Alignment.CenterVertically
        ) {
            if (pagerState.currentPage < pageCount - 1) {
                TextButton(onClick = onSkipClicked) {
                    Text("SKIP")
                }
            }
            else {
                Spacer(Modifier.weight(1f)) // Keep "DONE" button to the right
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp) , verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    Box(
                        modifier = Modifier
                                .size(8.dp)
                                .background(color , RoundedCornerShape(percent = 50))
                    )
                }
            }

            Button(onClick = onNextClicked) {
                Text(if (pagerState.currentPage < pageCount - 1) "NEXT" else "DONE")
            }
        }
    }
}