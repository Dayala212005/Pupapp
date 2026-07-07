package com.pdm0126.puppapp.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import java.util.Date
import java.util.concurrent.TimeUnit
import com.pdm0126.puppapp.data.model.OrderItem
import com.pdm0126.puppapp.ui.Amber40
import com.pdm0126.puppapp.ui.Green40
import com.pdm0126.puppapp.ui.Red40

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
    val showId: Boolean = false,
    val createdAt: Date? = null
)

@Composable
fun OrderCard(order: OrderPreview, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    val currentTime = System.currentTimeMillis()
    val orderTime = order.createdAt?.time ?: currentTime
    val diffMillis = currentTime - orderTime
    val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
    val diffHours = TimeUnit.MILLISECONDS.toHours(diffMillis)

    val isVeryOld = (order.statusId in 1..3) && diffHours >= 24

    // Logica para el color de la barra indicadora lateral
    val indicatorColor = when {
        order.statusId in 1..2 && diffMinutes >= 30 -> Color(0xFFE53935) // Rojo (Tarde)
        order.statusId in 1..2 && diffMinutes >= 15 -> Color(0xFFFFB300) // Ámbar (Demora)
        order.statusId == 1 -> Color(0xFFFB8C00) // Naranja (Pendiente - Estado normal)
        order.statusId == 2 -> Color(0xFF1E88E5) // Azul (Preparando)
        order.statusId == 3 -> Color(0xFF43A047) // Verde (Listo)
        else -> Color.Gray // Otros (Entregado, Cancelado)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        // Estructura principal para la barra lateral
        Row(modifier = Modifier.fillMaxWidth()) {

            // Barra Vertical Indicadora
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .defaultMinSize(minHeight = 100.dp)
                    .background(indicatorColor)
            )

            // Contenido de la Tarjeta
            Column(
                Modifier
                    .padding(12.dp)
                    .weight(1f)
            ) {
                // Encabezado
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

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$headerText${if (order.reference != null) " - ${order.reference}" else ""}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // Alerta crítica de +24h
                        if (isVeryOld) {
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Orden muy antigua",
                                tint = Color(0xFFE53935), // Rojo
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    StatusChip(order.statusId)
                }

                Spacer(Modifier.height(4.dp))

                // Info Secundaria (Cliente y Reloj)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${order.clientName ?: "Consumidor Final"} · ${order.time}",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )

                    // Indicador de tiempo transcurrido (solo si está demorada)
                    if (order.statusId in 1..2 && diffMinutes >= 15) {
                        Text(
                            text = "Hace $diffMinutes min",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = indicatorColor
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
                Spacer(Modifier.height(8.dp))

                // Lista de Productos
                if (order.items.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        order.items.forEach { item ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text(
                                    text = "${item.quantity}x",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.width(28.dp) // Espacio fijo para alinear las cantidades
                                )
                                Text(
                                    text = item.productName,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
                Spacer(Modifier.height(8.dp))

                // Footer
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${order.itemCount} productos",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Text(
                        text = "\$%.2f".format(order.total),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
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
