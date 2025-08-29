package com.d4rk.android.apps.apptoolkit.app.main.domain.repository

import com.d4rk.android.libs.apptoolkit.core.domain.model.navigation.NavigationDrawerItem
import kotlinx.coroutines.flow.Flow

interface MainRepository {
    fun getNavigationDrawerItems(): Flow<List<NavigationDrawerItem>>
}

