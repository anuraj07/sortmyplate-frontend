package io.sortmyplate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sortmyplate.model.AuthResponse
import io.sortmyplate.model.GroupResponse
import io.sortmyplate.network.isDebugBuild
import io.sortmyplate.theme.*

@Composable
fun HouseholdView(
    user: AuthResponse,
    group: GroupResponse?,
    activeMemberIds: Set<String>,
    onToggleMember: (String) -> Unit,
    onCreateGroup: (String) -> Unit,
    onJoinGroup: (String) -> Unit,
    onLeaveGroup: () -> Unit,
    isManagingGroup: Boolean,
    isUpdatingProfile: Boolean,
    discoveredBaseUrl: String,
    discoveredEndpointCount: Int,
    networkTxCount: Int,
    hasNetworkErrors: Boolean,
    onUpdateProfile: (allergiesRaw: String, dietPreference: String) -> Unit,
    onOpenGuide: () -> Unit,
    onReplayOnboarding: () -> Unit,
    onOpenNetworkInspector: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isWide = maxWidth >= 820.dp
            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    CircleCard(
                        group = group,
                        activeMemberIds = activeMemberIds,
                        currentUserId = user.userId,
                        isManagingGroup = isManagingGroup,
                        onToggleMember = onToggleMember,
                        onCreateGroup = onCreateGroup,
                        onJoinGroup = onJoinGroup,
                        onLeaveGroup = onLeaveGroup,
                        modifier = Modifier.weight(1.1f)
                    )
                    Column(
                        modifier = Modifier.weight(0.95f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ProfileSafeguardsCard(
                            user = user,
                            isUpdatingProfile = isUpdatingProfile,
                            onUpdateProfile = onUpdateProfile
                        )
                        VaultSessionCard(
                            user = user,
                            discoveredBaseUrl = discoveredBaseUrl,
                            discoveredEndpointCount = discoveredEndpointCount,
                            networkTxCount = networkTxCount,
                            hasNetworkErrors = hasNetworkErrors,
                            onOpenGuide = onOpenGuide,
                            onReplayOnboarding = onReplayOnboarding,
                            onOpenNetworkInspector = onOpenNetworkInspector,
                            onSignOut = onSignOut
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircleCard(
                        group = group,
                        activeMemberIds = activeMemberIds,
                        currentUserId = user.userId,
                        isManagingGroup = isManagingGroup,
                        onToggleMember = onToggleMember,
                        onCreateGroup = onCreateGroup,
                        onJoinGroup = onJoinGroup,
                        onLeaveGroup = onLeaveGroup
                    )
                    ProfileSafeguardsCard(
                        user = user,
                        isUpdatingProfile = isUpdatingProfile,
                        onUpdateProfile = onUpdateProfile
                    )
                    VaultSessionCard(
                        user = user,
                        discoveredBaseUrl = discoveredBaseUrl,
                        discoveredEndpointCount = discoveredEndpointCount,
                        networkTxCount = networkTxCount,
                        hasNetworkErrors = hasNetworkErrors,
                        onOpenGuide = onOpenGuide,
                        onReplayOnboarding = onReplayOnboarding,
                        onOpenNetworkInspector = onOpenNetworkInspector,
                        onSignOut = onSignOut
                    )
                }
            }
        }
    }
}

