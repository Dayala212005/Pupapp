package com.pdm0126.puppapp.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
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
    NavItem("Nueva", Icons.Outlined.AddCircle, Icons.Filled.AddCircle),
    NavItem("Menú", Icons.Outlined.Restaurant, Icons.Filled.Restaurant),
    NavItem("Historial", Icons.Outlined.History, Icons.Filled.History),
)

@Composable
fun PupappBottomNav(selectedIndex: Int, onItemSelected: (Int) -> Unit = {}) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
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
    ) {
        FilledTonalIconButton(
            onClick = onDecrease,
            modifier = Modifier.size(28.dp),
            enabled = quantity > 1
        ) {
            Icon(Icons.Filled.Remove, contentDescription = "Reducir", modifier = Modifier.size(16.dp))
        }
        Text(
            text = quantity.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        FilledTonalIconButton(
            onClick = onIncrease,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Aumentar", modifier = Modifier.size(16.dp))
        }
    }
}

// ─── Order card (used in active orders list) ─────────────────────────────────

data class OrderPreview(
    val id: Int,
    val reference: String?,
    val clientName: String?,
    val time: String,
    val itemCount: Int,
    val total: Double,
    val statusId: Int
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
                Text(
                    text = "#${order.id}${if (order.reference != null) " - ${order.reference}" else ""}",
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

// ─── Image picker field ─────────────────────────────────────────────────────

@Composable
fun ImagePickerField(
    selectedUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    modifier: Modifier = Modifier
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        onImageSelected(uri)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable {
                launcher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (selectedUri != null) {
                AsyncImage(
                    model = selectedUri,
                    contentDescription = "Selected image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Botón flotante para editar si ya hay imagen
                Box(Modifier
                    .fillMaxSize()
                    .padding(8.dp), contentAlignment = Alignment.BottomEnd) {
                    FilledTonalIconButton(
                        onClick = {
                            launcher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar", modifier = Modifier.size(16.dp))
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Subir imagen del producto",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
