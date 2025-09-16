package com.d4rk.android.libs.apptoolkit.core.domain.model.ui

import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ScreenStateExtensionsTest {

    @Test
    fun updateData_transformsDataAndSetsProvidedState() {
        val flow = createFlow(screenState = ScreenState.NoData())

        flow.updateData(newState = ScreenState.Success()) { current ->
            current.copy(counter = current.counter + 1, message = "updated")
        }

        val result = flow.value
        assertThat(result.screenState).isEqualTo(ScreenState.Success())
        assertThat(result.data).isEqualTo(TestUiData(counter = defaultData.counter + 1, message = "updated"))
    }

    @Test
    fun updateData_withNullDataOnlyChangesState() {
        val flow = createFlow(data = null, screenState = ScreenState.NoData())

        flow.updateData(newState = ScreenState.Error()) { current ->
            current.copy(counter = current.counter + 10)
        }

        val result = flow.value
        assertThat(result.screenState).isEqualTo(ScreenState.Error())
        assertThat(result.data).isNull()
    }

    @Test
    fun copyData_transformsDataWithoutChangingState() {
        val flow = createFlow(screenState = ScreenState.NoData())
        val originalState = flow.value.screenState

        flow.copyData {
            copy(counter = counter + 5, message = "copied")
        }

        val result = flow.value
        assertThat(result.screenState).isEqualTo(originalState)
        assertThat(result.data).isEqualTo(TestUiData(counter = defaultData.counter + 5, message = "copied"))
    }

    @Test
    fun successData_transformsDataAndSetsSuccess() {
        val flow = createFlow(screenState = ScreenState.NoData())

        flow.successData {
            copy(counter = counter * 2, message = "success")
        }

        val result = flow.value
        assertThat(result.screenState).isEqualTo(ScreenState.Success())
        assertThat(result.data).isEqualTo(TestUiData(counter = defaultData.counter * 2, message = "success"))
    }

    @Test
    fun successData_withNullDataLeavesDataNull() {
        val flow = createFlow(data = null, screenState = ScreenState.NoData())

        flow.successData {
            copy(counter = counter + 1)
        }

        val result = flow.value
        assertThat(result.screenState).isEqualTo(ScreenState.Success())
        assertThat(result.data).isNull()
    }

    @Test
    fun applyResult_success_updatesDataAndState() {
        val flow = createFlow(screenState = ScreenState.NoData())

        flow.applyResult(
            result = DataState.Success(data = 5),
            transform = { newData, current ->
                current.copy(counter = current.counter + newData, message = "from result")
            }
        )

        val result = flow.value
        assertThat(result.screenState).isEqualTo(ScreenState.Success())
        assertThat(result.data).isEqualTo(TestUiData(counter = defaultData.counter + 5, message = "from result"))
    }

    @Test
    fun applyResult_error_setsErrorStateAndSnackbar() {
        val flow = createFlow(screenState = ScreenState.NoData())
        val errorMessage = UiTextHelper.DynamicString("boom")

        flow.applyResult(
            result = DataState.Error<Unit, Error>(error = Error("fail")),
            errorMessage = errorMessage,
            transform = { _, current -> current }
        )

        val result = flow.value
        assertThat(result.screenState).isEqualTo(ScreenState.Error())
        assertThat(result.errors).hasSize(1)
        val snackbar = result.errors.single()
        assertThat(snackbar.message).isEqualTo(errorMessage)
        assertThat(snackbar.isError).isTrue()
        assertThat(result.data).isEqualTo(defaultData)
    }

    @Test
    fun applyResult_loading_setsLoadingState() {
        val flow = createFlow(screenState = ScreenState.Success())

        flow.applyResult(
            result = DataState.Loading<Unit, Error>(),
            transform = { _, current -> current }
        )

        val result = flow.value
        assertThat(result.screenState).isEqualTo(ScreenState.IsLoading())
    }

    @Test
    fun updateState_setsNewScreenState() {
        val flow = createFlow(screenState = ScreenState.NoData())

        flow.updateState(ScreenState.Error())

        assertThat(flow.value.screenState).isEqualTo(ScreenState.Error())
    }

    @Test
    fun setErrors_replacesErrorsList() {
        val flow = createFlow()
        val errors = listOf(testSnackbar("first"), testSnackbar("second", isError = false))

        flow.setErrors(errors)

        assertThat(flow.value.errors).isEqualTo(errors)
    }

    @Test
    fun showSnackbar_setsSnackbarOnState() {
        val flow = createFlow()
        val snackbar = testSnackbar("snackbar", isError = false)

        flow.showSnackbar(snackbar)

        assertThat(flow.value.snackbar).isEqualTo(snackbar)
    }

    @Test
    fun dismissSnackbar_clearsSnackbar() {
        val snackbar = testSnackbar("existing")
        val flow = createFlow(snackbar = snackbar)

        flow.dismissSnackbar()

        assertThat(flow.value.snackbar).isNull()
    }

    @Test
    fun setLoading_setsLoadingState() {
        val flow = createFlow(screenState = ScreenState.Success())

        flow.setLoading()

        assertThat(flow.value.screenState).isEqualTo(ScreenState.IsLoading())
    }

    @Test
    fun getData_returnsCurrentData() {
        val data = TestUiData(counter = 3, message = "custom")
        val flow = createFlow(data = data)

        assertThat(flow.getData()).isEqualTo(data)
    }

    @Test
    fun getData_throwsWhenDataMissing() {
        val flow = createFlow(data = null)

        assertThrows<IllegalStateException> {
            flow.getData()
        }
    }

    @Test
    fun getErrors_returnsCurrentErrors() {
        val errors = listOf(testSnackbar("problem"))
        val flow = createFlow(errors = errors)

        assertThat(flow.getErrors()).isEqualTo(errors)
    }

    private fun createFlow(
        data: TestUiData? = defaultData.copy(),
        screenState: ScreenState = ScreenState.IsLoading(),
        errors: List<UiSnackbar> = emptyList(),
        snackbar: UiSnackbar? = null
    ): MutableStateFlow<UiStateScreen<TestUiData>> {
        return MutableStateFlow(
            UiStateScreen(
                screenState = screenState,
                errors = errors,
                snackbar = snackbar,
                data = data
            )
        )
    }

    private fun testSnackbar(message: String, isError: Boolean = true): UiSnackbar {
        return UiSnackbar(
            message = UiTextHelper.DynamicString(message),
            isError = isError,
            timeStamp = 123L
        )
    }

    private data class TestUiData(
        val counter: Int,
        val message: String
    )

    private val defaultData = TestUiData(counter = 1, message = "initial")
}