@Composable
private fun CircleCard(
    group: GroupResponse?,
    activeMemberIds: Set<String>,
    currentUserId: String,
    isManagingGroup: Boolean,
    onToggleMember: (String) -> Unit,
    onCreateGroup: (String) -> Unit,
    onJoinGroup: (String) -> Unit,
    onLeaveGroup: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newGroupName by remember { mutableStateOf("") }
    var joinInviteCode by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = CardWhite,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
        )
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SHARED DINING",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = group?.name ?: "Household / Flat Circle",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                if (group != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SageSurface
                        ) {
                            Text(
                                text = "Code: ${group.inviteCode}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestHeroMid,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        TextButton(
                            onClick = onLeaveGroup,
                            enabled = !isManagingGroup,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Leave", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoseRed)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newGroupName,
                    onValueChange = { newGroupName = it },
                    placeholder = { Text("New circle (e.g. Flat 4B)", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = sortMyPlateInputColors()
                )
                Button(
                    onClick = {
                        onCreateGroup(newGroupName)
                        newGroupName = ""
                    },
                    enabled = !isManagingGroup && newGroupName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForestHeroDeep,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Create", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = joinInviteCode,
                    onValueChange = { joinInviteCode = it },
                    placeholder = { Text("Invite code", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = sortMyPlateInputColors()
                )
                OutlinedButton(
                    onClick = {
                        onJoinGroup(joinInviteCode)
                        joinInviteCode = ""
                    },
                    enabled = !isManagingGroup && joinInviteCode.isNotBlank(),
                    shape = RoundedCornerShape(50),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(ForestHeroMid)
                    )
                ) {
                    Text("Join", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForestHeroDeep)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Check who is eating tonight — recommendations balance everyone's markers & allergies automatically:",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (group == null || group.members.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CreamSurface,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No circle joined yet — currently planning meals in Solo Mode.",
                        fontSize = 12.5.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    group.members.forEach { member ->
                        val isChecked = activeMemberIds.contains(member.id)
                        val isYou = member.id == currentUserId

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isChecked) SageSurface else CreamSurface,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (isChecked) EmeraldPrimary.copy(alpha = 0.55f) else CardBorder
                                )
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleMember(member.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(ForestHeroDeep)
                                            .border(1.5.dp, EmeraldPrimary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = member.fullName.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = member.fullName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            if (isYou) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "(You)",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = EmeraldPrimary
                                                )
                                            }
                                        }
                                        if (member.allergies.isNotEmpty()) {
                                            Text(
                                                text = "Allergy: ${member.allergies.joinToString(", ")}",
                                                fontSize = 11.sp,
                                                color = RoseRed
                                            )
                                        } else {
                                            Text(
                                                text = member.email,
                                                fontSize = 11.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }

                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { onToggleMember(member.id) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = ForestHeroDeep,
                                        checkmarkColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSafeguardsCard(
    user: AuthResponse,
    isUpdatingProfile: Boolean,
    onUpdateProfile: (allergiesRaw: String, dietPreference: String) -> Unit
) {
    var dietPreference by remember(user.dietPreference) { mutableStateOf(user.dietPreference.ifBlank { "omnivore" }) }
    var allergiesText by remember(user.allergies) { mutableStateOf(user.allergies.joinToString(", ")) }
    val dietOptions = listOf(
        "omnivore" to "Omnivore",
        "pescatarian" to "Pescatarian",
        "vegetarian" to "Vegetarian",
        "vegan" to "Vegan"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = CardWhite,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(ForestHeroDeep)
                        .border(2.dp, EmeraldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.fullName.take(1).uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.fullName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = user.email,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Text(
                text = "DIETARY PREFERENCE",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.8.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                dietOptions.forEach { (key, label) ->
                    val selected = dietPreference.equals(key, ignoreCase = true)
                    FilterChip(
                        selected = selected,
                        onClick = { dietPreference = key },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.5.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestHeroDeep,
                            selectedLabelColor = Color.White,
                            containerColor = CreamSurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = if (selected) ForestHeroDeep else CardBorder
                        )
                    )
                }
            }

            OutlinedTextField(
                value = allergiesText,
                onValueChange = { allergiesText = it },
                label = { Text("Strict Allergen Guardrails (comma-separated)") },
                placeholder = { Text("peanuts, shellfish, dairy") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = sortMyPlateInputColors()
            )

            Button(
                onClick = { onUpdateProfile(allergiesText, dietPreference) },
                enabled = !isUpdatingProfile,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SageSurface,
                    contentColor = ForestHeroDeep
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isUpdatingProfile) "Saving Safeguards..." else "Save Dietary Safeguards",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun VaultSessionCard(
    user: AuthResponse,
    discoveredBaseUrl: String,
    discoveredEndpointCount: Int,
    networkTxCount: Int,
    hasNetworkErrors: Boolean,
    onOpenGuide: () -> Unit,
    onReplayOnboarding: () -> Unit,
    onOpenNetworkInspector: () -> Unit,
    onSignOut: () -> Unit
) {
    val showDebugNetwork = isDebugBuild()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = CardWhite,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "ZERO-TRUST PHI VAULT",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary,
                letterSpacing = 0.8.sp
            )
            Text(
                text = "Your Google identity stays in Zone A while blood markers remain encrypted in Zone B via AES-256-GCM.",
                fontSize = 12.5.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = CreamSurface,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VaultStatRow(label = "Decoupled Subject Token", value = user.subjectToken)
                    VaultStatRow(label = "Discovered Backend", value = discoveredBaseUrl)
                    VaultStatRow(label = "Session Endpoints", value = "$discoveredEndpointCount verified")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenGuide,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Safety Guide", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForestHeroDeep)
                }
                OutlinedButton(
                    onClick = onReplayOnboarding,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Welcome Tour", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForestHeroDeep)
                }
            }

            if (showDebugNetwork) {
                val netAccent = if (hasNetworkErrors) RoseRed else EmeraldPrimary
                OutlinedButton(
                    onClick = onOpenNetworkInspector,
                    shape = RoundedCornerShape(50),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(netAccent.copy(alpha = 0.45f))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Network Inspector ($networkTxCount)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = netAccent
                    )
                }
            }

            TextButton(
                onClick = onSignOut,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Sign Out", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = RoseRed)
            }
        }
    }
}

@Composable
private fun VaultStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.5.sp, color = TextMuted)
        Text(text = value, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}
