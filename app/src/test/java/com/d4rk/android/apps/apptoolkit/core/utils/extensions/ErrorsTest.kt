package com.d4rk.android.apps.apptoolkit.core.utils.extensions

import android.database.sqlite.SQLiteException
import com.d4rk.android.apps.apptoolkit.R as AppR
import com.d4rk.android.apps.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.R as ToolkitR
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import kotlinx.serialization.SerializationException
import org.junit.Test
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.sql.SQLException
import kotlin.test.assertEquals

class ErrorsTest {

    @Test
    fun `asUiText maps network and database issues to io message`() {
        val expected = FakeUiText.StringResource(ToolkitR.string.io_error)

        assertEquals(expected, Errors.Network.REQUEST_TIMEOUT.asUiText().toFake())
        assertEquals(expected, Errors.Network.NO_INTERNET.asUiText().toFake())
        assertEquals(expected, Errors.Database.DATABASE_OPERATION_FAILED.asUiText().toFake())
    }

    @Test
    fun `asUiText maps serialization and no data errors to initialization message`() {
        val expected = FakeUiText.StringResource(ToolkitR.string.initialization_error)

        assertEquals(expected, Errors.Network.SERIALIZATION.asUiText().toFake())
        assertEquals(expected, Errors.UseCase.NO_DATA.asUiText().toFake())
    }

    @Test
    fun `asUiText maps use case errors to specific text`() {
        assertEquals(
            FakeUiText.StringResource(AppR.string.error_failed_to_load_apps),
            Errors.UseCase.FAILED_TO_LOAD_APPS.asUiText().toFake(),
        )
        assertEquals(
            FakeUiText.StringResource(ToolkitR.string.illegal_argument_error),
            Errors.UseCase.ILLEGAL_ARGUMENT.asUiText().toFake(),
        )
    }

    @Test
    fun `asUiText falls back to unknown error for unhandled types`() {
        val unknownError = object : Errors {}

        assertEquals(
            FakeUiText.StringResource(ToolkitR.string.unknown_error),
            unknownError.asUiText().toFake(),
        )
    }

    @Test
    fun `toError maps known throwable types`() {
        assertEquals(Errors.Network.NO_INTERNET, UnknownHostException().toError())
        assertEquals(Errors.Network.NO_INTERNET, ConnectException().toError())
        assertEquals(Errors.Network.REQUEST_TIMEOUT, SocketTimeoutException().toError())
        assertEquals(Errors.Network.SERIALIZATION, SerializationException("boom").toError())
        assertEquals(Errors.Database.DATABASE_OPERATION_FAILED, SQLException().toError())
        assertEquals(Errors.Database.DATABASE_OPERATION_FAILED, SQLiteException().toError())
        assertEquals(Errors.UseCase.ILLEGAL_ARGUMENT, IllegalArgumentException().toError())
    }

    @Test
    fun `toError returns provided default for unknown throwable`() {
        val default = Errors.UseCase.FAILED_TO_LOAD_APPS

        assertEquals(default, IllegalStateException().toError(default))
    }

    private fun UiTextHelper.toFake() : FakeUiText {
        return when (this) {
            is UiTextHelper.DynamicString -> FakeUiText.DynamicString(content)
            is UiTextHelper.StringResource -> FakeUiText.StringResource(resourceId)
        }
    }

    private sealed interface FakeUiText {
        data class DynamicString(val value : String) : FakeUiText
        data class StringResource(val id : Int) : FakeUiText
    }
}
