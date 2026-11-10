package io.sortmyplate

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sortmyplate.network.NetworkInspectorStore
import io.sortmyplate.network.NetworkLogStatus
import io.sortmyplate.network.isDebugBuild
import io.sortmyplate.theme.*
import io.sortmyplate.ui.*
import io.sortmyplate.viewmodel.AppSection
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
        if (showNetworkInspector && isDebugBuild()) {
            NetworkInspectorDialog(
                transactions = networkTransactions,
                onClear = { NetworkInspectorStore.clear() },
                onDismiss = { showNetworkInspector = false }
            )
        }

        if (uiState.showGuideModal) {
            OnboardingGuideDialog(
                onDismiss = { viewModel.setShowGuideModal(false) }
            )
        }

        uiState.selectedInsight?.let { pillar ->
            InsightDetailDialog(
                pillar = pillar,
                onDismiss = { viewModel.openInsightDetail(null) }
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(MistBackground, MistTint)))
        ) {
            val isWideScreen = maxWidth >= 700.dp
            val showFloatingBottomBar = uiState.currentUser != null && !uiState.isOnboardingVisible && !isWideScreen

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 1120.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HeaderView(
                            user = uiState.currentUser,
                            llmStatusLabel = uiState.llmStatusLabel,
                            onOpenGuide = { viewModel.setShowGuideModal(true) }
                        )

                        uiState.statusMessage?.let { statusMsg ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = EmeraldPrimary.copy(alpha = 0.12f),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(EmeraldPrimary.copy(alpha = 0.35f))
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "✓ $statusMsg",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ForestHeroMid,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }

                        uiState.errorMessage?.let { errorMsg ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = RoseRed.copy(alpha = 0.1f),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(RoseRed.copy(alpha = 0.35f))
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
                                        fontSize = 12.5.sp,
                                        color = Color(0xFF9F2213),
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

                        val currentUser = uiState.currentUser
                        if (currentUser == null) {
                            SignInView(
                                isBootstrapping = uiState.isBootstrapping,
                                isBootstrapped = uiState.isBootstrapped,
                                discoveredBaseUrl = uiState.discoveredBaseUrl,
                                discoveredEndpointCount = uiState.discoveredEndpointCount,
                                isAuthLoading = uiState.isAuthLoading,
                                isAwaitingGoogleBrowser = uiState.isAwaitingGoogleBrowser,
                                oauthSetupHint = uiState.oauthSetupHint,
                                onRetryDiscovery = { viewModel.bootstrapSession() },
                                onContinueWithGoogle = { viewModel.startContinueWithGoogle() },
                                onCancelGoogleBrowser = { viewModel.cancelGoogleSignIn() },
                                onFallbackSignIn = { email, name, allergies ->
                                    viewModel.signInWithGoogle(email, name, allergies)
                                }
                            )
                        } else if (uiState.isOnboardingVisible) {
                            PostLoginOnboardingView(
                                user = currentUser,
                                onboardingStep = uiState.onboardingStep,
                                onSelectStep = { step -> viewModel.setOnboardingStep(step) },
                                onFinishOnboarding = { viewModel.completeOnboarding() }
                            )
                        } else {
                            if (isWideScreen) {
                                DesktopSectionPills(
                                    activeSection = uiState.activeSection,
                                    onSelectSection = { section -> viewModel.selectSection(section) }
                                )
                            }

                            when (uiState.activeSection) {
                                AppSection.TODAY -> {
                                    TodayView(
                                        user = currentUser,
                                        panel = uiState.biomarkers,
                                        group = uiState.group,
                                        activeMemberIds = uiState.activeMemberIds,
                                        selectedMealSlot = uiState.selectedMealSlot,
                                        isLoadingMeal = uiState.isLoadingMeal,
                                        currentMeal = uiState.currentMeal,
                                        onSelectMealSlot = { slot -> viewModel.setMealSlot(slot) },
                                        onGetNextMeal = { viewModel.getNextMeal() },
                                        onNavigateSection = { section -> viewModel.selectSection(section) },
                                        onOpenGuide = { viewModel.setShowGuideModal(true) }
                                    )
                                }
                                AppSection.BLOOD -> {
                                    BiomarkersView(
                                        panel = uiState.biomarkers,
                                        isSavingBiomarker = uiState.isSavingBiomarker,
                                        onSaveBiomarker = { preset, value ->
                                            viewModel.recordQuickBiomarker(preset, value)
                                        }
                                    )
                                }
                                AppSection.INSIGHTS -> {
                                    InsightsView(
                                        panel = uiState.biomarkers,
                                        onOpenGuide = { viewModel.setShowGuideModal(true) },
                                        onSelectInsight = { pillar -> viewModel.openInsightDetail(pillar) }
                                    )
                                }
                                AppSection.CIRCLE -> {
                                    HouseholdView(
                                        user = currentUser,
                                        group = uiState.group,
                                        activeMemberIds = uiState.activeMemberIds,
                                        onToggleMember = { id -> viewModel.toggleMember(id) },
                                        onCreateGroup = { name -> viewModel.createGroup(name) },
                                        onJoinGroup = { code -> viewModel.joinGroup(code) },
                                        onLeaveGroup = { viewModel.leaveGroup() },
                                        isManagingGroup = uiState.isManagingGroup,
                                        isUpdatingProfile = uiState.isUpdatingProfile,
                                        discoveredBaseUrl = uiState.discoveredBaseUrl,
                                        discoveredEndpointCount = uiState.discoveredEndpointCount,
                                        networkTxCount = networkTransactions.size,
                                        hasNetworkErrors = hasNetworkErrors,
                                        onUpdateProfile = { allergiesRaw, dietPref ->
                                            viewModel.updateProfileSafeguards(allergiesRaw, dietPref)
                                        },
                                        onOpenGuide = { viewModel.setShowGuideModal(true) },
                                        onReplayOnboarding = { viewModel.replayOnboarding() },
                                        onOpenNetworkInspector = { showNetworkInspector = true },
                                        onSignOut = { viewModel.signOut() }
                                    )
                                }
                            }
                        }

                        if (showFloatingBottomBar) {
                            Spacer(modifier = Modifier.height(104.dp))
                        } else {
                            Spacer(modifier = Modifier.navigationBarsPadding().height(16.dp))
                        }
                    }
                }

                if (showFloatingBottomBar) {
                    MobileBottomSectionBar(
                        activeSection = uiState.activeSection,
                        onSelectSection = { section -> viewModel.selectSection(section) },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

@Composable
private fun DesktopSectionPills(
    activeSection: AppSection,
    onSelectSection: (AppSection) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = CardWhite.copy(alpha = 0.85f),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.85f))
            )
        ) {
            Row(
                modifier = Modifier.padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppSection.entries.forEach { section ->
                    val selected = section == activeSection
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (selected) ForestHeroDeep else Color.Transparent,
                        modifier = Modifier.clickable { onSelectSection(section) }
                    ) {
                        Text(
                            text = "${section.icon}  ${section.label}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.White else TextSecondary,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MobileBottomSectionBar(
    activeSection: AppSection,
    onSelectSection: (AppSection) -> Unit,
    modifier: Modifier = Modifier
) {
    // Transparent outer wrapper so underlying content visibly floats underneath
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        val pillShape = RoundedCornerShape(32.dp)
        Box(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = pillShape,
                    ambientColor = ForestHeroDeep.copy(alpha = 0.16f),
                    spotColor = ForestHeroDeep.copy(alpha = 0.22f)
                )
                .clip(pillShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.56f),
                            SageSurface.copy(alpha = 0.36f),
                            MistTint.copy(alpha = 0.26f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.90f),
                            ForestHeroMid.copy(alpha = 0.16f)
                        )
                    ),
                    shape = pillShape
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 7.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppSection.entries.forEach { section ->
                    val selected = section == activeSection
                    val tabShape = RoundedCornerShape(24.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(tabShape)
                            .then(
                                if (selected) {
                                    Modifier
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    ForestHeroMid.copy(alpha = 0.86f),
                                                    ForestHeroDeep.copy(alpha = 0.90f)
                                                )
                                            )
                                        )
                                        .border(
                                            width = 0.8.dp,
                                            color = Color.White.copy(alpha = 0.28f),
                                            shape = tabShape
                                        )
                                } else {
                                    Modifier.background(Color.Transparent)
                                }
                            )
                            .clickable { onSelectSection(section) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = section.icon, fontSize = 15.sp)
                            Text(
                                text = section.label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (selected) Color.White else ForestHeroDeep.copy(alpha = 0.80f)
                            )
                        }
                    }
                }
            }
        }
    }
}
