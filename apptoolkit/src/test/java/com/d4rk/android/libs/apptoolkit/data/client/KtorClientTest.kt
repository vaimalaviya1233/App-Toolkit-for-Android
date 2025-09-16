package com.d4rk.android.libs.apptoolkit.data.client

import com.google.common.truth.Truth.assertThat
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.pluginOrNull
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KtorClientTest {

    @Serializable
    private data class SamplePayload(val id: Int)

    private val clientField = KtorClient::class.java.getDeclaredField("client").apply {
        isAccessible = true
    }

    private var engineProvider: (() -> MockEngine)? = null

    @BeforeEach
    fun setUp() {
        clearStoredClient()
        engineProvider = null
        mockkObject(Android)
        every { Android.create(any()) } answers {
            engineProvider?.invoke() ?: error("Mock engine not configured")
        }
    }

    @AfterEach
    fun tearDown() {
        clearStoredClient()
        unmockkObject(Android)
    }

    @Test
    fun `createClient returns shared instance`() {
        val engine = MockEngine { respond("{}", HttpStatusCode.OK) }
        engineProvider = { engine }

        val first = KtorClient.createClient()
        val second = KtorClient.createClient()

        assertThat(second).isSameInstanceAs(first)
        verify(exactly = 1) { Android.create(any()) }
    }

    @Test
    fun `client installs JSON negotiation and default headers`() = runTest {
        var capturedHeaders: Map<String, String>? = null
        var capturedBody: OutgoingContent? = null
        val jsonBody = """{"id":1,"unexpected":"value",}""""
        engineProvider = {
            MockEngine { request ->
                capturedHeaders = request.headers.entries().associate { it.key to it.value.joinToString(",") }
                capturedBody = request.body as? OutgoingContent
                respond(
                    content = ByteReadChannel(jsonBody),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Plain.toString())
                )
            }
        }

        val client = KtorClient.createClient()
        val payload = client.post("https://example.com/data") {
            setBody(SamplePayload(id = 1))
        }.body<SamplePayload>()

        assertThat(payload).isEqualTo(SamplePayload(id = 1))
        val headers = checkNotNull(capturedHeaders)
        assertThat(headers[HttpHeaders.Accept]).contains(ContentType.Application.Json.toString())
        val bodyContentType = (capturedBody as? TextContent)?.contentType
        checkNotNull(bodyContentType)
        assertThat(bodyContentType.contentType).isEqualTo(ContentType.Application.Json.contentType)
        assertThat(bodyContentType.contentSubtype).isEqualTo(ContentType.Application.Json.contentSubtype)
        assertThat(client.pluginOrNull(ContentNegotiation)).isNotNull()
    }

    @Test
    fun `client configures http timeouts`() {
        engineProvider = { MockEngine { respond("{}", HttpStatusCode.OK) } }

        val client = KtorClient.createClient()
        val pluginInstance = client.plugin(HttpTimeout)
        val configField = pluginInstance::class.java.getDeclaredField("config").apply { isAccessible = true }
        val timeoutConfig = configField.get(pluginInstance) as HttpTimeoutConfig

        assertThat(timeoutConfig.requestTimeoutMillis).isEqualTo(10_000)
        assertThat(timeoutConfig.connectTimeoutMillis).isEqualTo(10_000)
        assertThat(timeoutConfig.socketTimeoutMillis).isEqualTo(10_000)
    }

    @Test
    fun `logging plugin only installed when enabled`() {
        engineProvider = { MockEngine { respond("{}", HttpStatusCode.OK) } }
        val withLogging = KtorClient.createClient(enableLogging = true)
        assertThat(withLogging.pluginOrNull(Logging)).isNotNull()

        clearStoredClient()
        engineProvider = { MockEngine { respond("{}", HttpStatusCode.OK) } }
        val withoutLogging = KtorClient.createClient(enableLogging = false)
        assertThat(withoutLogging.pluginOrNull(Logging)).isNull()
    }

    private fun clearStoredClient() {
        val stored = clientField.get(KtorClient) as? HttpClient
        stored?.close()
        clientField.set(KtorClient, null)
    }
}
