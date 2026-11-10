package io.sortmyplate.network

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.sortmyplate.model.*
import kotlinx.serialization.json.Json

class OAuthNotConfiguredException(message: String) : IllegalStateException(message)

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
    private var sessionGoogleClientId: String? = null

    fun isBootstrapped(): Boolean = sessionBaseUrl != null && sessionEndpoints.isNotEmpty()

    fun getDiscoveredBaseUrl(): String = sessionBaseUrl ?: serverOrigin

    fun getDiscoveredEndpointCount(): Int = sessionEndpoints.size

    fun getDiscoveredGoogleClientId(): String? = sessionGoogleClientId

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
            sessionGoogleClientId = payload.googleClientId
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

    suspend fun getGoogleOAuthLoginUrl(
        state: String,
        platform: String,
        redirectTo: String? = null
    ): Result<OAuthLoginUrlResponse> {
        return runCatching {
            val response = client.get(resolveEndpoint("auth_google_login")) {
                parameter("state", state)
                parameter("platform", platform)
                if (!redirectTo.isNullOrBlank()) {
                    parameter("redirect_to", redirectTo)
                }
                parameter("response_mode", "json")
            }
            if (response.status == HttpStatusCode.NotImplemented) {
                throw OAuthNotConfiguredException(extractError(response))
            }
            if (!response.status.isSuccess()) {
                throw IllegalStateException(extractError(response))
            }
            response.body()
        }
    }

    suspend fun exchangeGoogleOAuth(
        state: String,
        exchangeCode: String?
    ): Result<AuthResponse> {
        return runCatching {
            val response = client.post(resolveEndpoint("auth_google_exchange")) {
                contentType(ContentType.Application.Json)
                setBody(OAuthExchangeRequest(state = state, exchangeCode = exchangeCode))
            }
            if (!response.status.isSuccess()) {
                throw IllegalStateException(extractError(response))
            }
            response.body()
        }
    }

    suspend fun pollGoogleOAuthSession(state: String): Result<OAuthSessionPollResponse> {
        return runCatching {
            val response = client.get(resolveEndpoint("auth_google_session", "oauth_state" to state))
            if (!response.status.isSuccess()) {
                throw IllegalStateException(extractError(response))
            }
            response.body()
        }
    }

    suspend fun signInWithGoogle(
        email: String? = null,
        name: String? = null,
        allergies: List<String>? = null,
        idToken: String? = null,
        googleAccessToken: String? = null
    ): Result<AuthResponse> {
        return runCatching {
            val response = client.post(resolveEndpoint("auth_google")) {
                contentType(ContentType.Application.Json)
                setBody(
                    GoogleAuthRequest(
                        idToken = idToken,
                        googleAccessToken = googleAccessToken,
                        email = email?.trim(),
                        name = name?.trim(),
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

    suspend fun updateProfile(
        token: String,
        allergies: List<String>,
        dietPreference: String
    ): Result<AuthResponse> {
        return runCatching {
            val response = client.put(resolveEndpoint("auth_profile")) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(
                    ProfileUpdateRequest(
                        allergies = allergies,
                        dietPreference = dietPreference
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
