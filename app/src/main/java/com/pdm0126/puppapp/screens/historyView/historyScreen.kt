package com.pdm0126.puppapp.screens.historyView

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdm0126.puppapp.components.OrderCard
import com.pdm0126.puppapp.components.SectionHeader
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    padding: PaddingValues = PaddingValues(),
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Todos, 1: Completadas
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
        // Tabs
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(selectedTab),
                    width = 80.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("General", color = MaterialTheme.colorScheme.onPrimary) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Resumen", color = MaterialTheme.colorScheme.onPrimary) }
            )
        }

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            if (selectedTab == 0) {
                AllOrdersList(
                    uiState = uiState,
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    onLoadMore = { viewModel.loadAllOrders() }
                )
            } else {
                DeliveredOrdersView(
                    uiState = uiState,
                    onPeriodSelected = { viewModel.setPeriod(it) }
                )
            }
        }
    }
}

@Composable
fun AllOrdersList(
    uiState: HistoryUiState,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()

    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 2
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !uiState.isLoadingAll && !uiState.hasReachedEnd) {
            onLoadMore()
        }
    }

    val filteredOrders = remember(uiState.allOrders, searchQuery) {
        uiState.allOrders.filter {
            it.clientName?.contains(searchQuery, ignoreCase = true) == true ||
                    it.reference?.contains(searchQuery, ignoreCase = true) == true ||
                    it.id.toString().contains(searchQuery) ||
                    it.orderNumber.toString().contains(searchQuery)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Buscar por orden o cliente...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        items(filteredOrders) { order ->
            OrderCard(order = order, onClick = {})
        }

        if (uiState.isLoadingAll) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@Composable
fun DeliveredOrdersView(
    uiState: HistoryUiState,
    onPeriodSelected: (HistoryPeriod) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(HistoryPeriod.entries) { period ->
                    FilterChip(
                        selected = uiState.selectedPeriod == period,
                        onClick = { onPeriodSelected(period) },
                        label = { Text(period.label) }
                    )
                }
            }
        }

        item {
            HistorySummaryRow(uiState.summary)
        }

        item {
            SectionHeader("Órdenes entregadas")
        }

        if (uiState.isLoadingDelivered) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else {
            items(uiState.deliveredOrders) { order ->
                OrderCard(order = order, onClick = {})
            }
        }
    }
}

@Composable
private fun HistorySummaryRow(summary: HistorySummary) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        MetricCard(
            label = "Ingresos",
            value = String.format(Locale.US, "$%.2f", summary.totalRevenue),
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            label = "Ordenes Completadas",
            value = summary.totalOrders.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp)
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Preview
@Composable
fun PreviewHistory() {
    HistoryScreen()
}
