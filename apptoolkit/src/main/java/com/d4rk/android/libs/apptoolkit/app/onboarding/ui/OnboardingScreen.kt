package com.d4rk.android.libs.apptoolkit.app.onboarding.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.onboarding.data.repository.DefaultOnboardingRepository
import com.d4rk.android.libs.apptoolkit.app.onboarding.domain.data.model.ui.OnboardingPage
import com.d4rk.android.libs.apptoolkit.app.onboarding.ui.components.OnboardingBottomNavigation
import com.d4rk.android.libs.apptoolkit.app.onboarding.ui.components.pages.OnboardingDefaultPageLayout
import com.d4rk.android.libs.apptoolkit.app.onboarding.utils.interfaces.providers.OnboardingProvider
import com.d4rk.android.libs.apptoolkit.core.ui.components.buttons.OutlinedIconButtonWithText
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.hapticPagerSwipe
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen() {
    val context = LocalContext.current
    val onboardingProvider: OnboardingProvider = koinInject()
    val pages: List<OnboardingPage> = remember { onboardingProvider.getOnboardingPages(context = context) }
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val dispatchers: DispatcherProvider = koinInject()
    val repository = remember { DefaultOnboardingRepository(CommonDataStore.getInstance(context), dispatchers) }
    val viewModel: OnboardingViewModel = viewModel(factory = OnboardingViewModel.provideFactory(repository))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState: PagerState = rememberPagerState(initialPage = uiState.currentTabIndex) { pages.size }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.updateCurrentTab(pagerState.currentPage)
    }

    val onSkipRequested = {
        viewModel.completeOnboarding {
            onboardingProvider.onOnboardingFinished(context)
        }
    }

    Scaffold(
        topBar = {
            if (pages.isNotEmpty()) {
                TopAppBar(title = { }, actions = {
                    AnimatedVisibility(
                        visible = pagerState.currentPage < pages.size - 1,
                        enter = slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) + fadeIn(),
                        exit = slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }) + fadeOut()
                    ) {
                        OutlinedIconButtonWithText(
                            onClick = { onSkipRequested() },
                            icon = Icons.Filled.SkipNext,
                            iconContentDescription = stringResource(id = R.string.skip_button_content_description),
                            label = stringResource(id = R.string.skip_button_text)
                        )
                    }
                })
            }
        },
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
                            viewModel.completeOnboarding {
                                onboardingProvider.onOnboardingFinished(context)
                            }
                        }
                    },
                    onBackClicked = {
                        if (pagerState.currentPage > 0) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues: PaddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .hapticPagerSwipe(pagerState = pagerState)
                .padding(paddingValues = paddingValues)
        ) { pageIndex: Int ->
            when (val page = pages[pageIndex]) {
                is OnboardingPage.DefaultPage -> OnboardingDefaultPageLayout(page = page)
                is OnboardingPage.CustomPage -> page.content()
            }
        }
    }
}
