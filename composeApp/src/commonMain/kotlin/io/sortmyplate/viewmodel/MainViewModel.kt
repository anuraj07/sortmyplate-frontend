package io.sortmyplate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.sortmyplate.model.*
import io.sortmyplate.network.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppSection(val label: String, val icon: String) {
    TODAY("Today", "🌿"),
    BLOOD("Blood", "🩸"),
    INSIGHTS("Insights", "✨"),
    CIRCLE("Circle", "👥")
}

data class InsightPoint(
    val title: String,
    val body: String
)

data class InsightPillar(
    val id: String,
    val kicker: String,
    val title: String,
    val bigValue: String,
    val statusBadge: String,
    val wide: Boolean,
    val paletteKey: String,
    val summary: String,
    val points: List<InsightPoint>
)

data class BiomarkerPreset(
    val code: String,
    val loincCode: String,
    val displayName: String,
    val category: String,
    val unit: String,
    val low: Double,
    val high: Double
)

val SUPPORTED_BIOMARKER_PRESETS = listOf(
    BiomarkerPreset("APOB", "1884-6", "Apolipoprotein B (ApoB)", "Lipid Panel", "mg/dL", 40.0, 90.0),
    BiomarkerPreset("LDL_C", "2089-1", "LDL Cholesterol", "Lipid Panel", "mg/dL", 0.0, 100.0),
    BiomarkerPreset("HBA1C", "4548-4", "Hemoglobin A1c", "Glycemic Control", "%", 4.0, 5.6),
    BiomarkerPreset("FERRITIN", "24331-1", "Serum Ferritin", "Iron Status", "ng/mL", 30.0, 200.0),
    BiomarkerPreset("HS_CRP", "30934-4", "High-Sensitivity CRP", "Inflammation", "mg/L", 0.0, 1.0),
    BiomarkerPreset("VITAMIN_D3", "62292-8", "Vitamin D3 (25-OH)", "Micronutrients", "ng/mL", 30.0, 80.0),
    BiomarkerPreset("FASTING_GLUCOSE", "1558-6", "Fasting Glucose", "Glycemic Control", "mg/dL", 70.0, 99.0)
)

