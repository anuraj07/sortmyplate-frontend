package io.sortmyplate.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun SignInView(
    isBootstrapping: Boolean,
    isBootstrapped: Boolean,
    discoveredBaseUrl: String,
    discoveredEndpointCount: Int,
    isAuthLoading: Boolean,
    onRetryDiscovery: () -> Unit,
    onSignIn: (email: String, name: String, allergiesRaw: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF162036)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(CyanAccent.copy(alpha = 0.4f), PurpleAccent.copy(alpha = 0.35f)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sign in to SortMyPlate",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Connect your Google account to securely manage your blood biomarkers and get instant meal guidance.",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Endpoint Discovery Status Chip
            val statusColor = when {
                isBootstrapped -> EmeraldGreen
                isBootstrapping -> CyanAccent
                else -> RoseRed
            }
            val statusText = when {
                isBootstrapped -> "📡 Discovered $discoveredEndpointCount session endpoints • $discoveredBaseUrl"
                isBootstrapping -> "📡 Discovering session endpoints from $discoveredBaseUrl..."
                else -> "⚠️ Endpoint discovery offline"
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = statusColor.copy(alpha = 0.1f),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(statusColor.copy(alpha = 0.3f))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor,
                        modifier = Modifier.weight(1f)
                    )
                    if (!isBootstrapping && !isBootstrapped) {
                        TextButton(onClick = onRetryDiscovery) {
                            Text("Retry", fontSize = 11.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Google Email Address") },
                placeholder = { Text("you@gmail.com") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = sortMyPlateInputColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                placeholder = { Text("Your Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = sortMyPlateInputColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = allergies,
                onValueChange = { allergies = it },
                label = { Text("Food Allergies (comma separated, optional)") },
                placeholder = { Text("peanuts, shellfish") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = sortMyPlateInputColors()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onSignIn(email, fullName, allergies) },
                enabled = isBootstrapped && !isAuthLoading && email.isNotBlank() && fullName.isNotBlank(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanAccent,
                    contentColor = Color(0xFF0F172A),
                    disabledContainerColor = CardBorder,
                    disabledContentColor = TextMuted
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isAuthLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color(0xFF0F172A),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Signing In...", fontWeight = FontWeight.Bold)
                } else {
                    Text(
                        text = "🔐  Continue with Google",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun sortMyPlateInputColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CyanAccent,
    unfocusedBorderColor = CardBorder,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedLabelColor = CyanAccent,
    unfocusedLabelColor = TextSecondary,
    cursorColor = CyanAccent,
    focusedPlaceholderColor = TextMuted,
    unfocusedPlaceholderColor = TextMuted
)
