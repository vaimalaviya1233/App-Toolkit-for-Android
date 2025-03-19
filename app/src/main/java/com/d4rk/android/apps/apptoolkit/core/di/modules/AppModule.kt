package com.d4rk.android.apps.apptoolkit.core.di.modules

import com.d4rk.android.apps.apptoolkit.app.main.ui.MainViewModel
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.d4rk.android.libs.apptoolkit.data.core.ads.AdsCoreManager
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule : Module = module {
    single<DataStore> { DataStore.getInstance(context = get()) }
    single<AdsCoreManager> { AdsCoreManager(context = get()) }

    viewModel {
        MainViewModel()
    }
}