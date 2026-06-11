package com.pdm0126.puppapp.screens.menuView

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdm0126.puppapp.components.PupappBottomNav
import com.pdm0126.puppapp.ui.Green40
import com.pdm0126.puppapp.ui.Purple60


// ── Data ────────────────────────────────────────────────────────────────────

data class MenuItem(val name: String, val price: Double, val isPredefined: Boolean)

private val menuData = mapOf(
    "Pupusas" to listOf(
        MenuItem("Pupusa de queso",   1.00, isPredefined = true),
        MenuItem("Pupusa revuelta",   1.25, isPredefined = false),
        MenuItem("Pupusa de frijol",  1.00, isPredefined = true),
        MenuItem("Pupusa de loroco",  1.25, isPredefined = false),
    ),
    "Bebidas" to listOf(
        MenuItem("Fresco de tamarindo", 0.75, isPredefined = false),
        MenuItem("Fresco de horchata",  0.75, isPredefined = false),
        MenuItem("Agua pura",           0.50, isPredefined = true),
    )
)

// ── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(onSelectTab: (Int) -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de menú", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            PupappBottomNav(selectedIndex = 2, onItemSelected = onSelectTab)
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start  = 16.dp,
                end    = 16.dp,
                top    = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            menuData.forEach { (category, items) ->
                item {
                    CategoryHeader(category)
                    Spacer(Modifier.height(8.dp))
                }
                items.forEach { item ->
                    item {
                        MenuItemRow(item)
                        Spacer(Modifier.height(6.dp))
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

// ── Category header ──────────────────────────────────────────────────────────

@Composable
private fun CategoryHeader(title: String) {
    Row(
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        TextButton(
            onClick  = {},
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Agregar", fontSize = 12.sp)
        }
    }
}

// ── Menu item row ────────────────────────────────────────────────────────────

@Composable
private fun MenuItemRow(item: MenuItem) {
    val dotColor = if (item.isPredefined) Purple60 else Green40

    Card(
        shape  = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment   = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Colored dot indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(
                    text     = if (item.isPredefined) "Predefinido" else "Personalizado",
                    fontSize = 11.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text       = "\$%.2f".format(item.price),
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.primary,
                modifier   = Modifier.padding(end = 8.dp)
            )

            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector        = Icons.Outlined.Edit,
                    contentDescription = "Editar",
                    modifier           = Modifier.size(18.dp),
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector        = Icons.Outlined.Delete,
                    contentDescription = "Eliminar",
                    modifier           = Modifier.size(18.dp),
                    tint               = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewMenu() {
    MenuScreen()
}