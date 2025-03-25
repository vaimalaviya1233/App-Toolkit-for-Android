package com.d4rk.android.apps.apptoolkit.app.home.domain.usecases

import com.d4rk.android.apps.apptoolkit.app.home.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.home.utils.constants.PlayStoreUrls
import com.d4rk.android.apps.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.apps.apptoolkit.core.utils.extensions.toError
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.RootError
import com.d4rk.android.libs.apptoolkit.core.domain.usecases.RepositoryWithoutParam
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class FetchDeveloperAppsUseCase(private val client : HttpClient) : RepositoryWithoutParam<Flow<DataState<List<AppInfo> , RootError>>> {

    override suspend operator fun invoke() : Flow<DataState<List<AppInfo> , RootError>> = flow {
        runCatching {
            val developerPageContent : String = client.get(urlString = PlayStoreUrls.DEVELOPER_PAGE) {
                headers {
                    append(name = HttpHeaders.UserAgent , value = PlayStoreUrls.DEFAULT_USER_AGENT)
                    append(name = HttpHeaders.AcceptLanguage , value = PlayStoreUrls.DEFAULT_LANGUAGE)
                }
                timeout { requestTimeoutMillis = 15000 }
            }.bodyAsText()

            val dataCallbackRegex = Regex(pattern = "AF_initDataCallback\\((.*?)\\);" , option = RegexOption.DOT_MATCHES_ALL)
            val dataCallbackJson = dataCallbackRegex.findAll(developerPageContent).firstNotNullOfOrNull { match ->
                val dataCallbackContent = match.groupValues[1]
                if (dataCallbackContent.contains("\"ds:3\"") || dataCallbackContent.contains("key: 'ds:3'")) {
                    Regex("data\\s*:\\s*(\\[.*?])\\s*,\\s*sideChannel" , RegexOption.DOT_MATCHES_ALL).find(dataCallbackContent)?.groupValues?.getOrNull(1)
                }
                else null
            } ?: throw Exception("Failed to extract JSON data from the page.")

            val dataContentJson : JsonElement = Json.parseToJsonElement(string = dataCallbackJson)
            val foundApps : List<AppInfo> = searchForApps(jsonElement = dataContentJson).sortedBy { it.name.lowercase() }
            foundApps
        }.onSuccess { foundApps ->
            emit(value = DataState.Success(data = foundApps))
        }.onFailure { error ->
            emit(value = DataState.Error(error = error.toError(default = Errors.UseCase.FAILED_TO_LOAD_APPS)))
        }
    }

    private fun searchForApps(jsonElement : JsonElement) : List<AppInfo> {
        val appInfos = mutableListOf<AppInfo>()
        val knownPackages = mutableSetOf<String>()

        fun JsonElement.flattenStrings() : List<String> = when (this) {
            is JsonPrimitive -> listOf(this.content)
            is JsonArray -> this.flatMap { it.flattenStrings() }
            is JsonObject -> this.values.flatMap { it.flattenStrings() }
            else -> emptyList()
        }

        fun traverse(jsonElement : JsonElement) {
            when (jsonElement) {
                is JsonArray -> {
                    val strings = jsonElement.flattenStrings()
                    val packageName = strings.firstOrNull { it.startsWith("com.") } ?: ""
                    if (packageName.isNotEmpty() && packageName !in knownPackages) {
                        val appIconUrl = strings.firstOrNull { it.startsWith("https://") } ?: PlayStoreUrls.DEFAULT_ICON_URL
                        val suggestedName = (jsonElement.getOrNull(3) as? JsonPrimitive)?.content?.takeIf { it != "null" && it.any(Char::isLetter) && it.length < 50 }
                        val alternativeName = jsonElement.flattenStrings().firstOrNull {
                            it != packageName && ! it.startsWith("https://") && it.any(Char::isLetter) && it.length < 50
                        }
                        val appName = suggestedName ?: alternativeName ?: packageName
                        appInfos.add(AppInfo(name = appName , iconUrl = appIconUrl , packageName = packageName))
                        knownPackages.add(packageName)
                    }
                    jsonElement.forEach { traverse(it) }
                }

                is JsonObject -> jsonElement.values.forEach { traverse(it) }
                else -> {}
            }
        }
        traverse(jsonElement)
        return appInfos
    }
}