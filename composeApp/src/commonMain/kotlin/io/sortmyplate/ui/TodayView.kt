package io.sortmyplate.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sortmyplate.model.*
import io.sortmyplate.theme.*
import io.sortmyplate.viewmodel.AppSection
import kotlin.math.roundToInt

@Composable
fun TodayView(
    user: AuthResponse,
    panel: BiomarkerPanelRecord?,
    group: GroupResponse?,
    activeMemberIds: Set<String>,
    selectedMealSlot: String,
    isLoadingMeal: Boolean,
    currentMeal: NextMealResponse?,
    onSelectMealSlot: (String) -> Unit,
    onGetNextMeal: () -> Unit,
    onNavigateSection: (AppSection) -> Unit,
    onOpenGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val obsList = panel?.observations?.values?.toList().orEmpty()
    val attentionCount = obsList.count { it.status != "OPTIMAL" && it.status != "NORMAL" }
    val readinessScore = if (obsList.isEmpty()) {
        86
    } else {
        val total = obsList.fold(0) { acc, obs ->
            acc + when (obs.status) {
                "OPTIMAL" -> 98
                "NORMAL" -> 88
                else -> 64
            }
        }
        (total.toDouble() / obsList.size).roundToInt()
    }
    val deltaLabel = when {
        obsList.isEmpty() -> "Ready • Add Blood Lab to personalize"
        attentionCount == 0 -> "${obsList.size} Biomarkers Optimal"
        else -> "$attentionCount Marker${if (attentionCount > 1) "s" else ""} Prioritized Today"
    }

    val dinersChipLabel = when {
        group != null && activeMemberIds.isNotEmpty() -> "${group.name} • ${activeMemberIds.size} eating"
        group != null -> "${group.name} • 0 eating"
        else -> "Solo Mode"
    }

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
                    HeroBioCard(
                        user = user,
                        score = readinessScore,
                        deltaLabel = deltaLabel,
                        dinersChipLabel = dinersChipLabel,
                        selectedMealSlot = selectedMealSlot,
                        isLoadingMeal = isLoadingMeal,
                        onSelectMealSlot = onSelectMealSlot,
                        onGetNextMeal = onGetNextMeal,
                        onOpenGuide = onOpenGuide,
                        modifier = Modifier.weight(1.15f)
                    )
                    TodayBentoSideColumn(
                        obsCount = obsList.size,
                        attentionCount = attentionCount,
                        group = group,
                        activeMemberCount = activeMemberIds.size,
                        subjectToken = user.subjectToken,
                        onNavigateSection = onNavigateSection,
                        modifier = Modifier.weight(0.85f)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HeroBioCard(
                        user = user,
                        score = readinessScore,
                        deltaLabel = deltaLabel,
                        dinersChipLabel = dinersChipLabel,
                        selectedMealSlot = selectedMealSlot,
                        isLoadingMeal = isLoadingMeal,
                        onSelectMealSlot = onSelectMealSlot,
                        onGetNextMeal = onGetNextMeal,
                        onOpenGuide = onOpenGuide
                    )
                    TodayBentoSideColumn(
                        obsCount = obsList.size,
                        attentionCount = attentionCount,
                        group = group,
                        activeMemberCount = activeMemberIds.size,
                        subjectToken = user.subjectToken,
                        onNavigateSection = onNavigateSection
                    )
                }
            }
        }

        currentMeal?.let { meal ->
            MealResultView(meal = meal)
        }
    }
}

