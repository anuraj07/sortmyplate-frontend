package io.sortmyplate.network

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.sortmyplate.model.ApiErrorDetail
import io.sortmyplate.model.AuthResponse
import io.sortmyplate.model.BiomarkerPanelRecord
import io.sortmyplate.model.BiomarkerUploadRequest
import io.sortmyplate.model.BootstrapResponse
import io.sortmyplate.model.GoogleAuthRequest
import io.sortmyplate.model.GroupCreateRequest
import io.sortmyplate.model.GroupJoinRequest
import io.sortmyplate.model.GroupResponse
import io.sortmyplate.model.LlmStatusResponse
import io.sortmyplate.model.NextMealRequest
import io.sortmyplate.model.NextMealResponse
import kotlinx.serialization.json.Json

class SortMyPlateApi(
    private val serverOrigin: String = getServerOrigin()
) {
    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    val client = createPlatformHttpClient {
        install(ContentNegotiation) {
            json(jsonConfig)
        }
        installNetworkInspector()
    }

    // Session-scoped discovered endpoint map populated from GET /bootstrap
    private var sessionBaseUrl: String? = null
    private var sessionEndpoints: Map<String, String> = emptyMap()

    fun isBootstrapped(): Boolean = sessionBaseUrl != null && sessionEndpoints.isNotEmpty()

    fun getDiscoveredBaseUrl(): String = sessionBaseUrl ?: serverOrigin

    fun getDiscoveredEndpointCount(): Int = sessionEndpoints.size

    private fun resolveEndpoint(key: String, vararg replacements: Pair<String, String>): String {
        val base = sessionBaseUrl
            ?: throw IllegalStateException("Endpoints not discovered for this session. Call bootstrap() first.")
        var template = sessionEndpoints[key]
            ?: throw IllegalStateException("Endpoint '$key' missing from /bootstrap discovery payload.")

        replacements.forEach { (paramKey, paramValue) ->
            template = template.replace("{$paramKey}", paramValue)
        }

        if (template.startsWith("http://") || template.startsWith("https://")) {
            return template
        }
        val normalizedPath = if (template.startsWith("/")) template else "/$template"
        return "${base.trimEnd('/')}$normalizedPath"
    }

    private suspend fun extractError(response: HttpResponse): String {
        val rawBody = runCatching { response.bodyAsText() }.getOrNull().orEmpty()
        val parsed = runCatching { jsonConfig.decodeFromString<ApiErrorDetail>(rawBody).detail }.getOrNull()
        return parsed ?: "HTTP ${response.status.value}: ${rawBody.ifBlank { response.status.description }}"
    }

    suspend fun bootstrap(): Result<BootstrapResponse> {
        return runCatching {
            val cleanOrigin = serverOrigin.trimEnd('/')
            val response = client.get("$cleanOrigin/bootstrap")
            if (!response.status.isSuccess()) {
                throw IllegalStateException(extractError(response))
            }
            val payload: BootstrapResponse = response.body()
            if (payload.endpoints.isEmpty()) {
                throw IllegalStateException("Discovery endpoint returned an empty endpoint registry.")
            }

            var resolvedBase = payload.baseUrl.ifBlank { cleanOrigin }.trimEnd('/')
            if (cleanOrigin.startsWith("https://") && resolvedBase.startsWith("http://")) {
                resolvedBase = cleanOrigin
            }

            sessionBaseUrl = resolvedBase
            sessionEndpoints = payload.endpoints
            payload.copy(baseUrl = resolvedBase)
        }
    }

    suspend fun getLlmStatus(): Result<LlmStatusResponse> {
        return runCatching {
            val response = client.get(resolveEndpoint("llm_status"))
            if (!response.status.isSuccess()) {
                throw IllegalStateException(extractError(response))
            }
            response.body()
        }
    }

    suspend fun signInWithGoogle(
        email: String,
        name: String,
        allergies: List<String> = emptyList(),
        idToken: String? = null
    ): Result<AuthResponse> {
        return runCatching {
            val response = client.post(resolveEndpoint("auth_google")) {
                contentType(ContentType.Application.Json)
                setBody(
                    GoogleAuthRequest(
                        idToken = idToken,
                        email = email.trim(),
                        name = name.trim(),
                        allergies = allergies
                    )
                )
            }
            if (!response.status.isSuccess()) {
                throw IllegalStateException(extractError(response))
            }
            response.body()
        }
    }

    suspend fun getLatestBiomarkers(token: String): Result<BiomarkerPanelRecord?> {
        return runCatching {
            val response = client.get(resolveEndpoint("biomarkers_latest")) {
                header("Authorization", "Bearer $token")
            }
            if (response.status == HttpStatusCode.NotFound) {
                return@runCatching null
            }
            if (!response.status.isSuccess()) {
                throw IllegalStateException(extractError(response))
            }
            response.body<BiomarkerPanelRecord>()
        }
    }

    suspend fun recordBiomarkers(
        token: String,
        request: BiomarkerUploadRequest
    ): Result<BiomarkerPanelRecord> {
        return runCatching {
            val response = client.post(resolveEndpoint("biomarkers_record")) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(request)
            }
            if (!response.status.isSuccess()) {
                throw IllegalStateException(extractError(response))
            }
            response.body()
        }
    }

    suspend fun getMyGroups(token: String): Result<List<GroupResponse>> {
        return runCatching {
            val response = client.get(resolveEndpoint("groups_my")) {
                header("Authorization", "Bearer $token")
            }
            if (!response.status.isSuccess()) {
                throw IllegalStateException(extractError(response))
            }
            response.body()
        }
    }

    suspend fun createGroup(token: String, name: String): Result<GroupResponse> {
        return runCatching {
            val response = client.post(resolveEndpoint("groups_create")) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(GroupCreateRequest(name = name.trim()))
            }
            if (!response.status.isSuccess()) {
                throw IllegalStateException(extractError(response))
            }
            response.body()
        }
    }

    suspend fun joinGroup(token: String, inviteCode: String): Result<GroupResponse> {
        return runCatching {
            val response = client.post(resolveEndpoint("groups_join")) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(GroupJoinRequest(inviteCode = inviteCode.trim()))
            }
            if (!response.status.isSuccess()) {
                throw IllegalStateException(extractError(response))
            }
            response.body()
        }
    }

    suspend fun leaveGroup(token: String, groupId: String): Result<Unit> {
        return runCatching {
            val response = client.post(resolveEndpoint("groups_leave", "group_id" to groupId)) {
                header("Authorization", "Bearer $token")
            }
            if (!response.status.isSuccess()) {
                throw IllegalStateException(extractError(response))
            }
        }
    }

    suspend fun getNextMeal(
        token: String,
        groupId: String? = null,
        activeMemberIds: List<String>? = null,
        mealSlot: String = "DINNER"
    ): Result<NextMealResponse> {
        return runCatching {
            val response = client.post(resolveEndpoint("meals_next")) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(
                    NextMealRequest(
                        groupId = groupId,
                        activeMemberIds = activeMemberIds,
                        mealSlot = mealSlot
                    )
                )
            }
            if (!response.status.isSuccess()) {
                throw IllegalStateException(extractError(response))
            }
            response.body()
        }
    }
}
