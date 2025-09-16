package com.d4rk.android.libs.apptoolkit.core.utils.constants.error

import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Error
import kotlin.test.Test
import kotlin.test.assertTrue

class ErrorTypeTest {

    @Test
    fun `all error types implement the marker interface`() {
        ErrorType.entries.forEach { errorType ->
            assertTrue(errorType is Error)
        }
    }
}
