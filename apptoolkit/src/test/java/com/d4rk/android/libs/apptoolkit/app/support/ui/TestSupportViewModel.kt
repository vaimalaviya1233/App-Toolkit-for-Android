package com.d4rk.android.libs.apptoolkit.app.support.ui

import com.android.billingclient.api.ProductDetails
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.support.domain.actions.SupportEvent
import com.d4rk.android.libs.apptoolkit.core.MainDispatcherExtension
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class TestSupportViewModel : TestSupportViewModelBase() {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = MainDispatcherExtension()
    }

    @Test
    fun `query product details success`() = runTest(dispatcherExtension.testDispatcher) {
        val low = mockk<ProductDetails>()
        every { low.productId } returns "low"
        val high = mockk<ProductDetails>()
        every { high.productId } returns "high"
        val details = mapOf(
            "low" to low,
            "high" to high
        )
        val flow = flow {
            emit(DataState.Loading<Map<String, ProductDetails>, Errors>())
            emit(DataState.Success<Map<String, ProductDetails>, Errors>(details))
        }
        setup(flow = flow, testDispatcher = dispatcherExtension.testDispatcher)

        viewModel.uiState.test {
            viewModel.onEvent(SupportEvent.QueryProductDetails(billingClient))

            val first = awaitItem()
            assertTrue(first.screenState is ScreenState.IsLoading)
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

            val second = awaitItem()
            assertTrue(second.screenState is ScreenState.Success)
            assertThat(second.data?.productDetails?.size).isEqualTo(details.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `query product details error`() = runTest(dispatcherExtension.testDispatcher) {
        val flow = flow {
            emit(DataState.Loading<Map<String, ProductDetails>, Errors>())
            emit(
                DataState.Error<Map<String, ProductDetails>, Errors>(
                    null,
                    Errors.UseCase.FAILED_TO_LOAD_SKU_DETAILS
                )
            )
        }
        setup(flow = flow, testDispatcher = dispatcherExtension.testDispatcher)

        viewModel.uiState.test {
            viewModel.onEvent(SupportEvent.QueryProductDetails(billingClient))

            val first = awaitItem()
            assertTrue(first.screenState is ScreenState.IsLoading)
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

            val second = awaitItem()
            assertTrue(second.screenState is ScreenState.Error)
            val msg = second.errors.first().message as UiTextHelper.StringResource
            assertThat(msg.resourceId).isEqualTo(R.string.error_failed_to_load_sku_details)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
