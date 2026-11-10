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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sortmyplate.model.AuthResponse
import io.sortmyplate.theme.*

@Composable
fun HeaderView(
    user: AuthResponse?,
    llmStatusLabel: String,
    onOpenGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CardWhite.copy(alpha = 0.92f),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Segmented Plate & Leaf App Mark
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(listOf(ForestHeroMid, ForestHeroDeep))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(26.dp)) {
                        val ringStroke = size.minDimension * 0.11f
                        val inset = ringStroke
                        val ringSize = Size(size.width - inset * 2f, size.height - inset * 2f)
                        val topLeft = Offset(inset, inset)

                        // Warm Cream Left Plate Arc
                        drawArc(
                            color = CreamSurface,
                            startAngle = 140f,
                            sweepAngle = 158f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = ringSize,
                            style = Stroke(width = ringStroke, cap = StrokeCap.Round)
                        )
                        // Emerald Bottom-Right Arc
                        drawArc(
                            color = EmeraldGlow,
                            startAngle = 24f,
                            sweepAngle = 92f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = ringSize,
                            style = Stroke(width = ringStroke, cap = StrokeCap.Round)
                        )
                        // Botanical Lime Accent Arc
                        drawArc(
                            color = Color(0xFFA6EB82),
                            startAngle = -26f,
                            sweepAngle = 26f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = ringSize,
                            style = Stroke(width = ringStroke, cap = StrokeCap.Round)
                        )
                        // Botanical Leaf Centerpiece
                        val leaf = Path().apply {
                            moveTo(size.width * 0.35f, size.height * 0.66f)
                            cubicTo(
                                size.width * 0.34f, size.height * 0.44f,
                                size.width * 0.47f, size.height * 0.32f,
                                size.width * 0.66f, size.height * 0.34f
                            )
                            cubicTo(
                                size.width * 0.67f, size.height * 0.54f,
                                size.width * 0.53f, size.height * 0.67f,
                                size.width * 0.35f, size.height * 0.66f
                            )
                        }
                        drawPath(leaf, color = Color(0xFFA6F2C2))
                        drawLine(
                            color = ForestHeroDeep,
                            start = Offset(size.width * 0.33f, size.height * 0.68f),
                            end = Offset(size.width * 0.58f, size.height * 0.42f),
                            strokeWidth = size.minDimension * 0.06f,
                            cap = StrokeCap.Round
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "SortMyPlate",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Biomarker Wellness",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            // Right: Calm utility pills (LLM dot, ? Guide, User Avatar)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Live Engine Pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = SageSurface,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary)
                        )
                        Text(
                            text = llmStatusLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForestHeroMid
                        )
                    }
                }

                // ? Guide trigger
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CardWhite,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
                    ),
                    modifier = Modifier.clickable(onClick = onOpenGuide)
                ) {
                    Text(
                        text = "? Guide",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                if (user != null) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ForestHeroDeep)
                            .border(1.5.dp, EmeraldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.fullName.take(1).uppercase(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
