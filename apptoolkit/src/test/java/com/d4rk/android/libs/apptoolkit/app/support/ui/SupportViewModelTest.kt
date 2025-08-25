package com.d4rk.android.libs.apptoolkit.app.support.ui

import android.app.Activity
import app.cash.turbine.test
import com.android.billingclient.api.ProductDetails
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.support.billing.BillingRepository
import com.d4rk.android.libs.apptoolkit.app.support.billing.PurchaseResult
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.utils.dispatchers.UnconfinedDispatcherExtension
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class SupportViewModelTest {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = UnconfinedDispatcherExtension()
    }

    private val productDetailsFlow = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    private val purchaseResultFlow = MutableSharedFlow<PurchaseResult>()
    private val billingRepository = mockk<BillingRepository>(relaxed = true) {
        every { productDetails } returns productDetailsFlow
        every { purchaseResult } returns purchaseResultFlow
        every { queryProductDetails(any()) } returns Unit
        every { launchPurchaseFlow(any(), any()) } returns Unit
    }

    private fun createViewModel() = SupportViewModel(billingRepository)

    @Test
    fun `products update screenState to success with mapped list`() = runTest(dispatcherExtension.testDispatcher) {
        val p1 = mockk<ProductDetails>()
        val p2 = mockk<ProductDetails>()
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initial state
            productDetailsFlow.value = linkedMapOf("a" to p1, "b" to p2)
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)
            assertThat(state.data!!.products).containsExactly(p1, p2)
        }
    }

    @Test
    fun `pending purchase shows snackbar`() = runTest(dispatcherExtension.testDispatcher) {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initial
            purchaseResultFlow.emit(PurchaseResult.Pending)
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            val snackbar = state.snackbar!!
            assertThat(snackbar.isError).isFalse()
            val msg = snackbar.message as UiTextHelper.StringResource
            assertThat(msg.resourceId).isEqualTo(R.string.purchase_pending)
        }
    }

    @Test
    fun `success purchase shows thank you snackbar`() = runTest(dispatcherExtension.testDispatcher) {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()
            purchaseResultFlow.emit(PurchaseResult.Success)
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            val snackbar = state.snackbar!!
            assertThat(snackbar.isError).isFalse()
            val msg = snackbar.message as UiTextHelper.StringResource
            assertThat(msg.resourceId).isEqualTo(R.string.purchase_thank_you)
        }
    }

    @Test
    fun `failed purchase shows error state and snackbar`() = runTest(dispatcherExtension.testDispatcher) {
        val viewModel = createViewModel()
        val error = "boom"

        viewModel.uiState.test {
            awaitItem()
            purchaseResultFlow.emit(PurchaseResult.Failed(error))
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.screenState).isInstanceOf(ScreenState.Error::class.java)
            assertThat(state.data!!.error).isEqualTo(error)
            val snackbar = state.snackbar!!
            assertThat(snackbar.isError).isTrue()
            val msg = snackbar.message as UiTextHelper.DynamicString
            assertThat(msg.content).isEqualTo(error)
        }
    }

    @Test
    fun `user cancelled purchase shows snackbar`() = runTest(dispatcherExtension.testDispatcher) {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()
            purchaseResultFlow.emit(PurchaseResult.UserCancelled)
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            val snackbar = state.snackbar!!
            assertThat(snackbar.isError).isFalse()
            val msg = snackbar.message as UiTextHelper.StringResource
            assertThat(msg.resourceId).isEqualTo(R.string.purchase_cancelled)
        }
    }

    @Test
    fun `onDonateClicked delegates to billingRepository`() = runTest(dispatcherExtension.testDispatcher) {
        val viewModel = createViewModel()
        val activity = mockk<Activity>()
        val product = mockk<ProductDetails>()

        viewModel.onDonateClicked(activity, product)
        verify { billingRepository.launchPurchaseFlow(activity, product) }
    }
}

