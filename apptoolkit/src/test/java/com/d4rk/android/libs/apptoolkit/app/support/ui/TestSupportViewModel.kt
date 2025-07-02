package com.d4rk.android.libs.apptoolkit.app.support.ui

import com.android.billingclient.api.ProductDetails
import com.d4rk.android.libs.apptoolkit.core.MainDispatcherExtension
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
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
        val details = mapOf(
            "low" to ProductDetails.newBuilder().setProductId("low").build(),
            "high" to ProductDetails.newBuilder().setProductId("high").build()
        )
        val flow = flow {
            emit(DataState.Loading<Map<String, ProductDetails>, Errors>())
            emit(DataState.Success<Map<String, ProductDetails>, Errors>(details))
        }
        setup(flow = flow, testDispatcher = dispatcherExtension.testDispatcher)
        viewModel.onEvent(SupportEvent.QueryProductDetails(billingClient))
        viewModel.uiState.testSuccess(expectedSize = details.size, testDispatcher = dispatcherExtension.testDispatcher)
    }

    @Test
    fun `query product details error`() = runTest(dispatcherExtension.testDispatcher) {
        val flow = flow {
            emit(DataState.Loading<Map<String, ProductDetails>, Errors>())
            emit(DataState.Error<Map<String, ProductDetails>, Errors>(null, Errors.UseCase.FAILED_TO_LOAD_SKU_DETAILS))
        }
        setup(flow = flow, testDispatcher = dispatcherExtension.testDispatcher)
        viewModel.onEvent(SupportEvent.QueryProductDetails(billingClient))
        viewModel.uiState.testError(testDispatcher = dispatcherExtension.testDispatcher)
    }
}
