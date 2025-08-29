package com.d4rk.android.libs.apptoolkit.core.domain.model.ui

import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScreenStateExtensionsTest {

    data class Counter(var value: Int)

    private fun create(initial: Int = 0): MutableStateFlow<UiStateScreen<Counter>> {
        return MutableStateFlow(UiStateScreen(data = Counter(initial)))
    }

    @Test
    fun `updateData copyData and successData modify state`() = runTest {
        val state = create(5)

        state.updateData(ScreenState.Success()) { current -> Counter(current.value + 5) }
        assertEquals(10, state.value.data?.value)
        assert(state.value.screenState is ScreenState.Success)

        state.copyData { copy(value = value + 1) }
        assertEquals(11, state.value.data?.value)

        state.successData { copy(value = value * 2) }
        assertEquals(22, state.value.data?.value)
        assert(state.value.screenState is ScreenState.Success)
    }

    @Test
    fun `applyResult handles success error and loading`() = runTest {
        val state = create(2)

        state.applyResult(DataState.Success(3)) { data, current -> current.copy(value = current.value + data) }
        assertEquals(5, state.value.data?.value)
        assert(state.value.screenState is ScreenState.Success)

        state.applyResult(DataState.Error(error = Error("fail"))) { _, current -> current }
        assertEquals(1, state.value.errors.size)
        assert(state.value.screenState is ScreenState.Error)

        state.applyResult(DataState.Loading<Int, Error>()) { _, current -> current }
        assert(state.value.screenState is ScreenState.IsLoading)
    }

    @Test
    fun `snackbar and error helpers work`() = runTest {
        val state = create()
        val snackbar = UiSnackbar(message = UiTextHelper.DynamicString("hi"))

        state.setErrors(listOf(snackbar))
        assertEquals(1, state.getErrors().size)

        state.showSnackbar(snackbar)
        assertEquals("hi", (state.value.snackbar?.message as UiTextHelper.DynamicString).content)

        state.dismissSnackbar()
        assertEquals(null, state.value.snackbar)
    }

    @Test
    fun `getData returns current data or throws`() = runTest {
        val state = create(7)
        assertEquals(7, state.getData().value)

        state.setLoading()
        assert(state.value.screenState is ScreenState.IsLoading)

        val empty = MutableStateFlow(UiStateScreen<Counter>())
        assertThrows(IllegalStateException::class.java) {
            empty.getData()
        }
    }
}

