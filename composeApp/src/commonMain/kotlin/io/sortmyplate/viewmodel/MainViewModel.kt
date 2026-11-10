package io.sortmyplate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.sortmyplate.model.AuthResponse
import io.sortmyplate.model.BiomarkerPanelRecord
import io.sortmyplate.model.GroupResponse
import io.sortmyplate.model.NextMealResponse
import io.sortmyplate.network.SortMyPlateApi
import io.sortmyplate.network.getApiBaseUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val currentUser: AuthResponse? = null,
    val biomarkers: BiomarkerPanelRecord? = null,
    val group: GroupResponse? = null,
    val activeMemberIds: Set<String> = emptySet(),
    val selectedMealSlot: String = "DINNER",
    val isLoading: Boolean = false,
    val isLoadingMeal: Boolean = false,
    val currentMeal: NextMealResponse? = null,
    val errorMessage: String? = null
)

class MainViewModel(
    private val api: SortMyPlateApi = SortMyPlateApi()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loginAsAlexDemo()
    }

    fun loginAsAlexDemo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val authResult = api.loginAsDevUser()
            authResult.fold(
                onSuccess = { auth ->
                    _uiState.update { it.copy(currentUser = auth, isLoading = false) }
                    loadBiomarkers(auth.accessToken)
                    loadGroups(auth.accessToken)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Auth error connecting to ${getApiBaseUrl()}: ${error.message}. Ensure backend is running on port 8000."
                        )
                    }
                }
            )
        }
    }

    private fun loadBiomarkers(token: String) {
        viewModelScope.launch {
            val res = api.getLatestBiomarkers(token)
            res.onSuccess { panel ->
                _uiState.update { it.copy(biomarkers = panel) }
            }
        }
    }

    private fun loadGroups(token: String) {
        viewModelScope.launch {
            val res = api.getMyGroups(token)
            res.onSuccess { groups ->
                if (groups.isNotEmpty()) {
                    val primaryGroup = groups.first()
                    _uiState.update {
                        it.copy(
                            group = primaryGroup,
                            activeMemberIds = primaryGroup.members.map { m -> m.id }.toSet()
                        )
                    }
                }
            }
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
            val res = api.getNextMeal(
                token = token,
                groupId = state.group?.id,
                activeMemberIds = state.activeMemberIds.toList(),
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
