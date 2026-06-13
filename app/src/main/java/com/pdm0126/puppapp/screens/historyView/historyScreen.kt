package com.pdm0126.puppapp.screens.historyView

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdm0126.puppapp.components.OrderCard
import com.pdm0126.puppapp.components.OrderPreview
import com.pdm0126.puppapp.components.OrderStatus
import com.pdm0126.puppapp.components.PupappBottomNav
import com.pdm0126.puppapp.components.SectionHeader


// ── Data ────────────────────────────────────────────────────────────────────



private val filterOptions = listOf("Todos", "Completadas", "Canceladas")

// ── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onSelectTab: (Int) -> Unit = {}) {
    var searchQuery     by remember { mutableStateOf("") }
    var selectedFilter  by remember { mutableStateOf("Todos") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            PupappBottomNav(selectedIndex = 3, onItemSelected = onSelectTab)
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start  = 16.dp,
                end    = 16.dp,
                top    = innerPadding.calculateTopPadding() + 12.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Search field
            item {
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder   = { Text("Buscar por orden o cliente...") },
                    leadingIcon   = {
                        Icon(Icons.Filled.Search, contentDescription = null)
                    },
                    singleLine    = true,
                    shape         = RoundedCornerShape(10.dp),
                    modifier      = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
            }

            // Filter chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterOptions) { option ->
                        FilterChip(
                            selected = selectedFilter == option,
                            onClick  = { selectedFilter = option },
                            label    = { Text(option, fontSize = 12.sp) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Summary metrics
            item {
                HistorySummaryRow()
                Spacer(Modifier.height(16.dp))
                SectionHeader("Órdenes completadas y canceladas")
                Spacer(Modifier.height(8.dp))
            }

            // Order list

        }
    }
}

// ── Summary row ──────────────────────────────────────────────────────────────

@Composable
private fun HistorySummaryRow() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        MetricCard(
            label = "Ingresos totales",
            value = "$27.00",
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            label = "Total órdenes",
            value = "10",
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            label = "Canceladas",
            value = "2",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape    = RoundedCornerShape(10.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp)
        ) {
            Text(
                text       = value,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text     = label,
                fontSize = 10.sp,
                color    = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Preview
@Composable
fun PreviewHistory() {
    HistoryScreen()
}