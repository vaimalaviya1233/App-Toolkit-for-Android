package com.d4rk.android.libs.apptoolkit.app.oboarding.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d4rk.android.libs.apptoolkit.app.oboarding.domain.data.model.ui.OnboardingPage
import com.d4rk.android.libs.apptoolkit.app.oboarding.ui.components.AnimatedMorphingShapeContainer
import com.d4rk.android.libs.apptoolkit.app.oboarding.ui.components.OnboardingBottomNavigation
import com.d4rk.android.libs.apptoolkit.app.oboarding.utils.interfaces.providers.OnboardingProvider
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
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
                            coroutineScope.launch {
                                CommonDataStore.getInstance(context = context).saveStartup(isFirstTime = false)
                            }
                            onboardingProvider.onOnboardingFinished(context)
                        }
                    },
                    onSkipClicked = {
                        coroutineScope.launch {
                            CommonDataStore.getInstance(context = context).saveStartup(isFirstTime = false)
                        }
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
            when (val page = pages[pageIndex]) {
                is OnboardingPage.DefaultPage -> OnboardingPageLayout(page = page)
                is OnboardingPage.CustomPage -> page.content()
            }
        }

    }
}

@Composable
fun OnboardingPageLayout(page: OnboardingPage.DefaultPage) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        AnimatedMorphingShapeContainer(page.imageVector)

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}