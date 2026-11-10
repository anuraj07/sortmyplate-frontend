package io.sortmyplate.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BootstrapResponse(
    val service: String = "SortMyPlate",
    val version: String = "1.0.0",
    @SerialName("base_url") val baseUrl: String = "",
    @SerialName("google_client_id") val googleClientId: String? = null,
    @SerialName("google_oauth_enabled") val googleOauthEnabled: Boolean = false,
    @SerialName("google_gis_enabled") val googleGisEnabled: Boolean = false,
    val endpoints: Map<String, String> = emptyMap()
)

@Serializable
data class LlmStatusResponse(
    val provider: String = "mock",
    val status: String = "ONLINE",
    @SerialName("latency_ms") val latencyMs: Double? = null
)

@Serializable
data class ApiErrorDetail(
    val detail: String = "Request failed"
)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String = "",
    @SerialName("token_type") val tokenType: String = "bearer",
    @SerialName("user_id") val userId: String = "",
    val email: String = "",
    @SerialName("full_name") val fullName: String = "",
    @SerialName("picture_url") val pictureUrl: String? = null,
    @SerialName("subject_token") val subjectToken: String = "",
    val allergies: List<String> = emptyList(),
    @SerialName("diet_preference") val dietPreference: String = "omnivore"
)

@Serializable
data class GoogleAuthRequest(
    @SerialName("id_token") val idToken: String? = null,
    @SerialName("google_access_token") val googleAccessToken: String? = null,
    val email: String? = null,
    val name: String? = null,
    val picture: String? = null,
    val allergies: List<String>? = null,
    @SerialName("diet_preference") val dietPreference: String? = null
)

@Serializable
data class OAuthLoginUrlResponse(
    @SerialName("auth_url") val authUrl: String = "",
    val state: String = "",
    @SerialName("callback_uri") val callbackUri: String = ""
)

@Serializable
data class OAuthExchangeRequest(
    val state: String,
    @SerialName("exchange_code") val exchangeCode: String? = null
)

@Serializable
data class OAuthSessionPollResponse(
    val status: String = "pending",
    val auth: AuthResponse? = null,
    val error: String? = null
)

@Serializable
data class ProfileUpdateRequest(
    @SerialName("full_name") val fullName: String? = null,
    val allergies: List<String>? = null,
    @SerialName("diet_preference") val dietPreference: String? = null
)

@Serializable
data class EncryptionStatusResponse(
    val status: String = "ACTIVE",
    val cipher: String = "AES-256-GCM",
    @SerialName("subject_token") val subjectToken: String = "",
    @SerialName("identity_decoupled") val identityDecoupled: Boolean = true,
    @SerialName("zero_trust_zone") val zeroTrustZone: String = "Zone B"
)

@Serializable
data class GroupCreateRequest(
    val name: String
)

@Serializable
data class GroupJoinRequest(
    @SerialName("invite_code") val inviteCode: String
)

@Serializable
data class ReferenceInterval(
    val low: Double? = null,
    val high: Double? = null
)

@Serializable
data class BiomarkerObservation(
    val code: String,
    @SerialName("loinc_code") val loincCode: String? = null,
    @SerialName("display_name") val displayName: String,
    val category: String,
    @SerialName("numeric_value") val numericValue: Double,
    val unit: String,
    @SerialName("reference_interval") val referenceInterval: ReferenceInterval? = null,
    val status: String = "NORMAL"
)

@Serializable
data class BiomarkerPanelRecord(
    val id: String = "",
    @SerialName("subject_token") val subjectToken: String = "",
    @SerialName("report_date") val reportDate: String = "",
    @SerialName("lab_provider") val labProvider: String = "Standard Diagnostics",
    val observations: Map<String, BiomarkerObservation> = emptyMap()
)

@Serializable
data class BiomarkerUploadRequest(
    @SerialName("lab_provider") val labProvider: String = "Manual Clinical Entry",
    val observations: List<BiomarkerObservation>
)

@Serializable
data class GroupMember(
    val id: String,
    @SerialName("full_name") val fullName: String,
    val email: String,
    @SerialName("picture_url") val pictureUrl: String? = null,
    val allergies: List<String> = emptyList(),
    @SerialName("diet_preference") val dietPreference: String = "omnivore"
)

@Serializable
data class GroupResponse(
    val id: String,
    val name: String,
    @SerialName("invite_code") val inviteCode: String,
    @SerialName("owner_id") val ownerId: String,
    val members: List<GroupMember> = emptyList()
)

@Serializable
data class Ingredient(
    val name: String,
    val amount: String,
    val unit: String,
    val allergens: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)

@Serializable
data class NextMealRequest(
    @SerialName("group_id") val groupId: String? = null,
    @SerialName("active_member_ids") val activeMemberIds: List<String>? = null,
    @SerialName("meal_slot") val mealSlot: String = "DINNER",
    @SerialName("max_prep_time_minutes") val maxPrepTimeMinutes: Int = 35,
    @SerialName("dining_mode") val diningMode: String = "HOME_COOKING"
)

@Serializable
data class IndividualClinicalRationale(
    @SerialName("member_id") val memberId: String,
    @SerialName("member_name") val memberName: String,
    @SerialName("targeted_biomarkers") val targetedBiomarkers: List<String> = emptyList(),
    val action: String,
    val mechanism: String,
    @SerialName("suggested_custom_finish") val suggestedCustomFinish: String? = null
)

@Serializable
data class NextMealResponse(
    @SerialName("meal_id") val mealId: String,
    val title: String,
    val description: String,
    @SerialName("meal_slot") val mealSlot: String,
    @SerialName("prep_time_minutes") val prepTimeMinutes: Int,
    @SerialName("participating_members") val participatingMembers: List<String>,
    @SerialName("allergens_strictly_avoided") val allergensStrictlyAvoided: List<String> = emptyList(),
    @SerialName("macros_per_serving") val macrosPerServing: Map<String, Double> = emptyMap(),
    @SerialName("shared_base_ingredients") val sharedBaseIngredients: List<Ingredient> = emptyList(),
    @SerialName("prep_instructions") val prepInstructions: List<String> = emptyList(),
    @SerialName("member_customizations") val memberCustomizations: List<IndividualClinicalRationale> = emptyList(),
    @SerialName("overall_consensus_reasoning") val overallConsensusReasoning: String,
    @SerialName("llm_provider_used") val llmProviderUsed: String
)
