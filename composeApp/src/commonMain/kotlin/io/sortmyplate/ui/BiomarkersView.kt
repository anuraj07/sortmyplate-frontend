package io.sortmyplate.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🩸 ", fontSize = 16.sp)
                    Text(
                        text = "Latest Blood Panel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = panel?.labProvider ?: "No Report Yet",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldGreen
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "AES-256-GCM decrypted observations at runtime:",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (panel == null || panel.observations.isEmpty()) {
                Text(
                    text = "No biomarker report recorded yet. Add a marker below.",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    panel.observations.values.forEach { obs ->
                        BiomarkerRow(obs)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = CardBorder)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Record / Update Lab Marker",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SUPPORTED_BIOMARKER_PRESETS.forEach { preset ->
                    val isSelected = selectedPreset.code == preset.code
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPreset = preset },
                        label = {
                            Text(
                                text = preset.code.replace("_", "-"),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = Color(0xFF0F172A),
                            containerColor = Color(0xFF0F172A),
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) CyanAccent else CardBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BiomarkerRow(obs: BiomarkerObservation) {
    val (statusBg, statusFg) = when (obs.status) {
        "HIGH", "CRITICAL_HIGH" -> RoseRed.copy(alpha = 0.15f) to RoseRed
        "LOW", "CRITICAL_LOW" -> AmberYellow.copy(alpha = 0.15f) to AmberYellow
        else -> EmeraldGreen.copy(alpha = 0.15f) to EmeraldGreen
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = obs.displayName,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "LOINC: ${obs.loincCode ?: obs.code}",
                fontSize = 10.sp,
                color = TextMuted
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "${obs.numericValue} ${obs.unit}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Surface(
                shape = RoundedCornerShape(50),
                color = statusBg
            ) {
                Text(
                    text = obs.status,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusFg,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}
