package com.d4rk.android.apps.apptoolkit.app.main.domain.repository

import com.d4rk.android.libs.apptoolkit.core.domain.model.navigation.NavigationDrawerItem

interface MainRepository {
    suspend fun getNavigationDrawerItems(): List<NavigationDrawerItem>
}

