package io.sortmyplate.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.sortmyplate.model.BiomarkerPanelRecord
import io.sortmyplate.theme.*
import io.sortmyplate.viewmodel.InsightPillar
import io.sortmyplate.viewmodel.InsightPoint

@Composable
fun InsightsView(
    panel: BiomarkerPanelRecord?,
    onOpenGuide: () -> Unit,
    onSelectInsight: (InsightPillar) -> Unit,
    modifier: Modifier = Modifier
) {
    val obsMap = panel?.observations.orEmpty()
    val pillars = remember(panel) {
        val hba1c = obsMap["HBA1C"]
        val crp = obsMap["HS_CRP"]
        val apob = obsMap["APOB"]
        val ferritin = obsMap["FERRITIN"]

        listOf(
            InsightPillar(
                id = "nutrition",
                kicker = "Glycemic & Fiber Pillar",
                title = "Nutrition & HbA1c",
                bigValue = hba1c?.let { "${it.numericValue}%" } ?: "94",
                statusBadge = hba1c?.status ?: "Steady",
                wide = false,
                paletteKey = "crimson",
                summary = "Low-glycemic load plates rich in soluble fiber prevent sharp glucose swings after meals.",
                points = listOf(
                    InsightPoint(
                        title = "Why daily consistency works",
                        body = "Sequencing protein and soluble fiber first blunts post-meal glucose spikes by up to 30%."
                    ),
                    InsightPoint(
                        title = "Your lab context",
                        body = hba1c?.let { "Recorded HbA1c is ${it.numericValue}% (${it.status})." }
                            ?: "No HbA1c recorded yet — SortMyPlate keeps glycemic load gentle by default."
                    )
                )
            ),
            InsightPillar(
                id = "inflammation",
                kicker = "Cellular Recovery",
                title = "Inflammation",
                bigValue = crp?.let { "${it.numericValue}" } ?: "82",
                statusBadge = crp?.unit ?: "mg/L Focus",
                wide = false,
                paletteKey = "amber",
                summary = "Anti-inflammatory omega-3 lipids, turmeric, and polyphenols calm systemic inflammation over time.",
                points = listOf(
                    InsightPoint(
                        title = "Daily inflammation lever",
                        body = "Swapping refined seed oils for extra-virgin olive oil and omega-3 fish lowers oxidative stress."
                    ),
                    InsightPoint(
                        title = "Your hs-CRP signal",
                        body = crp?.let { "hs-CRP reads ${it.numericValue} ${it.unit} (${it.status})." }
                            ?: "Add hs-CRP in Blood Lab to calibrate anti-inflammatory boosts."
                    )
                )
            ),
            InsightPillar(
                id = "lipids",
                kicker = "Cardiometabolic Longevity",
                title = "Lipids & ApoB Balance",
                bigValue = apob?.let { "${it.numericValue} mg/dL" }
                    ?: if (obsMap.isNotEmpty()) "${obsMap.size} Markers" else "Ready",
                statusBadge = apob?.status ?: "AES-256 Protected",
                wide = true,
                paletteKey = "sage",
                summary = "Lowering ApoB and LDL-C through oat beta-glucan, legumes, nuts, and lean protein supports vascular longevity.",
                points = listOf(
                    InsightPoint(
                        title = "How SortMyPlate adapts meals",
                        body = "When ApoB or LDL-C trends high, meals prioritize plant sterols and viscous fibers that bind bile cholesterol."
                    ),
                    InsightPoint(
                        title = "Privacy guarantee",
                        body = "Your lipid values remain isolated behind your synthetic subject token — never stored alongside identity."
                    )
                )
            ),
            InsightPillar(
                id = "iron",
                kicker = "Oxygen & Energy",
                title = "Iron & Ferritin",
                bigValue = ferritin?.let { "${it.numericValue}" } ?: "Vital",
                statusBadge = ferritin?.status ?: "Track Iron",
                wide = false,
                paletteKey = "sky",
                summary = "Strategic Vitamin-C acidic dressings double non-heme plant iron absorption when ferritin dips.",
                points = listOf(
                    InsightPoint(
                        title = "Smart plate pairing",
                        body = "SortMyPlate finishes lentil or greens bowls with citrus zest or pickled veggies for individuals with low ferritin."
                    ),
                    InsightPoint(
                        title = "Your ferritin marker",
                        body = ferritin?.let { "Serum Ferritin is ${it.numericValue} ${it.unit} (${it.status})." }
                            ?: "Add Ferritin in Blood Lab if you track fatigue or endurance."
                    )
                )
            ),
            InsightPillar(
                id = "compounding",
                kicker = "Preventive Routine",
                title = "How Daily Meals Improve 6-Month Labs",
                bigValue = "3x / day",
                statusBadge = "Daily Guide",
                wide = true,
                paletteKey = "olive",
                summary = "Every breakfast, lunch, and dinner compounds small vascular, glycemic, and gut microbiome wins across 180 days.",
                points = listOf(
                    InsightPoint(
                        title = "Zero-Friction Shared Cooking",
                        body = "One shared household recipe keeps group prep fast while custom finishing toppings address individual labs."
                    ),
                    InsightPoint(
                        title = "Safe by Construction",
                        body = "Strict allergen exclusion runs deterministically before clinical AI explanations are generated."
                    )
                )
            )
        )
    }

    // Nixtio Deep Moss "Your Insights" Bento Shell
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = BgDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(ForestHeroDeep, Color(0xFF0E1B14))))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                    Text(
                        text = "Your Insights",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Tap any pillar to see how daily meals reshape 6-month lab panels.",
                        fontSize = 12.5.sp,
                        color = Color(0xFFB7CEBE)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.14f),
                    modifier = Modifier.clickable(onClick = onOpenGuide)
                ) {
                    Text(
                        text = "Safety & Daily Guide",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }

            // Bento Grid: Row of 2 compact cards -> 1 wide -> compact + wide
            val firstPair = pillars.take(2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                firstPair.forEach { pillar ->
                    InsightBentoCard(
                        pillar = pillar,
                        onClick = { onSelectInsight(pillar) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            pillars.getOrNull(2)?.let { wideLipids ->
                InsightBentoCard(
                    pillar = wideLipids,
                    onClick = { onSelectInsight(wideLipids) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pillars.getOrNull(3)?.let { ironCard ->
                    InsightBentoCard(
                        pillar = ironCard,
                        onClick = { onSelectInsight(ironCard) },
                        modifier = Modifier.weight(1f)
                    )
                }
                pillars.getOrNull(4)?.let { dailyCard ->
                    InsightBentoCard(
                        pillar = dailyCard,
                        onClick = { onSelectInsight(dailyCard) },
                        modifier = Modifier.weight(1.35f)
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightBentoCard(
    pillar: InsightPillar,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = when (pillar.paletteKey) {
        "crimson" -> listOf(CrimsonTop, CrimsonBottom)
        "amber" -> listOf(AmberBentoTop, AmberBentoBottom)
        "sage" -> listOf(SageBentoTop, SageBentoBottom)
        "sky" -> listOf(SkyBentoTop, SkyBentoBottom)
        else -> listOf(OliveBentoTop, OliveBentoBottom)
    }

    Box(
        modifier = modifier
            .heightIn(min = if (pillar.wide) 142.dp else 164.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(gradient))
            .clickable(onClick = onClick)
    ) {
        // Subtle decorative organic line art rings in corner
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.width * 0.88f, size.height * 0.28f)
            drawCircle(
                color = Color.White.copy(alpha = 0.14f),
                radius = size.minDimension * 0.32f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.09f),
                radius = size.minDimension * 0.48f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = pillar.kicker,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.86f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = pillar.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    lineHeight = 21.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pillar.bigValue,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = pillar.statusBadge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.94f)
                ) {
                    Text(
                        text = "Details →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InsightDetailDialog(
    pillar: InsightPillar,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 540.dp),
            shape = RoundedCornerShape(28.dp),
            color = CardWhite,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
            )
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SageSurface
                    ) {
                        Text(
                            text = pillar.kicker.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ForestHeroMid,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Close", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                    }
                }

                Text(
                    text = pillar.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = pillar.bigValue,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ForestHeroDeep
                    )
                    Surface(shape = RoundedCornerShape(50), color = SageSurface) {
                        Text(
                            text = pillar.statusBadge,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestHeroMid,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = pillar.summary,
                    fontSize = 13.5.sp,
                    color = TextSecondary,
                    lineHeight = 19.sp
                )

                pillar.points.forEach { pt ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CreamSurface,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = pt.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = pt.body,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
