package com.pdm0126.puppapp.screens.OrdersView.activeOrderView

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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


private val sampleOrders = listOf(
    OrderPreview("001", "María López",   "10:32 AM", 3, 4.50, OrderStatus.PREPARING),
    OrderPreview("002", "Carlos Pérez",  "10:41 AM", 2, 3.00, OrderStatus.PREPARING),
    OrderPreview("003", "Ana Morales",   "10:55 AM", 5, 7.00, OrderStatus.COMPLETED),
    OrderPreview("004", "Juan Martínez", "11:03 AM", 1, 1.25, OrderStatus.CANCELLED),
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveOrdersScreen(
    onNewOrder:    () -> Unit = {},
    onSelectTab:   (Int) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Pupapp", fontWeight = FontWeight.SemiBold)
                        Text(
                            text  = "Pupusería El Comal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector        = Icons.Outlined.Notifications,
                            contentDescription = "Notificaciones",
                            tint               = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick            = onNewOrder,
                containerColor     = MaterialTheme.colorScheme.primary,
                contentColor       = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva orden")
            }
        },
        bottomBar = {
            PupappBottomNav(selectedIndex = 0, onItemSelected = onSelectTab)
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start  = 16.dp,
                end    = 16.dp,
                top    = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    verticalAlignment   = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text       = "Órdenes activas",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text     = "${sampleOrders.count { it.status == OrderStatus.PREPARING }} activas",
                            fontSize = 11.sp,
                            color    = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            items(sampleOrders) { order ->
                OrderCard(order)
            }
        }
    }
}

@Preview
@Composable
fun PreviewOrder() {
    ActiveOrdersScreen()
}
