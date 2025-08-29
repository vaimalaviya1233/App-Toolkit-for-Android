package com.d4rk.android.apps.apptoolkit.app.main.data.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import com.d4rk.android.apps.apptoolkit.app.main.domain.repository.MainRepository
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.domain.model.navigation.NavigationDrawerItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class MainRepositoryImpl(
    private val ioDispatcher: CoroutineDispatcher
) : MainRepository {
    override suspend fun getNavigationDrawerItems(): List<NavigationDrawerItem> =
        withContext(ioDispatcher) {
            listOf(
                NavigationDrawerItem(
                    title = R.string.settings, selectedIcon = Icons.Outlined.Settings
                ), NavigationDrawerItem(
                    title = R.string.help_and_feedback, selectedIcon = Icons.AutoMirrored.Outlined.HelpOutline
                ), NavigationDrawerItem(
                    title = R.string.updates, selectedIcon = Icons.AutoMirrored.Outlined.EventNote
                ), NavigationDrawerItem(
                    title = R.string.share, selectedIcon = Icons.Outlined.Share
                )
            )
        }
}

