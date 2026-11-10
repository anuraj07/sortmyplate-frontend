package io.sortmyplate.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sortmyplate.model.BiomarkerObservation
import io.sortmyplate.model.BiomarkerPanelRecord
import io.sortmyplate.theme.*
import io.sortmyplate.viewmodel.BiomarkerPreset
import io.sortmyplate.viewmodel.SUPPORTED_BIOMARKER_PRESETS

@Composable
fun BiomarkersView(
    panel: BiomarkerPanelRecord?,
    isSavingBiomarker: Boolean,
    onSaveBiomarker: (BiomarkerPreset, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPreset by remember { mutableStateOf(SUPPORTED_BIOMARKER_PRESETS.first()) }
    var markerValueText by remember { mutableStateOf("") }

    val observations = panel?.observations?.values?.toList().orEmpty()
    val attentionNames = observations
        .filter { it.status != "OPTIMAL" && it.status != "NORMAL" }
        .map { it.displayName }

    val narrative = when {
        observations.isEmpty() ->
            "Enter a lab reading below or upload your diagnostic report via the web portal. All values sit encrypted in Zone B via AES-256-GCM."
        attentionNames.isEmpty() ->
            "Laboratory panels show metabolic, lipid, inflammatory, and iron markers within healthy clinical reference intervals."
        else ->
            "SortMyPlate meal recommendations are actively balancing ${attentionNames.joinToString(", ")} while keeping shared family recipes effortless."
    }

    // Inspired directly by Screen 1 warm Cream-to-Sage Blood card
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = CreamSurface,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(CreamSurface, SageSurface))
                )
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                        Text(
                            text = "Blood",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = narrative,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 19.sp
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFDFECE0)
                    ) {
                        Text(
                            text = panel?.labProvider ?: "PHI Vault Ready",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestHeroDeep,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (observations.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CardWhite,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "No biomarker observations yet",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Add your first biomarker reading below (for example ApoB or HbA1c) to see colored clinical spectrum ranges.",
                                fontSize = 12.5.sp,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        observations.forEach { obs ->
                            SpectrumBiomarkerCard(obs)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Record / Update Marker Card
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = CardWhite,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Add or Update a Marker",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Values merge cleanly into your latest encrypted panel.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SUPPORTED_BIOMARKER_PRESETS.forEach { preset ->
                                val active = selectedPreset.code == preset.code
                                FilterChip(
                                    selected = active,
                                    onClick = { selectedPreset = preset },
                                    label = {
                                        Text(
                                            text = preset.code.replace("_", "-"),
                                            fontSize = 11.sp,
                                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
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
                                        selected = active,
                                        borderColor = if (active) ForestHeroDeep else CardBorder
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = markerValueText,
                                onValueChange = { markerValueText = it },
                                placeholder = { Text("${selectedPreset.displayName} (${selectedPreset.unit})", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = sortMyPlateInputColors()
                            )
                            Button(
                                onClick = {
                                    onSaveBiomarker(selectedPreset, markerValueText)
                                    markerValueText = ""
                                },
                                enabled = !isSavingBiomarker && markerValueText.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ForestHeroDeep,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                            ) {
                                Text(
                                    text = if (isSavingBiomarker) "Saving..." else "Save",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
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
private fun SpectrumBiomarkerCard(obs: BiomarkerObservation) {
    val (statusBg, statusFg) = when (obs.status) {
        "HIGH", "CRITICAL_HIGH" -> RoseRed.copy(alpha = 0.14f) to Color(0xFFAA2514)
        "LOW", "CRITICAL_LOW" -> AmberGold.copy(alpha = 0.16f) to Color(0xFF8F5305)
        else -> EmeraldPrimary.copy(alpha = 0.14f) to Color(0xFF196B3D)
    }

    val low = obs.referenceInterval?.low ?: 0.0
    val high = obs.referenceInterval?.high ?: kotlin.math.max(obs.numericValue * 1.4, 100.0)
    val span = kotlin.math.max(high - low, 1.0)
    val ratio = ((0.32 + ((obs.numericValue - low) / span) * 0.50).coerceIn(0.06, 0.94)).toFloat()

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CardWhite,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = obs.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "VN: ${formatBound(low)}–${formatBound(high)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${obs.numericValue} ${obs.unit}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Surface(shape = RoundedCornerShape(50), color = statusBg) {
                    Text(
                        text = obs.status,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusFg,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Signature Horizontal Multi-Zone Spectrum Bar
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
            ) {
                val weights = listOf(0.28f, 0.22f, 0.16f, 0.22f, 0.12f)
                val colors = listOf(CoralRed, AmberGold, LimeHealth, EmeraldPrimary, VioletRange)
                val gap = 4.dp.toPx()
                val usableW = size.width - gap * (weights.size - 1)
                val barTop = (size.height - 11.dp.toPx()) / 2f
                val barH = 11.dp.toPx()

                var cursor = 0f
                weights.forEachIndexed { idx, w ->
                    val segW = usableW * w
                    drawRoundRect(
                        color = colors[idx],
                        topLeft = Offset(cursor, barTop),
                        size = Size(segW, barH),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                    cursor += segW + gap
                }

                // Positioned marker pin
                val pinX = size.width * ratio
                val pinCenter = Offset(pinX, size.height / 2f)
                drawCircle(color = Color.White, radius = 7.dp.toPx(), center = pinCenter)
                drawCircle(
                    color = ForestHeroDeep,
                    radius = 7.dp.toPx(),
                    center = pinCenter,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = obs.category, fontSize = 11.sp, color = TextMuted)
                Text(text = "LOINC ${obs.loincCode ?: obs.code}", fontSize = 11.sp, color = TextMuted)
            }
        }
    }
}

private fun formatBound(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
}
