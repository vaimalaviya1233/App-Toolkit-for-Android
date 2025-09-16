package com.d4rk.android.apps.apptoolkit.core.domain.model.network

import com.d4rk.android.apps.apptoolkit.core.domain.model.network.Errors as AppErrors
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors as LibErrors
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorsConsistencyTest {

    @Test
    fun `network error constants are shared between modules`() {
        assertEquals(enumNames<LibErrors.Network>(), enumNames<AppErrors.Network>())
    }

    @Test
    fun `use case error constants are shared between modules`() {
        assertEquals(enumNames<LibErrors.UseCase>(), enumNames<AppErrors.UseCase>())
    }

    @Test
    fun `database error constants are shared between modules`() {
        assertEquals(enumNames<LibErrors.Database>(), enumNames<AppErrors.Database>())
    }
}

private inline fun <reified T : Enum<T>> enumNames(): Set<String> =
    enumValues<T>().map { it.name }.toSet()
