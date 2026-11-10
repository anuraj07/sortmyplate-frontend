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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sortmyplate.model.AuthResponse
import io.sortmyplate.theme.*

data class OnboardingSlide(
    val tabTitle: String,
    val badge: String,
    val badgeBg: Color,
    val badgeFg: Color,
    val headline: String,
    val subtitle: String,
    val items: List<Triple<String, String, String>>
)

val ONBOARDING_SLIDES = listOf(
    OnboardingSlide(
        tabTitle = "1. Safety & Privacy",
        badge = "ZERO-TRUST PHI ARCHITECTURE",
        badgeBg = Color(0x202B9A5B),
        badgeFg = Color(0xFF1A6D3F),
        headline = "Your blood data stays encrypted & detached from your name.",
        subtitle = "SortMyPlate isolates who you are from what your lab reports contain.",
        items = listOf(
            Triple("🔐", "AES-256-GCM Vault Encryption", "Blood markers (`ApoB`, `HbA1c`, `Ferritin`, `hs-CRP`) are encrypted before hitting storage."),
            Triple("🪪", "Identity-Decoupled Subject Tokens", "Your Google name & email live in Zone A; health records use an anonymous `sub_...` token in Zone B."),
            Triple("🛡️", "Deterministic Allergy Guardrails", "Household allergies are filtered in code before AI meal reasoning runs.")
        )
    ),
    OnboardingSlide(
        tabTitle = "2. How It Works",
        badge = "3 EFFORTLESS STEPS",
        badgeBg = Color(0xFFE1EDE2),
        badgeFg = Color(0xFF2C5C39),
        headline = "From blood test PDF to tonight's dinner in 60 seconds.",
        subtitle = "No rigid calorie spreadsheets — follow a calm weekly nutrition rhythm.",
        items = listOf(
            Triple("1", "Continue with Google", "One tap imports your verified name, email, and profile picture — never touches Gmail or Drive."),
            Triple("2", "Upload Lab PDF or Add Markers", "Import Quest, LabCorp, or hospital reports, or enter key markers cleanly in Blood Lab."),
            Triple("3", "Pick Diners & Tap Get Next Meal", "Solo or cooking with flatmates? Get one shared recipe plus custom clinical finishes.")
        )
    ),
    OnboardingSlide(
        tabTitle = "3. Daily Health",
        badge = "PREVENTIVE COMPOUNDING",
        badgeBg = Color(0x26E2911D),
        badgeFg = Color(0xFF8F5305),
        headline = "How small daily meals reshape your 6-month lab panels.",
        subtitle = "Gentle, evidence-based nutrition shifts compounded across breakfast, lunch, and dinner.",
        items = listOf(
            Triple("🌿", "Calm Glucose & HbA1c Curves", "High-soluble-fiber, low-glycemic pairings curb post-meal energy crashes."),
            Triple("💚", "Support Healthy ApoB & LDL-C", "Unsaturated fats and viscous plant fibers steadily balance lipid markers."),
            Triple("⚡", "Lower hs-CRP & Restore Iron", "Anti-inflammatory finishes and vitamin-C iron pairing lift daily vitality.")
        )
    )
)

@Composable
fun SignInView(
    isBootstrapping: Boolean,
    isBootstrapped: Boolean,
    discoveredBaseUrl: String,
    discoveredEndpointCount: Int,
    isAuthLoading: Boolean,
    isAwaitingGoogleBrowser: Boolean,
    oauthSetupHint: String?,
    onRetryDiscovery: () -> Unit,
    onContinueWithGoogle: () -> Unit,
    onCancelGoogleBrowser: () -> Unit,
    onFallbackSignIn: (email: String, name: String, allergiesRaw: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        GoogleAuthCard(
            isBootstrapping = isBootstrapping,
            isBootstrapped = isBootstrapped,
            discoveredBaseUrl = discoveredBaseUrl,
            discoveredEndpointCount = discoveredEndpointCount,
            isAuthLoading = isAuthLoading,
            isAwaitingGoogleBrowser = isAwaitingGoogleBrowser,
            oauthSetupHint = oauthSetupHint,
            onRetryDiscovery = onRetryDiscovery,
            onContinueWithGoogle = onContinueWithGoogle,
            onCancelGoogleBrowser = onCancelGoogleBrowser,
            onFallbackSignIn = onFallbackSignIn,
            modifier = Modifier.widthIn(max = 500.dp)
        )
    }
}

