package com.d4rk.android.apps.apptoolkit.app.apps.list.ui.components

import com.d4rk.android.apps.apptoolkit.app.apps.common.screens.buildAppListItems
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppListItem
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuildAppListItemsTest {

    @Test
    fun `buildAppListItems inserts ads at configured frequency`() {
        val apps = (1..8).map {
            AppInfo(
                name = "App$it",
                packageName = "pkg$it",
                iconUrl = "icon$it",
                description = "Description $it",
                screenshots = emptyList(),
            )
        }
        val items = buildAppListItems(apps, adsEnabled = true, adFrequency = 4)

        assertEquals(10, items.size)
        assertTrue(items[4] is AppListItem.Ad)
        assertTrue(items[9] is AppListItem.Ad)
    }

    @Test
    fun `buildAppListItems adds trailing ad when apps count not multiple of frequency`() {
        val apps = (1..5).map {
            AppInfo(
                name = "App$it",
                packageName = "pkg$it",
                iconUrl = "icon$it",
                description = "Description $it",
                screenshots = emptyList(),
            )
        }
        val items = buildAppListItems(apps, adsEnabled = true, adFrequency = 4)

        assertEquals(7, items.size)
        assertTrue(items[4] is AppListItem.Ad)
        assertTrue(items[6] is AppListItem.Ad)
    }

    @Test
    fun `buildAppListItems returns only apps when ads disabled`() {
        val apps = (1..5).map {
            AppInfo(
                name = "App$it",
                packageName = "pkg$it",
                iconUrl = "icon$it",
                description = "Description $it",
                screenshots = emptyList(),
            )
        }
        val items = buildAppListItems(apps, adsEnabled = false, adFrequency = 4)

        assertEquals(apps.map { AppListItem.App(it) }, items)
    }
}
