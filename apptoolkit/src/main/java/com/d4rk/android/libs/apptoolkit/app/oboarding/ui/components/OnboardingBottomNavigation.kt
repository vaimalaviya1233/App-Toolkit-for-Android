package com.d4rk.android.libs.apptoolkit.app.oboarding.ui.components

import android.view.SoundEffectConstants
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.bounceClick

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingBottomNavigation(
    pagerState : PagerState , pageCount : Int , onNextClicked : () -> Unit , onBackClicked : () -> Unit
) {
    val view : View = LocalView.current

    BottomAppBar {
        Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp , vertical = 8.dp) , verticalAlignment = Alignment.CenterVertically , horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.weight(1f)) {
                this@Row.AnimatedVisibility(visible = pagerState.currentPage > 0 , modifier = Modifier.fillMaxWidth() , enter = slideInHorizontally(initialOffsetX = { - it } , animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy , stiffness = Spring.StiffnessLow
                )) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) , exit = slideOutHorizontally(targetOffsetX = { - it } , animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy , stiffness = Spring.StiffnessLow
                )) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))) {

                    Box(contentAlignment = Alignment.CenterStart) {
                        OutlinedButton(
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                onBackClicked()
                                      } , modifier = Modifier.bounceClick()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft , contentDescription = "Back" , modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("BACK")
                        }
                    }
                }
            }

            PageIndicatorDots(pagerState = pagerState , pageCount = pageCount , modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                        .weight(1f)
                        .animateContentSize() , contentAlignment = Alignment.CenterEnd
            ) {
                val isLastPage = pagerState.currentPage == pageCount - 1
                Button(
                    onClick = onNextClicked , modifier = Modifier.bounceClick()
                ) {
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