package com.d4rk.android.libs.apptoolkit.app.oboarding.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.bounceClick

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingBottomNavigation(
    pagerState : PagerState , pageCount : Int , onNextClicked : () -> Unit , onBackClicked : () -> Unit
) {
    BottomAppBar {
        Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp , vertical = 8.dp)
                    .animateContentSize() , verticalAlignment = Alignment.CenterVertically , horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                        .weight(1f)
                        .animateContentSize() , contentAlignment = Alignment.CenterStart
            ) {

                if (pagerState.currentPage > 0) {
                    OutlinedButton(
                        onClick = onBackClicked , modifier = Modifier
                                .animateContentSize()
                                .bounceClick()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft , contentDescription = "Back" , modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("BACK")
                    }
                }
                else {

                    Spacer(Modifier.animateContentSize())
                }
            }

            PageIndicatorDots(
                pagerState = pagerState , pageCount = pageCount , modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                        .weight(1f)
                        .animateContentSize() , contentAlignment = Alignment.CenterEnd
            ) {
                Button(
                    onClick = onNextClicked , modifier = Modifier
                            .animateContentSize()
                            .bounceClick()
                ) {
                    val isLastPage = pagerState.currentPage == pageCount - 1
                    if (isLastPage) {
                        Icon(
                            imageVector = Icons.Filled.Check , contentDescription = "Done" , modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("DONE")
                    }
                    else {
                        Text("NEXT")
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight , contentDescription = "Next" , modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                    }
                }
            }
        }
    }
}