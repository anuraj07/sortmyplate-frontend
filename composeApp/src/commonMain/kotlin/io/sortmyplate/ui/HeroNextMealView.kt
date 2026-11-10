package io.sortmyplate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sortmyplate.theme.*

@Composable
fun HeroNextMealView(
    selectedMealSlot: String,
    onSelectMealSlot: (String) -> Unit,
    onGetNextMeal: () -> Unit,
    isLoading: Boolean,
    subjectToken: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF162036)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(CyanAccent.copy(alpha = 0.4f), PurpleAccent.copy(alpha = 0.3f)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "What should I eat next?",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Single tap recommends the optimal meal balanced for your 6-month blood biomarkers and dynamic household.",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Meal Slot Selector Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val slots = listOf("BREAKFAST" to "🌅 Breakfast", "LUNCH" to "☀️ Lunch", "DINNER" to "🌙 Dinner", "SNACK" to "🍎 Snack")
                slots.forEach { (slotKey, label) ->
                    val isSelected = selectedMealSlot == slotKey
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectMealSlot(slotKey) },
                        label = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
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

            Spacer(modifier = Modifier.height(20.dp))

            // Big 1-Click Action Button
            Button(
                onClick = onGetNextMeal,
                enabled = !isLoading,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanAccent,
                    contentColor = Color(0xFF0F172A)
                ),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
                modifier = Modifier.height(52.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF0F172A),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Consulting Biomarkers...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "✨  Get Next Meal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (!subjectToken.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "🔒 Decoupled Medical Token: $subjectToken",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}
