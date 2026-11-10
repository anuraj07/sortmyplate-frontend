package io.sortmyplate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import io.sortmyplate.model.GroupMember
import io.sortmyplate.model.GroupResponse
import io.sortmyplate.theme.*

@Composable
fun HouseholdView(
    group: GroupResponse?,
    activeMemberIds: Set<String>,
    onToggleMember: (String) -> Unit,
    currentUserId: String?,
    modifier: Modifier = Modifier
) {
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
                    Text(text = "👥 ", fontSize = 16.sp)
                    Text(
                        text = group?.name ?: "Household / Flat",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                if (group != null) {
                    Text(
                        text = "Code: ${group.inviteCode}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Check who is eating now. Meal re-adapts automatically:",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (group == null || group.members.isEmpty()) {
                Text(
                    text = "No flatmates in circle yet.",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    group.members.forEach { member ->
                        val isChecked = activeMemberIds.contains(member.id)
                        val isYou = member.id == currentUserId

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isChecked) Color(0xFF1E293B) else Color(0xFF0F172A),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(if (isChecked) CyanAccent.copy(alpha = 0.5f) else CardBorder)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleMember(member.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(CyanAccent.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = member.fullName.take(1),
                                            fontWeight = FontWeight.Bold,
                                            color = CyanAccent,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = member.fullName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary
                                            )
                                            if (isYou) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "(You)",
                                                    fontSize = 11.sp,
                                                    color = CyanAccent
                                                )
                                            }
                                        }
                                        if (member.allergies.isNotEmpty()) {
                                            Text(
                                                text = "Allergy: ${member.allergies.joinToString(", ")}",
                                                fontSize = 11.sp,
                                                color = RoseRed
                                            )
                                        } else {
                                            Text(
                                                text = member.email,
                                                fontSize = 11.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }

                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { onToggleMember(member.id) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = CyanAccent,
                                        checkmarkColor = Color(0xFF0F172A)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
