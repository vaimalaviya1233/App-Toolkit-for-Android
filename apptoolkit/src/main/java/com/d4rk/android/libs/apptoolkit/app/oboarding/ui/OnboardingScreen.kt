package com.d4rk.android.libs.apptoolkit.app.oboarding.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d4rk.android.libs.apptoolkit.app.oboarding.domain.data.model.ui.OnboardingPage
import com.d4rk.android.libs.apptoolkit.app.oboarding.ui.components.OnboardingBottomNavigation
import com.d4rk.android.libs.apptoolkit.app.oboarding.ui.components.RoundedPolygonShape
import com.d4rk.android.libs.apptoolkit.app.oboarding.utils.interfaces.providers.OnboardingProvider
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen() {
    val context = LocalContext.current
    val onboardingProvider: OnboardingProvider = koinInject()
    val pages = remember { onboardingProvider.getOnboardingPages(context) }
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState { pages.size }

    Scaffold(
        bottomBar = {
            if (pages.isNotEmpty()) {
                OnboardingBottomNavigation(
                    pagerState = pagerState,
                    pageCount = pages.size,
                    onNextClicked = {
                        if (pagerState.currentPage < pages.size - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onboardingProvider.onOnboardingFinished(context)
                        }
                    },
                    onSkipClicked = {
                        onboardingProvider.onOnboardingFinished(context)
                    }
                )
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
        ) { pageIndex ->
            val page = pages[pageIndex]
            OnboardingPageLayout(page = page)
        }
    }
}

@Composable
fun OnboardingPageLayout(page: OnboardingPage) {

    // Logic for the custom shape
    val customShape: Shape? = remember(page.shape) {
        page.shape?.let { RoundedPolygonShape(polygon = it) } // FIXME: Unresolved reference 'RoundedPolygonShape'.
    }

    Column(
        modifier = Modifier
                .fillMaxSize()
                .padding(16.dp) ,
        horizontalAlignment = Alignment.CenterHorizontally ,
        verticalArrangement = Arrangement.Center
    ) {

        if (customShape != null) {
            Box(
                modifier = Modifier
                        .size(200.dp) // Or dynamic size
                        .clip(customShape)
                        .background(MaterialTheme.colorScheme.secondary) // Color for the shape
                        .padding(16.dp), // Padding inside the shape
                contentAlignment = Alignment.Center
            ) {
                // You could put the icon or part of the title inside the shape
                Icon(
                    imageVector = page.imageVector ,
                    contentDescription = null ,
                    modifier = Modifier.size(80.dp) ,
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        } else {
            Icon(
                imageVector = page.imageVector,
                contentDescription = null,
                modifier = Modifier
                        .size(120.dp)
                        .padding(bottom = 32.dp),
            )
        }

        Text(
            text = page.title ,
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp) , // Expressive
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = page.description ,
            style = MaterialTheme.typography.bodyLarge ,
            textAlign = TextAlign.Center ,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}