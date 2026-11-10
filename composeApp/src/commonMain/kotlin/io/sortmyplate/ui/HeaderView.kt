package io.sortmyplate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sortmyplate.model.AuthResponse
import io.sortmyplate.theme.*

@Composable
fun HeaderView(
    user: AuthResponse?,
    onResetUser: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = EmeraldGreen.copy(alpha = 0.12f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EmeraldGreen.copy(alpha = 0.3f)))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔒 ", fontSize = 11.sp)
                        Text(
                            text = "AES-256-GCM PHI Vault",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }
                }

                if (user != null) {
                    Button(
                        onClick = onResetUser,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = user.fullName, fontSize = 12.sp, color = TextPrimary)
                    }
                }
            }
        }
    }
}
