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
import io.sortmyplate.theme.BgDark
import io.sortmyplate.theme.RoseRed
import io.sortmyplate.theme.SortMyPlateTheme
import io.sortmyplate.ui.*
import io.sortmyplate.viewmodel.MainViewModel

@Composable
fun App(viewModel: MainViewModel = remember { MainViewModel() }) {
    val uiState by viewModel.uiState.collectAsState()

    SortMyPlateTheme {
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
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 1. Header (User Auth, AES-256 Shield)
                    HeaderView(
                        user = uiState.currentUser,
                        onResetUser = { viewModel.loginAsAlexDemo() }
                    )

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
                                Button(
                                    onClick = { viewModel.loginAsAlexDemo() },
                                    colors = ButtonDefaults.buttonColors(containerColor = RoseRed),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Retry", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    // 2. Hero Action: 1-Click "What Should I Eat Next?"
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
                                currentUserId = uiState.currentUser?.userId,
                                modifier = Modifier.weight(1f)
                            )
                            BiomarkersView(
                                panel = uiState.biomarkers,
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
                                currentUserId = uiState.currentUser?.userId
                            )
                            BiomarkersView(
                                panel = uiState.biomarkers
                            )
                        }
                    }
                }
            }
        }
    }
}
