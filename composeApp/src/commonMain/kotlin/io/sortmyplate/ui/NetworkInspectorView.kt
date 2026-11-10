package io.sortmyplate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.sortmyplate.network.NetworkLogStatus
import io.sortmyplate.network.NetworkTransaction
import io.sortmyplate.theme.*

private enum class InspectorFilter {
    ALL,
    SUCCESS,
    ERRORS
}

@Composable
fun NetworkInspectorDialog(
    transactions: List<NetworkTransaction>,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(InspectorFilter.ALL) }
    var expandedIds by remember { mutableStateOf(setOf<Long>()) }

    val filteredTransactions = remember(transactions, selectedFilter) {
        when (selectedFilter) {
            InspectorFilter.ALL -> transactions
            InspectorFilter.SUCCESS -> transactions.filter { it.status == NetworkLogStatus.SUCCESS }
            InspectorFilter.ERRORS -> transactions.filter {
                it.status == NetworkLogStatus.CLIENT_ERROR ||
                    it.status == NetworkLogStatus.SERVER_ERROR ||
                    it.status == NetworkLogStatus.FAILED
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .widthIn(max = 920.dp),
            shape = RoundedCornerShape(20.dp),
            color = BgDark,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(CyanAccent.copy(alpha = 0.45f))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📡 KMP Network Inspector",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Live Ktor Traffic • OkHttp (Android Studio) / Darwin (iOS) / Fetch (Wasm)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(
                            onClick = onClear,
                            colors = ButtonDefaults.textButtonColors(contentColor = AmberYellow)
                        ) {
                            Text("Clear", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CardBorder,
                                contentColor = TextPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Close", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Tabs + Counts
                val okCount = transactions.count { it.status == NetworkLogStatus.SUCCESS }
                val errCount = transactions.count {
                    it.status == NetworkLogStatus.CLIENT_ERROR ||
                        it.status == NetworkLogStatus.SERVER_ERROR ||
                        it.status == NetworkLogStatus.FAILED
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InspectorFilterChip(
                        label = "All (${transactions.size})",
                        selected = selectedFilter == InspectorFilter.ALL,
                        onClick = { selectedFilter = InspectorFilter.ALL }
                    )
                    InspectorFilterChip(
                        label = "2xx OK ($okCount)",
                        selected = selectedFilter == InspectorFilter.SUCCESS,
                        onClick = { selectedFilter = InspectorFilter.SUCCESS }
                    )
                    InspectorFilterChip(
                        label = "Errors ($errCount)",
                        selected = selectedFilter == InspectorFilter.ERRORS,
                        onClick = { selectedFilter = InspectorFilter.ERRORS }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = CardBorder)
                Spacer(modifier = Modifier.height(10.dp))

                if (filteredTransactions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matching HTTP calls recorded in this session yet.",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredTransactions, key = { it.id }) { tx ->
                            val isExpanded = expandedIds.contains(tx.id)
                            NetworkTransactionCard(
                                tx = tx,
                                isExpanded = isExpanded,
                                onToggle = {
                                    expandedIds = if (isExpanded) {
                                        expandedIds - tx.id
                                    } else {
                                        expandedIds + tx.id
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InspectorFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CyanAccent,
            selectedLabelColor = Color(0xFF0F172A),
            containerColor = CardDark,
            labelColor = TextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) CyanAccent else CardBorder
        )
    )
}

@Composable
private fun NetworkTransactionCard(
    tx: NetworkTransaction,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val statusColor = when (tx.status) {
        NetworkLogStatus.PENDING -> CyanAccent
        NetworkLogStatus.SUCCESS -> EmeraldGreen
        NetworkLogStatus.CLIENT_ERROR -> AmberYellow
        NetworkLogStatus.SERVER_ERROR, NetworkLogStatus.FAILED -> RoseRed
    }
    val statusLabel = when {
        tx.status == NetworkLogStatus.PENDING -> "..."
        tx.status == NetworkLogStatus.FAILED -> "FAIL"
        tx.statusCode != null -> "${tx.statusCode}"
        else -> "..."
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isExpanded) statusColor.copy(alpha = 0.6f) else CardBorder
            )
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PurpleAccent.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = tx.method,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PurpleAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        text = tx.path,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = tx.durationMs?.let { "$it ms" } ?: "pending",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = if (isExpanded) "▲" else "▼",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = CardBorder)
                Spacer(modifier = Modifier.height(10.dp))

                SelectionContainer {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InspectorSection(
                            title = "URL",
                            content = "${tx.method} ${tx.url}" + (tx.statusDescription?.let { " ($statusLabel $it)" } ?: "")
                        )

                        if (tx.errorMessage != null) {
                            InspectorSection(
                                title = "Network Error",
                                content = tx.errorMessage,
                                accentColor = RoseRed
                            )
                        }

                        if (tx.requestHeaders.isNotEmpty()) {
                            InspectorSection(
                                title = "Request Headers",
                                content = tx.requestHeaders.entries.joinToString("\n") { "${it.key}: ${it.value}" }
                            )
                        }

                        if (!tx.requestBody.isNullOrBlank()) {
                            InspectorSection(
                                title = "Request Body",
                                content = tx.requestBody
                            )
                        }

                        if (tx.responseHeaders.isNotEmpty()) {
                            InspectorSection(
                                title = "Response Headers",
                                content = tx.responseHeaders.entries.joinToString("\n") { "${it.key}: ${it.value}" }
                            )
                        }

                        if (!tx.responseBody.isNullOrBlank()) {
                            InspectorSection(
                                title = "Response Body",
                                content = tx.responseBody
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InspectorSection(
    title: String,
    content: String,
    accentColor: Color = CyanAccent
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A0F1D), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Text(
                text = content,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                fontFamily = FontFamily.Monospace,
                color = if (accentColor == RoseRed) RoseRed else TextSecondary
            )
        }
    }
}
