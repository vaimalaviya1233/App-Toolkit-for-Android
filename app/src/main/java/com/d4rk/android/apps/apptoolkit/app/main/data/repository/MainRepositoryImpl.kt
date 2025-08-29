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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class MainRepositoryImpl(
    private val ioDispatcher: CoroutineDispatcher
) : MainRepository {
    override fun getNavigationDrawerItems(): Flow<List<NavigationDrawerItem>> =
        flow {
            emit(
                listOf(
                    NavigationDrawerItem(
                        title = R.string.settings, selectedIcon = Icons.Outlined.Settings
                    ),
                    NavigationDrawerItem(
                        title = R.string.help_and_feedback, selectedIcon = Icons.AutoMirrored.Outlined.HelpOutline
                    ),
                    NavigationDrawerItem(
                        title = R.string.updates, selectedIcon = Icons.AutoMirrored.Outlined.EventNote
                    ),
                    NavigationDrawerItem(
                        title = R.string.share, selectedIcon = Icons.Outlined.Share
                    )
                )
            )
        }.flowOn(ioDispatcher)
}

