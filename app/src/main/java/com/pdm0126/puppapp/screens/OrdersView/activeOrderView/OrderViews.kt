package com.pdm0126.puppapp.screens.OrdersView.activeOrderView

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdm0126.puppapp.components.OrderCard
import com.pdm0126.puppapp.components.OrderPreview
import com.pdm0126.puppapp.components.OrderStatus
import com.pdm0126.puppapp.components.OrderStatusBottomSheet
import com.pdm0126.puppapp.screens.OrdersView.newOrderView.NewOrderScreen
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveOrdersScreen(
    padding: PaddingValues = PaddingValues(),
    viewModel: ActiveOrdersViewModel = viewModel(factory = ActiveOrdersViewModel.Factory),
    onNewOrder: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedOrder by remember { mutableStateOf<OrderPreview?>(null) }
    var showNewOrderSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }

    val veryOldOrdersCount = remember(uiState.orders) {
        uiState.orders.count { order ->
            val orderTime = order.createdAt?.time ?: System.currentTimeMillis()
            val diffHours = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - orderTime)
            (order.statusId in 1..3) && diffHours >= 24
        }
    }

    LaunchedEffect(veryOldOrdersCount) {
        if (veryOldOrdersCount > 0) {
            snackbarHostState.showSnackbar(
                message = "Tienes $veryOldOrdersCount orden(es) con más de 24 horas sin entregar.",
                duration = SnackbarDuration.Long
            )
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refreshOrders() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Órdenes activas",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${uiState.orders.count { it.statusId != OrderStatus.DELIVERED.id }} activas",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                if (uiState.isLoading && uiState.orders.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (uiState.error != null && uiState.orders.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.error ?: "Error desconocido",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else if (uiState.orders.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay órdenes activas",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(uiState.orders) { order ->
                        OrderCard(
                            order = order,
                            onClick = { selectedOrder = order }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showNewOrderSheet = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add, 
                contentDescription = "Nueva orden",
                modifier = Modifier.size(28.dp)
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showNewOrderSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNewOrderSheet = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Box(Modifier.fillMaxHeight(0.97f)) { // Force 5/6 height (leaving 1/6 at top)
                NewOrderScreen(
                    onNavigateBack = {
                        showNewOrderSheet = false
                        viewModel.refreshOrders()
                    }
                )
            }
        }
    }

    selectedOrder?.let { order ->
        OrderStatusBottomSheet(
            order = order,
            onDismiss = { selectedOrder = null },
            onStatusChange = { newStatusId ->
                viewModel.updateOrderStatus(order.id, newStatusId)
                selectedOrder = null
            }
        )
    }
}

@Preview
@Composable
fun PreviewOrder() {
    ActiveOrdersScreen()
}