data class UiState(
    val isBootstrapping: Boolean = true,
    val isBootstrapped: Boolean = false,
    val discoveredBaseUrl: String = getServerOrigin(),
    val discoveredEndpointCount: Int = 0,
    val googleOauthEnabled: Boolean = false,
    val googleClientId: String? = null,
    val llmStatusLabel: String = "Connecting...",
    val currentUser: AuthResponse? = null,
    val isOnboardingVisible: Boolean = false,
    val completedOnboardingUserIds: Set<String> = emptySet(),
    val activeSection: AppSection = AppSection.TODAY,
    val onboardingStep: Int = 0,
    val showGuideModal: Boolean = false,
    val selectedInsight: InsightPillar? = null,
    val biomarkers: BiomarkerPanelRecord? = null,
    val group: GroupResponse? = null,
    val activeMemberIds: Set<String> = emptySet(),
    val selectedMealSlot: String = "DINNER",
    val isAuthLoading: Boolean = false,
    val isAwaitingGoogleBrowser: Boolean = false,
    val oauthSetupHint: String? = null,
    val isSavingBiomarker: Boolean = false,
    val isManagingGroup: Boolean = false,
    val isUpdatingProfile: Boolean = false,
    val isLoadingMeal: Boolean = false,
    val currentMeal: NextMealResponse? = null,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

class MainViewModel(
    private val api: SortMyPlateApi = SortMyPlateApi()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var oauthPollJob: Job? = null
    private var activeOAuthState: String? = null

    init {
        bootstrapSession()
        observeDeepLinkCallbacks()
    }

    private fun observeDeepLinkCallbacks() {
        viewModelScope.launch {
            OAuthDeepLinkBus.pendingCallback.collect { callback ->
                if (callback == null) return@collect
                OAuthDeepLinkBus.consume()
                handleOAuthCallbackResult(callback)
            }
        }
    }

    fun bootstrapSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBootstrapping = true, errorMessage = null) }
            api.bootstrap().fold(
                onSuccess = { discovery ->
                    _uiState.update {
                        it.copy(
                            isBootstrapping = false,
                            isBootstrapped = true,
                            discoveredBaseUrl = discovery.baseUrl,
                            discoveredEndpointCount = discovery.endpoints.size,
                            googleOauthEnabled = discovery.googleOauthEnabled,
                            googleClientId = discovery.googleClientId,
                            errorMessage = null
                        )
                    }
                    refreshLlmStatus()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isBootstrapping = false,
                            isBootstrapped = false,
                            llmStatusLabel = "Discovery Offline",
                            errorMessage = "Endpoint discovery failed on ${getServerOrigin()}: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    private fun refreshLlmStatus() {
        viewModelScope.launch {
            api.getLlmStatus().fold(
                onSuccess = { llm ->
                    val label = when (llm.provider) {
                        "gemini" -> "Gemini 2.5 (${llm.status})"
                        "deepseek" -> "DeepSeek-V3 (${llm.status})"
                        "claude" -> "Claude 3.5 (${llm.status})"
                        "openai" -> "GPT-4o (${llm.status})"
                        else -> "Clinical Engine (${llm.latencyMs?.toInt() ?: 1}ms)"
                    }
                    _uiState.update { it.copy(llmStatusLabel = label) }
                },
                onFailure = {
                    _uiState.update { it.copy(llmStatusLabel = "Clinical Engine") }
                }
            )
        }
    }

    fun selectSection(section: AppSection) {
        _uiState.update { it.copy(activeSection = section, statusMessage = null, errorMessage = null) }
    }

    fun setOnboardingStep(step: Int) {
        _uiState.update { it.copy(onboardingStep = step.coerceIn(0, 2)) }
    }

    fun completeOnboarding() {
        _uiState.update { state ->
            val updatedCompleted = state.currentUser?.userId?.let { state.completedOnboardingUserIds + it }
                ?: state.completedOnboardingUserIds
            state.copy(
                isOnboardingVisible = false,
                onboardingStep = 0,
                completedOnboardingUserIds = updatedCompleted
            )
        }
    }

    fun replayOnboarding() {
        _uiState.update {
            it.copy(
                isOnboardingVisible = true,
                onboardingStep = 0,
                statusMessage = null,
                errorMessage = null
            )
        }
    }

    fun setShowGuideModal(visible: Boolean) {
        _uiState.update { it.copy(showGuideModal = visible) }
    }

    fun openInsightDetail(pillar: InsightPillar?) {
        _uiState.update { it.copy(selectedInsight = pillar) }
    }

    fun startContinueWithGoogle() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAuthLoading = true,
                    oauthSetupHint = null,
                    errorMessage = null,
                    statusMessage = null
                )
            }

            if (!api.isBootstrapped()) {
                val bootRes = api.bootstrap()
                if (bootRes.isFailure) {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            errorMessage = "Endpoint discovery failed: ${bootRes.exceptionOrNull()?.message}"
                        )
                    }
                    return@launch
                }
                val boot = bootRes.getOrThrow()
                _uiState.update {
                    it.copy(
                        isBootstrapped = true,
                        discoveredBaseUrl = boot.baseUrl,
                        discoveredEndpointCount = boot.endpoints.size,
                        googleOauthEnabled = boot.googleOauthEnabled,
                        googleClientId = boot.googleClientId
                    )
                }
            }

            val clientId = api.getDiscoveredGoogleClientId() ?: _uiState.value.googleClientId
            val outcome = signInInAppWithGoogle(
                googleClientId = clientId,
                requestOAuthUrl = { state, platform, redirectTo ->
                    activeOAuthState = state
                    val loginResult = api.getGoogleOAuthLoginUrl(
                        state = state,
                        platform = platform,
                        redirectTo = redirectTo
                    )
                    loginResult.fold(
                        onSuccess = { meta ->
                            OAuthBrowserStart(state = meta.state, authUrl = meta.authUrl)
                        },
                        onFailure = { err ->
                            if (err is OAuthNotConfiguredException) {
                                val base = _uiState.value.discoveredBaseUrl
                                OAuthBrowserStart(
                                    state = state,
                                    unconfiguredMessage = "Server OAuth needs GOOGLE_CLIENT_ID & GOOGLE_CLIENT_SECRET (Redirect URI: $base/v1/auth/google/callback)."
                                )
                            } else {
                                OAuthBrowserStart(
                                    state = state,
                                    errorMessage = "Google Sign-In failed: ${err.message}"
                                )
                            }
                        }
                    )
                }
            )

            when (outcome) {
                is InAppGoogleSignInOutcome.IdToken -> {
                    api.signInWithGoogle(idToken = outcome.idToken).fold(
                        onSuccess = { auth -> completeAuthenticatedSession(auth) },
                        onFailure = { err ->
                            _uiState.update {
                                it.copy(
                                    isAuthLoading = false,
                                    isAwaitingGoogleBrowser = false,
                                    errorMessage = "Google ID verification failed: ${err.message}"
                                )
                            }
                        }
                    )
                }

                is InAppGoogleSignInOutcome.GoogleAccessToken -> {
                    api.signInWithGoogle(googleAccessToken = outcome.accessToken).fold(
                        onSuccess = { auth -> completeAuthenticatedSession(auth) },
                        onFailure = { err ->
                            _uiState.update {
                                it.copy(
                                    isAuthLoading = false,
                                    isAwaitingGoogleBrowser = false,
                                    errorMessage = "Google account sync failed: ${err.message}"
                                )
                            }
                        }
                    )
                }

                is InAppGoogleSignInOutcome.DeepLinkCallback -> {
                    api.exchangeGoogleOAuth(outcome.state, outcome.exchangeCode).fold(
                        onSuccess = { auth -> completeAuthenticatedSession(auth) },
                        onFailure = { err ->
                            _uiState.update {
                                it.copy(
                                    isAuthLoading = false,
                                    isAwaitingGoogleBrowser = false,
                                    errorMessage = "Google session exchange failed: ${err.message}"
                                )
                            }
                        }
                    )
                }

                is InAppGoogleSignInOutcome.AwaitingSessionPoll -> {
                    activeOAuthState = outcome.state
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            isAwaitingGoogleBrowser = true,
                            statusMessage = outcome.message
                        )
                    }
                    startPollingGoogleSession(outcome.state)
                }

                InAppGoogleSignInOutcome.Cancelled -> {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            isAwaitingGoogleBrowser = false,
                            statusMessage = null
                        )
                    }
                }

                is InAppGoogleSignInOutcome.Error -> {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            isAwaitingGoogleBrowser = false,
                            oauthSetupHint = if (outcome.isOAuthUnconfigured) outcome.message else null,
                            errorMessage = if (outcome.isOAuthUnconfigured) null else outcome.message
                        )
                    }
                }
            }
        }
    }

    private fun startPollingGoogleSession(state: String) {
        oauthPollJob?.cancel()
        oauthPollJob = viewModelScope.launch {
            repeat(90) {
                delay(1500)
                if (!_uiState.value.isAwaitingGoogleBrowser) return@launch

                api.pollGoogleOAuthSession(state).onSuccess { poll ->
                    when (poll.status) {
                        "completed" -> {
                            poll.auth?.let { auth ->
                                completeAuthenticatedSession(auth)
                                return@launch
                            }
                        }
                        "error" -> {
                            _uiState.update {
                                it.copy(
                                    isAwaitingGoogleBrowser = false,
                                    errorMessage = poll.error ?: "Google authorization failed"
                                )
                            }
                            return@launch
                        }
                        "consumed" -> {
                            _uiState.update { it.copy(isAwaitingGoogleBrowser = false) }
                            return@launch
                        }
                    }
                }
            }
            _uiState.update {
                it.copy(
                    isAwaitingGoogleBrowser = false,
                    errorMessage = "Google sign-in timed out. Tap Continue with Google to retry."
                )
            }
        }
    }

    private fun handleOAuthCallbackResult(callback: OAuthDeepLinkResult) {
        oauthPollJob?.cancel()
        if (!callback.error.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    isAwaitingGoogleBrowser = false,
                    isAuthLoading = false,
                    errorMessage = "Google Sign-In cancelled: ${callback.error}"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, isAwaitingGoogleBrowser = false) }
            api.exchangeGoogleOAuth(callback.state, callback.exchangeCode).fold(
                onSuccess = { auth -> completeAuthenticatedSession(auth) },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            errorMessage = "OAuth exchange failed: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun cancelGoogleSignIn() {
        oauthPollJob?.cancel()
        activeOAuthState = null
        _uiState.update {
            it.copy(
                isAwaitingGoogleBrowser = false,
                isAuthLoading = false,
                statusMessage = null
            )
        }
    }

    private fun completeAuthenticatedSession(auth: AuthResponse) {
        oauthPollJob?.cancel()
        activeOAuthState = null
        _uiState.update {
            it.copy(
                currentUser = auth,
                isOnboardingVisible = !it.completedOnboardingUserIds.contains(auth.userId),
                onboardingStep = 0,
                activeSection = AppSection.TODAY,
                isAuthLoading = false,
                isAwaitingGoogleBrowser = false,
                oauthSetupHint = null,
                errorMessage = null,
                statusMessage = null
            )
        }
        loadBiomarkers(auth.accessToken)
        loadGroups(auth.accessToken)
    }

    fun signOut() {
        oauthPollJob?.cancel()
        activeOAuthState = null
        _uiState.update { state ->
            state.copy(
                currentUser = null,
                isOnboardingVisible = false,
                onboardingStep = 0,
                activeSection = AppSection.TODAY,
                biomarkers = null,
                group = null,
                activeMemberIds = emptySet(),
                currentMeal = null,
                selectedInsight = null,
                isAwaitingGoogleBrowser = false,
                statusMessage = null,
                errorMessage = null
            )
        }
    }

    fun updateProfileSafeguards(allergiesRaw: String, dietPreference: String) {
        val token = _uiState.value.currentUser?.accessToken ?: return
        val allergies = allergiesRaw.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingProfile = true, errorMessage = null, statusMessage = null) }
            api.updateProfile(token, allergies, dietPreference).fold(
                onSuccess = { updated ->
                    val mergedUser = updated.copy(accessToken = token)
                    _uiState.update {
                        it.copy(
                            currentUser = mergedUser,
                            isUpdatingProfile = false,
                            statusMessage = "Updated dietary safeguards & allergy exclusions."
                        )
                    }
                    loadGroups(token)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isUpdatingProfile = false,
                            errorMessage = "Profile update failed: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    private fun loadBiomarkers(token: String) {
        viewModelScope.launch {
            api.getLatestBiomarkers(token).onSuccess { panel ->
                _uiState.update { it.copy(biomarkers = panel) }
            }
        }
    }

    fun recordQuickBiomarker(preset: BiomarkerPreset, valueText: String) {
        val token = _uiState.value.currentUser?.accessToken ?: return
        val numericValue = valueText.trim().toDoubleOrNull()
        if (numericValue == null) {
            _uiState.update { it.copy(errorMessage = "Enter a valid number for ${preset.displayName}.") }
            return
        }

        val merged = _uiState.value.biomarkers?.observations?.toMutableMap() ?: mutableMapOf()
        merged[preset.code] = BiomarkerObservation(
            code = preset.code,
            loincCode = preset.loincCode,
            displayName = preset.displayName,
            category = preset.category,
            numericValue = numericValue,
            unit = preset.unit,
            referenceInterval = ReferenceInterval(low = preset.low, high = preset.high),
            status = "NORMAL"
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingBiomarker = true, errorMessage = null, statusMessage = null) }
            api.recordBiomarkers(
                token = token,
                request = BiomarkerUploadRequest(
                    labProvider = "Manual Clinical Entry",
                    observations = merged.values.toList()
                )
            ).fold(
                onSuccess = { panel ->
                    _uiState.update {
                        it.copy(
                            biomarkers = panel,
                            isSavingBiomarker = false,
                            statusMessage = "Encrypted ${preset.displayName} into Zone B PHI Vault."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSavingBiomarker = false,
                            errorMessage = "Failed to save biomarker: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    private fun loadGroups(token: String) {
        viewModelScope.launch {
            api.getMyGroups(token).onSuccess { groups ->
                if (groups.isNotEmpty()) {
                    val primaryGroup = groups.first()
                    _uiState.update {
                        it.copy(
                            group = primaryGroup,
                            activeMemberIds = primaryGroup.members.map { m -> m.id }.toSet()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(group = null, activeMemberIds = emptySet())
                    }
                }
            }
        }
    }

    fun createGroup(name: String) {
        val token = _uiState.value.currentUser?.accessToken ?: return
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter a household or group name.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isManagingGroup = true, errorMessage = null, statusMessage = null) }
            api.createGroup(token, name).fold(
                onSuccess = { grp ->
                    _uiState.update {
                        it.copy(
                            isManagingGroup = false,
                            group = grp,
                            activeMemberIds = grp.members.map { m -> m.id }.toSet(),
                            statusMessage = "Created '${grp.name}' (Invite code: ${grp.inviteCode})."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isManagingGroup = false,
                            errorMessage = "Create group failed: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun joinGroup(inviteCode: String) {
        val token = _uiState.value.currentUser?.accessToken ?: return
        if (inviteCode.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter a valid group invite code.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isManagingGroup = true, errorMessage = null, statusMessage = null) }
            api.joinGroup(token, inviteCode).fold(
                onSuccess = { grp ->
                    _uiState.update {
                        it.copy(
                            isManagingGroup = false,
                            group = grp,
                            activeMemberIds = grp.members.map { m -> m.id }.toSet(),
                            statusMessage = "Joined '${grp.name}'."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isManagingGroup = false,
                            errorMessage = "Join group failed: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun leaveGroup() {
        val token = _uiState.value.currentUser?.accessToken ?: return
        val groupId = _uiState.value.group?.id ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isManagingGroup = true, errorMessage = null, statusMessage = null) }
            api.leaveGroup(token, groupId).fold(
                onSuccess = {
                    _uiState.update { it.copy(isManagingGroup = false, statusMessage = "Left group. Switched to Solo Mode.") }
                    loadGroups(token)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isManagingGroup = false,
                            errorMessage = "Could not leave group: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun toggleMember(memberId: String) {
        _uiState.update { state ->
            val updated = if (state.activeMemberIds.contains(memberId)) {
                state.activeMemberIds - memberId
            } else {
                state.activeMemberIds + memberId
            }
            state.copy(activeMemberIds = updated)
        }
    }

    fun setMealSlot(slot: String) {
        _uiState.update { it.copy(selectedMealSlot = slot) }
    }

    fun getNextMeal() {
        val state = _uiState.value
        val token = state.currentUser?.accessToken ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMeal = true, errorMessage = null) }
            val activeIds = if (state.group != null && state.activeMemberIds.isNotEmpty()) {
                state.activeMemberIds.toList()
            } else {
                null
            }
            val res = api.getNextMeal(
                token = token,
                groupId = if (activeIds != null) state.group?.id else null,
                activeMemberIds = activeIds,
                mealSlot = state.selectedMealSlot
            )

            res.fold(
                onSuccess = { meal ->
                    _uiState.update { it.copy(isLoadingMeal = false, currentMeal = meal) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingMeal = false,
                            errorMessage = "Failed to recommend meal: ${error.message}"
                        )
                    }
                }
            )
        }
    }
}
