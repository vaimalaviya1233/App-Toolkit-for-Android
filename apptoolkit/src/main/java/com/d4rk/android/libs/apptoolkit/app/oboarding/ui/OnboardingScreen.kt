package com.d4rk.android.libs.apptoolkit.app.oboarding.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.d4rk.android.libs.apptoolkit.app.oboarding.domain.data.model.ui.OnboardingPage
import com.d4rk.android.libs.apptoolkit.app.oboarding.ui.components.OnboardingBottomNavigation
import com.d4rk.android.libs.apptoolkit.app.oboarding.ui.components.pages.OnboardingDefaultPageLayout
import com.d4rk.android.libs.apptoolkit.app.oboarding.utils.interfaces.providers.OnboardingProvider
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.bounceClick
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalFoundationApi::class , ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen() {
    val context = LocalContext.current
    val onboardingProvider : OnboardingProvider = koinInject()
    val pages = remember { onboardingProvider.getOnboardingPages(context) }
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState { pages.size }
    val onSkipRequested = {
        coroutineScope.launch {
            CommonDataStore.getInstance(context = context).saveStartup(isFirstTime = false)
        }
        onboardingProvider.onOnboardingFinished(context)
    }

    Scaffold(topBar = {
        if (pages.isNotEmpty()) {
            TopAppBar(title = { } , actions = {
                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(
                        onClick = onSkipRequested , modifier = Modifier
                                .animateContentSize()
                                .bounceClick()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext , contentDescription = "Skip" , modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("SKIP")
                    }
                }
            })
        }
    } , bottomBar = {
        if (pages.isNotEmpty()) {
            OnboardingBottomNavigation(pagerState = pagerState , pageCount = pages.size , onNextClicked = {
                if (pagerState.currentPage < pages.size - 1) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
                else {
                    coroutineScope.launch {
                        CommonDataStore.getInstance(context = context).saveStartup(isFirstTime = false)
                    }
                    onboardingProvider.onOnboardingFinished(context)
                }
            } , onBackClicked = {
                if (pagerState.currentPage > 0) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                }
            })
        }
    }) { paddingValues ->
        HorizontalPager(
            state = pagerState , modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
        ) { pageIndex ->
            when (val page = pages[pageIndex]) {
                is OnboardingPage.DefaultPage -> OnboardingDefaultPageLayout(page = page)
                is OnboardingPage.CustomPage -> page.content()
            }
        }
    }
}