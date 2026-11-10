package io.sortmyplate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sortmyplate.model.IndividualClinicalRationale
import io.sortmyplate.model.NextMealResponse
import io.sortmyplate.theme.*

@Composable
fun MealResultView(
    meal: NextMealResponse,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyanAccent.copy(alpha = 0.4f)))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Allergen Safeguard Banner
            if (meal.allergensStrictlyAvoided.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = EmeraldGreen.copy(alpha = 0.12f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EmeraldGreen.copy(alpha = 0.3f))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🛡️ ", fontSize = 14.sp)
                        Text(
                            text = "Safety Safeguard: Strictly avoided allergens: ${meal.allergensStrictlyAvoided.joinToString(", ")}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldGreen
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Meal Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = meal.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = meal.description,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF1E293B)
                ) {
                    Text(
                        text = "⏱️ ${meal.prepTimeMinutes} mins",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Macro Grid (Calories, Protein, Fiber, Glycemic Load)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val calories = meal.macrosPerServing["calories"]?.toInt() ?: 0
                val protein = meal.macrosPerServing["protein_g"] ?: 0.0
                val fiber = meal.macrosPerServing["fiber_g"] ?: 0.0
                val gl = meal.macrosPerServing["glycemic_load"] ?: 0.0

                MacroBox(value = "$calories kcal", label = "Calories", modifier = Modifier.weight(1f))
                MacroBox(value = "${protein}g", label = "Protein", modifier = Modifier.weight(1f))
                MacroBox(value = "${fiber}g", label = "Fiber", modifier = Modifier.weight(1f))
                MacroBox(value = "$gl", label = "Glycemic Load", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Household Consensus
            Text(
                text = "🤝 Household Consensus Summary",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = meal.overallConsensusReasoning,
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Ingredients
            Text(
                text = "🥣 Shared Base Ingredients",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            meal.sharedBaseIngredients.forEach { ing ->
                Text(
                    text = "• ${ing.name} — ${ing.amount} ${ing.unit}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Clinical Reasoning Cards
            Text(
                text = "🔬 Individual Clinical Reasoning & Mechanisms",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CyanAccent
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                meal.memberCustomizations.forEach { rationale ->
                    ClinicalRationaleCard(rationale)
                }
            }
        }
    }
}

@Composable
fun MacroBox(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0F172A),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CyanAccent
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
fun ClinicalRationaleCard(rationale: IndividualClinicalRationale) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyanAccent.copy(alpha = 0.3f)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👤 ${rationale.memberName}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Target: ${rationale.targetedBiomarkers.joinToString(", ")}",
                    fontSize = 11.sp,
                    color = EmeraldGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = rationale.mechanism,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            if (!rationale.suggestedCustomFinish.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CyanAccent.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "✨ Custom Finish: ${rationale.suggestedCustomFinish}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyanAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
