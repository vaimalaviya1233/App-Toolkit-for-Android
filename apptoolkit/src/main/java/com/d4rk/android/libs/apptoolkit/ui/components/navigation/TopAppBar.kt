package com.d4rk.android.libs.apptoolkit.ui.components.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.d4rk.android.libs.apptoolkit.ui.components.buttons.AnimatedButtonDirection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LargeTopAppBarWithScaffold(title: String, onBackClicked: () -> Unit, actions: @Composable (RowScope.() -> Unit) = {}, floatingActionButton: @Composable (() -> Unit)? = null, content: @Composable (PaddingValues) -> Unit) {
    val scrollBehaviorState: TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(state = rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(connection = scrollBehaviorState.nestedScrollConnection) ,
        topBar = {
            LargeTopAppBar(
                title = { Text(text = title) },
                navigationIcon = {
                    AnimatedButtonDirection(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = com.d4rk.android.libs.apptoolkit.R.string.go_back),
                        onClick = { onBackClicked() }
                    )
                },
                actions = actions,
                scrollBehavior = scrollBehaviorState
            )
        } ,
        floatingActionButton = floatingActionButton ?: {} ,
    ) { paddingValues ->
        content(paddingValues)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarScaffold(title : String , content : @Composable (PaddingValues) -> Unit) {
    val scrollBehaviorState : TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(state = rememberTopAppBarState())

    Scaffold(modifier = Modifier.nestedScroll(connection = scrollBehaviorState.nestedScrollConnection) , topBar = {
        LargeTopAppBar(title = { Text(text = title) } , scrollBehavior = scrollBehaviorState)
    }) { paddingValues ->
        content(paddingValues)
    }
}