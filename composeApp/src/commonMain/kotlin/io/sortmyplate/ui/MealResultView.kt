package io.sortmyplate.ui

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
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = CardWhite,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            if (meal.allergensStrictlyAvoided.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = EmeraldPrimary.copy(alpha = 0.1f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(EmeraldPrimary.copy(alpha = 0.3f))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🛡️ Allergen Guardrail: Strictly avoided ${meal.allergensStrictlyAvoided.joinToString(", ")}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestHeroMid,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                    Text(
                        text = "PERSONALIZED CLINICAL RECOMMENDATION",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = meal.title,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
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
                    color = SageSurface
                ) {
                    Text(
                        text = "⏱️ ${meal.prepTimeMinutes}m",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestHeroDeep,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val calories = meal.macrosPerServing["calories"]?.toInt() ?: 0
                val protein = meal.macrosPerServing["protein_g"] ?: 0.0
                val fiber = meal.macrosPerServing["fiber_g"] ?: 0.0
                val gl = meal.macrosPerServing["glycemic_load"] ?: 0.0

                MacroBox(value = "$calories", label = "kcal", modifier = Modifier.weight(1f))
                MacroBox(value = "${protein}g", label = "Protein", modifier = Modifier.weight(1f))
                MacroBox(value = "${fiber}g", label = "Fiber", modifier = Modifier.weight(1f))
                MacroBox(value = "$gl", label = "Glycemic", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Household Consensus",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = meal.overallConsensusReasoning,
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Shared Base Ingredients",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            meal.sharedBaseIngredients.forEach { ing ->
                Text(
                    text = "• ${ing.name} — ${ing.amount} ${ing.unit}",
                    fontSize = 12.5.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Individual Clinical Mechanisms",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ForestHeroMid
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
        shape = RoundedCornerShape(14.dp),
        color = CreamSurface,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ForestHeroDeep
            )
            Text(
                text = label,
                fontSize = 10.5.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
fun ClinicalRationaleCard(rationale: IndividualClinicalRationale) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CreamSurface,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
        )
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
                    text = rationale.targetedBiomarkers.joinToString(", "),
                    fontSize = 11.sp,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = rationale.mechanism,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )

            if (!rationale.suggestedCustomFinish.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = SageSurface
                ) {
                    Text(
                        text = "✨ Finish: ${rationale.suggestedCustomFinish}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestHeroMid,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
