package com.d4rk.android.libs.apptoolkit.app.oboarding.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.ui.res.stringResource
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.oboarding.domain.data.model.ui.OnboardingPage
import com.d4rk.android.libs.apptoolkit.app.oboarding.ui.components.OnboardingBottomNavigation
import com.d4rk.android.libs.apptoolkit.app.oboarding.ui.components.pages.OnboardingDefaultPageLayout
import com.d4rk.android.libs.apptoolkit.app.oboarding.utils.interfaces.providers.OnboardingProvider
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.bounceClick
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
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
    val dataStore : CommonDataStore = CommonDataStore.getInstance(context = context)
    val onSkipRequested = {
        coroutineScope.launch {
            dataStore.saveStartup(isFirstTime = false)
        }
        onboardingProvider.onOnboardingFinished(context)
    }

    Scaffold(topBar = {
        if (pages.isNotEmpty()) {
            TopAppBar(title = { } , actions = {
                AnimatedVisibility(
                    visible = pagerState.currentPage < pages.size - 1 , enter = slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) + fadeIn() , exit = slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }) + fadeOut()
                ) {
                    TextButton(
                        onClick = onSkipRequested , modifier = Modifier.bounceClick()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext , contentDescription = stringResource(id = R.string.skip_button_content_description) , modifier = Modifier.size(SizeConstants.ButtonIconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(text = stringResource(id = R.string.skip_button_text))
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
                        dataStore.saveStartup(isFirstTime = false)
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