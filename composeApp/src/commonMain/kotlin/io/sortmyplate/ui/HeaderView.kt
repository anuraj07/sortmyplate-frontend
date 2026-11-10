package io.sortmyplate.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sortmyplate.model.AuthResponse
import io.sortmyplate.theme.*

@Composable
fun HeaderView(
    user: AuthResponse?,
    llmStatusLabel: String,
    networkTxCount: Int,
    hasNetworkErrors: Boolean,
    onOpenNetworkInspector: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🥗",
                        fontSize = 28.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column {
                        Text(
                            text = "SortMyPlate",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "ZERO-TRUST BIOMARKER ENGINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyanAccent,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Always-visible Network Inspector trigger pill
                val inspectorAccent = if (hasNetworkErrors) RoseRed else CyanAccent
                Surface(
                    shape = RoundedCornerShape(50),
                    color = inspectorAccent.copy(alpha = 0.14f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(inspectorAccent.copy(alpha = 0.45f))
                    ),
                    modifier = Modifier.clickable(onClick = onOpenNetworkInspector)
                ) {
                    Text(
                        text = "📡 Net ($networkTxCount)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = inspectorAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = EmeraldGreen.copy(alpha = 0.12f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EmeraldGreen.copy(alpha = 0.3f)))
                ) {
                    Text(
                        text = "🔒 AES-256-GCM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = PurpleAccent.copy(alpha = 0.12f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PurpleAccent.copy(alpha = 0.3f)))
                ) {
                    Text(
                        text = "🧠 $llmStatusLabel",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurpleAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                if (user != null) {
                    TextButton(
                        onClick = onSignOut,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = TextPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = "${user.fullName} • Sign Out", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
