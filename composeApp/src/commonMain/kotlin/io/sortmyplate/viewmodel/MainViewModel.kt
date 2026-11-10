package io.sortmyplate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.sortmyplate.model.AuthResponse
import io.sortmyplate.model.BiomarkerObservation
import io.sortmyplate.model.BiomarkerPanelRecord
import io.sortmyplate.model.BiomarkerUploadRequest
import io.sortmyplate.model.GroupResponse
import io.sortmyplate.model.NextMealResponse
import io.sortmyplate.model.ReferenceInterval
import io.sortmyplate.network.SortMyPlateApi
import io.sortmyplate.network.getServerOrigin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    BiomarkerPreset("HS_CRP", "30934-4", "High-Sensitivity CRP", "Inflammation", "mg/L", 0.0, 1.0)
)

data class UiState(
    val isBootstrapping: Boolean = true,
    val isBootstrapped: Boolean = false,
    val discoveredBaseUrl: String = getServerOrigin(),
    val discoveredEndpointCount: Int = 0,
    val llmStatusLabel: String = "Discovering API...",
    val currentUser: AuthResponse? = null,
    val biomarkers: BiomarkerPanelRecord? = null,
    val group: GroupResponse? = null,
    val activeMemberIds: Set<String> = emptySet(),
    val selectedMealSlot: String = "DINNER",
    val isAuthLoading: Boolean = false,
    val isSavingBiomarker: Boolean = false,
    val isManagingGroup: Boolean = false,
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

    init {
        bootstrapSession()
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
                        "deepseek" -> "DeepSeek-V3 (${llm.status})"
                        "claude" -> "Claude 3.5 (${llm.status})"
                        "openai" -> "GPT-4o (${llm.status})"
                        else -> "Clinical AI (${llm.latencyMs?.toInt() ?: 1}ms)"
                    }
                    _uiState.update { it.copy(llmStatusLabel = label) }
                },
                onFailure = {
                    _uiState.update { it.copy(llmStatusLabel = "Clinical AI Ready") }
                }
            )
        }
    }

    fun signInWithGoogle(email: String, name: String, allergiesRaw: String) {
        val cleanEmail = email.trim()
        val cleanName = name.trim()
        if (cleanEmail.isBlank() || cleanName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter both your Google email and full name.") }
            return
        }

        val allergies = allergiesRaw
            .split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }

        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, errorMessage = null, statusMessage = null) }

            if (!api.isBootstrapped()) {
                val bootRes = api.bootstrap()
                if (bootRes.isFailure) {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            isBootstrapped = false,
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
                        discoveredEndpointCount = boot.endpoints.size
                    )
                }
            }

            api.signInWithGoogle(email = cleanEmail, name = cleanName, allergies = allergies).fold(
                onSuccess = { auth ->
                    _uiState.update {
                        it.copy(
                            currentUser = auth,
                            isAuthLoading = false,
                            errorMessage = null
                        )
                    }
                    loadBiomarkers(auth.accessToken)
                    loadGroups(auth.accessToken)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            errorMessage = "Google Sign-In failed: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun signOut() {
        _uiState.update { state ->
            state.copy(
                currentUser = null,
                biomarkers = null,
                group = null,
                activeMemberIds = emptySet(),
                currentMeal = null,
                statusMessage = null,
                errorMessage = null
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
                            statusMessage = "Encrypted ${preset.displayName} into PHI Vault."
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
