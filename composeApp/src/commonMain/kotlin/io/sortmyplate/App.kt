package io.sortmyplate

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sortmyplate.network.NetworkInspectorStore
import io.sortmyplate.network.NetworkLogStatus
import io.sortmyplate.theme.BgDark
import io.sortmyplate.theme.CyanAccent
import io.sortmyplate.theme.EmeraldGreen
import io.sortmyplate.theme.RoseRed
import io.sortmyplate.theme.SortMyPlateTheme
import io.sortmyplate.ui.*
import io.sortmyplate.viewmodel.MainViewModel

@Composable
fun App(viewModel: MainViewModel = remember { MainViewModel() }) {
    val uiState by viewModel.uiState.collectAsState()
    val networkTransactions by NetworkInspectorStore.transactions.collectAsState()
    var showNetworkInspector by remember { mutableStateOf(false) }

    val hasNetworkErrors = networkTransactions.any {
        it.status == NetworkLogStatus.CLIENT_ERROR ||
            it.status == NetworkLogStatus.SERVER_ERROR ||
            it.status == NetworkLogStatus.FAILED
    }

    SortMyPlateTheme {
        if (showNetworkInspector) {
            NetworkInspectorDialog(
                transactions = networkTransactions,
                onClear = { NetworkInspectorStore.clear() },
                onDismiss = { showNetworkInspector = false }
            )
        }

        Scaffold(
            containerColor = BgDark
        ) { paddingValues ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val isWideScreen = maxWidth >= 700.dp

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Header (Brand, Network Inspector Pill, LLM Status, Sign Out)
                    HeaderView(
                        user = uiState.currentUser,
                        llmStatusLabel = uiState.llmStatusLabel,
                        networkTxCount = networkTransactions.size,
                        hasNetworkErrors = hasNetworkErrors,
                        onOpenNetworkInspector = { showNetworkInspector = true },
                        onSignOut = { viewModel.signOut() }
                    )

                    // Status Notification Banner
                    uiState.statusMessage?.let { statusMsg ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldGreen.copy(alpha = 0.14f),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(EmeraldGreen.copy(alpha = 0.35f))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✓ $statusMsg",
                                fontSize = 12.sp,
                                color = EmeraldGreen,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Error / Retry Banner
                    uiState.errorMessage?.let { errorMsg ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = RoseRed.copy(alpha = 0.15f),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(RoseRed.copy(alpha = 0.4f))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚠️ $errorMsg",
                                    fontSize = 12.sp,
                                    color = RoseRed,
                                    modifier = Modifier.weight(1f).padding(end = 12.dp)
                                )
                                if (!uiState.isBootstrapped) {
                                    Button(
                                        onClick = { viewModel.bootstrapSession() },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoseRed),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Retry", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    if (uiState.currentUser == null) {
                        // 2A. Unauthenticated: Dynamic Discovery + Real Google Sign-In
                        SignInView(
                            isBootstrapping = uiState.isBootstrapping,
                            isBootstrapped = uiState.isBootstrapped,
                            discoveredBaseUrl = uiState.discoveredBaseUrl,
                            discoveredEndpointCount = uiState.discoveredEndpointCount,
                            isAuthLoading = uiState.isAuthLoading,
                            onRetryDiscovery = { viewModel.bootstrapSession() },
                            onSignIn = { email, name, allergies ->
                                viewModel.signInWithGoogle(email, name, allergies)
                            }
                        )
                    } else {
                        // Discovery indicator inside authenticated dashboard
                        Text(
                            text = "📡 Active Session Endpoint Discovery: ${uiState.discoveredEndpointCount} endpoints via ${uiState.discoveredBaseUrl}",
                            fontSize = 11.sp,
                            color = CyanAccent.copy(alpha = 0.85f)
                        )

                        // 2B. Hero Action: 1-Click "What Should I Eat Next?"
                        HeroNextMealView(
                            selectedMealSlot = uiState.selectedMealSlot,
                            onSelectMealSlot = { slot -> viewModel.setMealSlot(slot) },
                            onGetNextMeal = { viewModel.getNextMeal() },
                            isLoading = uiState.isLoadingMeal,
                            subjectToken = uiState.currentUser?.subjectToken
                        )

                        // 3. Recommended Meal Result (when available)
                        uiState.currentMeal?.let { meal ->
                            MealResultView(meal = meal)
                        }

                        // 4. Responsive Section: Household Dynamic Members & Blood Panel Vault
                        if (isWideScreen) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                HouseholdView(
                                    group = uiState.group,
                                    activeMemberIds = uiState.activeMemberIds,
                                    onToggleMember = { id -> viewModel.toggleMember(id) },
                                    onCreateGroup = { name -> viewModel.createGroup(name) },
                                    onJoinGroup = { code -> viewModel.joinGroup(code) },
                                    onLeaveGroup = { viewModel.leaveGroup() },
                                    isManagingGroup = uiState.isManagingGroup,
                                    currentUserId = uiState.currentUser?.userId,
                                    modifier = Modifier.weight(1f)
                                )
                                BiomarkersView(
                                    panel = uiState.biomarkers,
                                    isSavingBiomarker = uiState.isSavingBiomarker,
                                    onSaveBiomarker = { preset, value ->
                                        viewModel.recordQuickBiomarker(preset, value)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                HouseholdView(
                                    group = uiState.group,
                                    activeMemberIds = uiState.activeMemberIds,
                                    onToggleMember = { id -> viewModel.toggleMember(id) },
                                    onCreateGroup = { name -> viewModel.createGroup(name) },
                                    onJoinGroup = { code -> viewModel.joinGroup(code) },
                                    onLeaveGroup = { viewModel.leaveGroup() },
                                    isManagingGroup = uiState.isManagingGroup,
                                    currentUserId = uiState.currentUser?.userId
                                )
                                BiomarkersView(
                                    panel = uiState.biomarkers,
                                    isSavingBiomarker = uiState.isSavingBiomarker,
                                    onSaveBiomarker = { preset, value ->
                                        viewModel.recordQuickBiomarker(preset, value)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