@Composable
fun PostLoginOnboardingView(
    user: AuthResponse,
    onboardingStep: Int,
    onSelectStep: (Int) -> Unit,
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val firstName = user.fullName.trim().substringBefore(" ").ifBlank { "Friend" }
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Synced Google Identity Strip + Skip Tour action
            Surface(
                shape = RoundedCornerShape(50),
                color = CardWhite.copy(alpha = 0.92f),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(ForestHeroDeep)
                                .border(1.5.dp, EmeraldPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.fullName.take(1).uppercase(),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Column {
                            Text(
                                text = "Welcome, $firstName",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${user.email} • Google Account Connected",
                                fontSize = 11.5.sp,
                                color = TextMuted
                            )
                        }
                    }

                    TextButton(onClick = onFinishOnboarding) {
                        Text(
                            text = "Skip Tour",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }
                }
            }

            OnboardingDeckCard(
                step = onboardingStep,
                onSelectStep = onSelectStep,
                onFinishOnboarding = onFinishOnboarding
            )
        }
    }
}

@Composable
fun OnboardingDeckCard(
    step: Int,
    onSelectStep: (Int) -> Unit,
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val current = ONBOARDING_SLIDES[step.coerceIn(0, ONBOARDING_SLIDES.lastIndex)]

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = CardWhite.copy(alpha = 0.94f),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ONBOARDING_SLIDES.forEachIndexed { idx, slide ->
                        val active = idx == step
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (active) ForestHeroDeep else CreamSurface,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(if (active) ForestHeroDeep else CardBorder)
                            ),
                            modifier = Modifier.clickable { onSelectStep(idx) }
                        ) {
                            Text(
                                text = slide.tabTitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) Color.White else TextSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${step + 1} / ${ONBOARDING_SLIDES.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = current.badgeBg
            ) {
                Text(
                    text = current.badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = current.badgeFg,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = current.headline,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = current.subtitle,
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                current.items.forEach { (badgeText, title, desc) ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CreamSurface,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(SageSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestHeroMid
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = desc,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = CardBorder)
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(ONBOARDING_SLIDES.size) { idx ->
                        val active = idx == step
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (active) 22.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (active) EmeraldPrimary else CardBorder)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (step > 0) {
                        OutlinedButton(
                            onClick = { onSelectStep(step - 1) },
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("Back", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                    val isLastSlide = step == ONBOARDING_SLIDES.lastIndex
                    Button(
                        onClick = {
                            if (isLastSlide) onFinishOnboarding()
                            else onSelectStep(step + 1)
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLastSlide) ForestHeroDeep else SageSurface,
                            contentColor = if (isLastSlide) Color.White else ForestHeroDeep
                        ),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isLastSlide) "Enter SortMyPlate →" else "Next",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleAuthCard(
    isBootstrapping: Boolean,
    isBootstrapped: Boolean,
    discoveredBaseUrl: String,
    discoveredEndpointCount: Int,
    isAuthLoading: Boolean,
    isAwaitingGoogleBrowser: Boolean,
    oauthSetupHint: String?,
    onRetryDiscovery: () -> Unit,
    onContinueWithGoogle: () -> Unit,
    onCancelGoogleBrowser: () -> Unit,
    onFallbackSignIn: (email: String, name: String, allergiesRaw: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFallback by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = CardWhite,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Bioluminescent Forest-Emerald Top Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(ForestHeroDeep, ForestHeroMid))
                    )
                    .padding(24.dp)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(EmeraldGlow.copy(alpha = 0.35f), Color.Transparent),
                            center = Offset(size.width * 0.8f, size.height * 0.2f),
                            radius = size.maxDimension * 0.65f
                        )
                    )
                }
                Column {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "Airy Longevity & Plate Companion",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFD4F4DE),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Sign in safely to begin",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Connect cleanly inside the app—no leaving SortMyPlate. Your verified Google name, email, and avatar sync automatically.",
                        fontSize = 13.sp,
                        color = Color(0xFFD2E8D8),
                        lineHeight = 18.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Continue with Google Official-Style Pill CTA
                OutlinedButton(
                    onClick = onContinueWithGoogle,
                    enabled = isBootstrapped && !isAuthLoading && !isAwaitingGoogleBrowser,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CardWhite,
                        contentColor = TextPrimary
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFC5D5C4))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (isAuthLoading || isAwaitingGoogleBrowser) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = EmeraldPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isAwaitingGoogleBrowser) "Waiting for Google Sign-In..." else "Connecting Google...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        GoogleEmblemIcon(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Continue with Google",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (isAwaitingGoogleBrowser) {
                    TextButton(
                        onClick = onCancelGoogleBrowser,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Cancel Google sign-in", fontSize = 12.sp, color = TextSecondary)
                    }
                }

                // Read-only OpenID reassurance chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CreamSurface,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
                    )
                ) {
                    Text(
                        text = "🔒 Read-only OpenID (`name`, `email`, `picture`). Includes a 30-second privacy & daily nutrition tour right after signing in.",
                        fontSize = 11.5.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(11.dp)
                    )
                }

                if (!oauthSetupHint.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AmberYellow.copy(alpha = 0.12f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(AmberYellow.copy(alpha = 0.4f))
                        )
                    ) {
                        Text(
                            text = oauthSetupHint,
                            fontSize = 11.5.sp,
                            color = Color(0xFF7C4704),
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Subtle Collapsible Direct Fallback (so main flow remains 100% uncluttered)
                HorizontalDivider(color = CardBorder)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showFallback = !showFallback }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Direct email sign-in fallback (when OAuth creds aren't set)",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                    Text(
                        text = if (showFallback) "Hide" else "Open",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }

                if (showFallback || !oauthSetupHint.isNullOrBlank()) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Google Email") },
                        placeholder = { Text("you@gmail.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = sortMyPlateInputColors()
                    )
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        placeholder = { Text("Your Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = sortMyPlateInputColors()
                    )
                    OutlinedTextField(
                        value = allergies,
                        onValueChange = { allergies = it },
                        label = { Text("Food Allergies (optional)") },
                        placeholder = { Text("peanuts, shellfish") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = sortMyPlateInputColors()
                    )
                    Button(
                        onClick = { onFallbackSignIn(email, fullName, allergies) },
                        enabled = isBootstrapped && !isAuthLoading && email.isNotBlank() && fullName.isNotBlank(),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SageSurface,
                            contentColor = ForestHeroDeep
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sign In with Email", fontWeight = FontWeight.Bold)
                    }
                }

                // Quiet Session Discovery Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dotColor = when {
                        isBootstrapped -> EmeraldPrimary
                        isBootstrapping -> AmberYellow
                        else -> RoseRed
                    }
                    Text(
                        text = when {
                            isBootstrapped -> "● $discoveredEndpointCount endpoints • $discoveredBaseUrl"
                            isBootstrapping -> "● Discovering $discoveredBaseUrl..."
                            else -> "● Discovery offline"
                        },
                        fontSize = 10.5.sp,
                        color = dotColor,
                        modifier = Modifier.weight(1f)
                    )
                    if (!isBootstrapping && !isBootstrapped) {
                        TextButton(onClick = onRetryDiscovery) {
                            Text("Retry", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleEmblemIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = size.width * 0.19f
        val pad = stroke / 2f
        val arcSize = Size(size.width - stroke, size.height - stroke)
        // Red top-left arc
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = -145f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(pad, pad),
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        // Yellow left arc
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 140f,
            sweepAngle = 75f,
            useCenter = false,
            topLeft = Offset(pad, pad),
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        // Green bottom arc
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 95f,
            useCenter = false,
            topLeft = Offset(pad, pad),
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        // Blue right arc + crossbar
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -10f,
            sweepAngle = 60f,
            useCenter = false,
            topLeft = Offset(pad, pad),
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(size.width * 0.52f, size.height * 0.5f),
            end = Offset(size.width * 0.92f, size.height * 0.5f),
            strokeWidth = stroke,
            cap = StrokeCap.Butt
        )
    }
}

@Composable
fun sortMyPlateInputColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = EmeraldPrimary,
    unfocusedBorderColor = CardBorder,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedLabelColor = ForestHeroMid,
    unfocusedLabelColor = TextSecondary,
    cursorColor = EmeraldPrimary,
    focusedPlaceholderColor = TextMuted,
    unfocusedPlaceholderColor = TextMuted,
    focusedContainerColor = CreamSurface,
    unfocusedContainerColor = CreamSurface
)
