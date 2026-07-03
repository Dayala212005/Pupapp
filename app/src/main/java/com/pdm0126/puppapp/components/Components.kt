package com.pdm0126.puppapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdm0126.puppapp.data.model.OrderItem
import com.pdm0126.puppapp.ui.Amber40
import com.pdm0126.puppapp.ui.Green40
import com.pdm0126.puppapp.ui.Red40

// ─── Status chip ────────────────────────────────────────────────────────────

// IDs según API: 1: Pendiente, 2: Preparando, 3: Listo, 4: Entregado, 5: Cancelado
enum class OrderStatus(val id: Int, val label: String) {
    PENDING(1, "Pendiente"),
    PREPARING(2, "Preparando"),
    READY(3, "Listo"),
    DELIVERED(4, "Entregado"),
    CANCELLED(5, "Cancelada");

    companion object {
        fun fromId(id: Int) = entries.find { it.id == id } ?: PENDING
    }
}

@Composable
fun StatusChip(statusId: Int) {
    val status = OrderStatus.fromId(statusId)
    val color = when (status) {
        OrderStatus.PENDING -> Color.Gray
        OrderStatus.PREPARING -> Amber40
        OrderStatus.READY -> MaterialTheme.colorScheme.primary
        OrderStatus.DELIVERED -> Green40
        OrderStatus.CANCELLED -> Red40
    }
    
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.12f),
    ) {
        Text(
            text = status.label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

// ─── Avatar circle ───────────────────────────────────────────────────────────

@Composable
fun AvatarCircle(initials: String, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Text(
            text = initials,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─── Bottom nav ──────────────────────────────────────────────────────────────

data class NavItem(val label: String, val icon: ImageVector, val selectedIcon: ImageVector)

val bottomNavItems = listOf(
    NavItem("Órdenes", Icons.Outlined.List, Icons.Filled.List),
    NavItem("Menú", Icons.Outlined.Restaurant, Icons.Filled.Restaurant),
    NavItem("Historial", Icons.Outlined.History, Icons.Filled.History),
)

@Composable
fun PupappBottomNav(selectedIndex: Int, onItemSelected: (Int) -> Unit = {}) {
    NavigationBar(
        containerColor = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onItemSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (selectedIndex == index) item.selectedIcon else item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label, fontSize = 11.sp) }
            )
        }
    }
}

// ─── Section header ──────────────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.8.sp,
        modifier = modifier.padding(bottom = 6.dp)
    )
}

// ─── Quantity control ────────────────────────────────────────────────────────

@Composable
fun QuantityControl(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
            .padding(4.dp)
    ) {
        FilledTonalIconButton(
            onClick  = onDecrease,
            shape    = CircleShape,
            modifier = Modifier.size(32.dp),
            enabled  = quantity > 0 // Permitir llegar a 0 para quitar? 
                                    // Según NewOrderView, si es 0 muestra botón "Agregar".
                                    // Pero el QuantityControl se muestra cuando qty > 0.
        ) {
            Icon(Icons.Filled.Remove, contentDescription = "Reducir", modifier = Modifier.size(16.dp))
        }
        Text(
            text       = quantity.toString(),
            fontSize   = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.padding(horizontal = 12.dp)
        )
        FilledIconButton(
            onClick  = onIncrease,
            shape    = CircleShape,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Aumentar", modifier = Modifier.size(16.dp))
        }
    }
}

// ─── Order card (used in active orders list) ─────────────────────────────────

data class OrderPreview(
    val id: Int,
    val orderNumber: Int,
    val reference: String?,
    val clientName: String?,
    val time: String,
    val itemCount: Int,
    val total: Double,
    val statusId: Int,
    val items: List<OrderItem> = emptyList(),
    val showId: Boolean = false
)

@Composable
fun OrderCard(order: OrderPreview, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                val formattedNumber = order.orderNumber.toString().padStart(4, '0')
                val headerText = if (order.showId) {
                    "#$formattedNumber (ID: ${order.id})"
                } else {
                    "#$formattedNumber"
                }
                Text(
                    text = "$headerText${if (order.reference != null) " - ${order.reference}" else ""}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatusChip(order.statusId)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${order.clientName ?: "Consumidor Final"} · ${order.time}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (order.items.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                order.items.forEach { item ->
                    Text(
                        text = "• ${item.quantity}x ${item.productName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("${order.itemCount} productos", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("\$%.2f".format(order.total), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderStatusBottomSheet(
    order: OrderPreview,
    onDismiss: () -> Unit,
    onStatusChange: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            val formattedNumber = order.orderNumber.toString().padStart(4, '0')
            val headerText = if (order.showId) {
                "Orden #$formattedNumber (ID: ${order.id})"
            } else {
                "Orden #$formattedNumber"
            }
            Text(
                text       = headerText,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 16.sp
            )
            Text(
                text     = order.clientName ?: "Consumidor Final",
                fontSize = 13.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))
            Text(
                text       = "Cambiar estado",
                fontWeight = FontWeight.Medium,
                fontSize   = 14.sp
            )
            Spacer(Modifier.height(12.dp))

            // Botones de estado
            listOf(
                1 to "Pendiente",
                2 to "Preparando",
                3 to "Listo",
                4 to "Entregado",
                5 to "Cancelado"
            ).forEach { (statusId, label) ->
                val isCurrentStatus = order.statusId == statusId
                OutlinedButton(
                    onClick  = { if (!isCurrentStatus) onStatusChange(statusId) },
                    enabled  = !isCurrentStatus,
                    shape    = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(label)
                }
            }
        }
    }
}
