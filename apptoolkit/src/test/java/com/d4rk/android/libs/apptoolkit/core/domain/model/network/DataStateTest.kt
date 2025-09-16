package com.d4rk.android.libs.apptoolkit.core.domain.model.network

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

private class SampleRootError : Error("Sample root error")

class DataStateTest {

    @Test
    fun `success state holds provided data`() {
        val data = "payload"

        val state = DataState.Success<String, SampleRootError>(data)

        assertEquals(data, state.data)
    }

    @Test
    fun `error state exposes data and error`() {
        val data = 42
        val error = SampleRootError()

        val state = DataState.Error(data, error)

        assertEquals(data, state.data)
        assertSame(error, state.error)
    }

    @Test
    fun `loading state exposes optional data`() {
        val expected = listOf("one", "two")

        val state = DataState.Loading<List<String>, SampleRootError>(expected)

        assertEquals(expected, state.data)

        val emptyState = DataState.Loading<String, SampleRootError>()

        assertNull(emptyState.data)
    }

    @Test
    fun `onSuccess invokes callback only for success state`() {
        val data = "complete"
        val successState = DataState.Success<String, SampleRootError>(data)
        var received: String? = null

        val returnedSuccess = successState.onSuccess {
            received = it
        }

        assertEquals(data, received)
        assertSame(successState, returnedSuccess)

        val errorState: DataState<String, SampleRootError> =
            DataState.Error(data = null, error = SampleRootError())
        var invoked = false

        val returnedError = errorState.onSuccess {
            invoked = true
        }

        assertFalse(invoked)
        assertSame(errorState, returnedError)
    }

    @Test
    fun `onLoading invokes callback only for loading state`() {
        val data = "loading"
        val loadingState = DataState.Loading<String, SampleRootError>(data)
        var received: String? = null

        val returnedLoading = loadingState.onLoading {
            received = it
        }

        assertEquals(data, received)
        assertSame(loadingState, returnedLoading)

        val successState: DataState<String, SampleRootError> =
            DataState.Success<String, SampleRootError>(data)
        var invoked = false

        val returnedSuccess = successState.onLoading {
            invoked = true
        }

        assertFalse(invoked)
        assertSame(successState, returnedSuccess)
    }
}