@Composable
private fun HeroBioCard(
    user: AuthResponse,
    score: Int,
    deltaLabel: String,
    dinersChipLabel: String,
    selectedMealSlot: String,
    isLoadingMeal: Boolean,
    onSelectMealSlot: (String) -> Unit,
    onGetNextMeal: () -> Unit,
    onOpenGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val firstName = user.fullName.trim().split(" ").firstOrNull().orEmpty().ifBlank { "Friend" }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = ForestHeroDeep
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(ForestHeroDeep, Color(0xFF17201B)))
                )
        ) {
            // Bioluminescent radial aura behind biological/plate readiness score
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            EmeraldGlow.copy(alpha = 0.42f),
                            EmeraldPrimary.copy(alpha = 0.16f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.30f),
                        radius = size.maxDimension * 0.48f
                    )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Row: Avatar + Greeting + ? Help trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ForestHeroMid)
                                .border(1.5.dp, EmeraldGlow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = firstName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Hello, $firstName",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = user.email,
                                color = Color(0xFFD2E8D8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape)
                            .clickable(onClick = onOpenGuide),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "?",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Plate & Metabolic Readiness",
                    fontSize = 13.sp,
                    color = Color(0xFFE3F5E8),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$score",
                    fontSize = 60.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.09f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF84BE8D).copy(alpha = 0.5f))
                    )
                ) {
                    Text(
                        text = deltaLabel,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFDDF7E7),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Signature Nixtio White Tick Ruler
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .height(16.dp)
                ) {
                    val count = 28
                    val step = size.width / count
                    for (i in 0..count) {
                        val x = i * step
                        val edgeFade = 1f - kotlin.math.abs((i - count / 2f) / (count / 2f)) * 0.72f
                        val isCenterTick = i % 5 == 0
                        val tickH = if (isCenterTick) size.height * 0.85f else size.height * 0.48f
                        drawLine(
                            color = Color.White.copy(alpha = (0.5f * edgeFade).coerceIn(0.1f, 0.7f)),
                            start = Offset(x, size.height),
                            end = Offset(x, size.height - tickH),
                            strokeWidth = if (isCenterTick) 2f else 1.2f
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Glass Next Meal Action Dock
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.14f))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "What should I eat next?",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Matched to labs & active diners",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFFD2E8D8)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = dinersChipLabel,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFE6F4EA),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val slots = listOf(
                                "BREAKFAST" to "🌅 Breakfast",
                                "LUNCH" to "☀️ Lunch",
                                "DINNER" to "🌙 Dinner",
                                "SNACK" to "🍎 Snack"
                            )
                            slots.forEach { (slotKey, label) ->
                                val active = selectedMealSlot == slotKey
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (active) Color.White else Color(0x660F1B14),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(
                                            if (active) Color.White else Color.White.copy(alpha = 0.2f)
                                        )
                                    ),
                                    modifier = Modifier.clickable { onSelectMealSlot(slotKey) }
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                        color = if (active) ForestHeroDeep else Color(0xFFE8F5EB),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onGetNextMeal,
                            enabled = !isLoadingMeal,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD6F0DC),
                                contentColor = ForestHeroDeep
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isLoadingMeal) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = ForestHeroDeep,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Balancing Biomarkers...", fontWeight = FontWeight.ExtraBold)
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Get Next Meal",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(ForestHeroDeep),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("→", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayBentoSideColumn(
    obsCount: Int,
    attentionCount: Int,
    group: GroupResponse?,
    activeMemberCount: Int,
    subjectToken: String,
    onNavigateSection: (AppSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShortcutBentoTile(
                kicker = "PHI Lab Vault",
                title = if (attentionCount > 0) "$attentionCount Attention" else "Blood Lab",
                subtitle = if (obsCount > 0) "$obsCount markers encrypted" else "Add ApoB / HbA1c",
                pillLabel = if (obsCount > 0) "$obsCount Markers →" else "Add Panel →",
                topColor = SageBentoTop,
                bottomColor = SageBentoBottom,
                onClick = { onNavigateSection(AppSection.BLOOD) },
                modifier = Modifier.weight(1f)
            )
            ShortcutBentoTile(
                kicker = "Shared Cooking",
                title = group?.name ?: "Solo Mode",
                subtitle = if (group != null) "$activeMemberCount/${group.members.size} dining" else "Create or join circle",
                pillLabel = "Manage →",
                topColor = AmberBentoTop,
                bottomColor = AmberBentoBottom,
                onClick = { onNavigateSection(AppSection.CIRCLE) },
                modifier = Modifier.weight(1f)
            )
        }

        // Wide Olive Bento for Insights
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(Brush.linearGradient(listOf(OliveBentoTop, OliveBentoBottom)))
                .clickable { onNavigateSection(AppSection.INSIGHTS) }
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                    Text(
                        text = "Preventive Trajectory",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = "Your Daily Insights",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "See how soluble fiber & glycemic pacing improve ApoB, HbA1c, and hs-CRP.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 16.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.94f)
                ) {
                    Text(
                        text = "Explore →",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        // Calm Encryption Note
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = CardWhite.copy(alpha = 0.9f),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🔒 Encrypted PHI Session",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = subjectToken,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldPrimary
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Blood panels remain detached from your Google identity in Zone B.",
                    fontSize = 11.5.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ShortcutBentoTile(
    kicker: String,
    title: String,
    subtitle: String,
    pillLabel: String,
    topColor: Color,
    bottomColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .heightIn(min = 152.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(listOf(topColor, bottomColor)))
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = kicker,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = Color.White.copy(alpha = 0.88f)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.94f)
            ) {
                Text(
                    text = pillLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                )
            }
        }
    }
}
