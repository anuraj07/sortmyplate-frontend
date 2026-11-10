package io.sortmyplate.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.sortmyplate.model.AuthResponse
import io.sortmyplate.model.BiomarkerPanelRecord
import io.sortmyplate.model.GoogleDevLoginRequest
import io.sortmyplate.model.GroupResponse
import io.sortmyplate.model.NextMealRequest
import io.sortmyplate.model.NextMealResponse
import kotlinx.serialization.json.Json

class SortMyPlateApi(
    private val baseUrl: String = getApiBaseUrl()
) {
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
    }

    suspend fun loginAsDevUser(email: String = "alex.demo@gmail.com", name: String = "Alex Demo"): Result<AuthResponse> {
        return runCatching {
            client.post("$baseUrl/auth/google/dev-login") {
                contentType(ContentType.Application.Json)
                setBody(GoogleDevLoginRequest(email = email, fullName = name))
            }.body()
        }
    }

    suspend fun getLatestBiomarkers(token: String): Result<BiomarkerPanelRecord> {
        return runCatching {
            client.get("$baseUrl/health/biomarkers/latest") {
                header("Authorization", "Bearer $token")
            }.body()
        }
    }

    suspend fun getMyGroups(token: String): Result<List<GroupResponse>> {
        return runCatching {
            client.get("$baseUrl/groups/my") {
                header("Authorization", "Bearer $token")
            }.body()
        }
    }

    suspend fun getNextMeal(
        token: String,
        groupId: String? = null,
        activeMemberIds: List<String>? = null,
        mealSlot: String = "DINNER"
    ): Result<NextMealResponse> {
        return runCatching {
            client.post("$baseUrl/meals/next") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(
                    NextMealRequest(
                        groupId = groupId,
                        activeMemberIds = activeMemberIds,
                        mealSlot = mealSlot
                    )
                )
            }.body()
        }
    }
}
