package com.pdm0126.puppapp.screens.OrdersView.newOrderView

import com.pdm0126.puppapp.components.QuantityControl
import com.pdm0126.puppapp.components.SectionHeader
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MenuItemUi(val name: String, val price: Double, var quantity: Int = 0)

private val sampleMenuItems = mapOf(
    "Pupusas" to listOf(
        MenuItemUi("Pupusa de queso",   1.00, 2),
        MenuItemUi("Pupusa revuelta",   1.25, 1),
        MenuItemUi("Pupusa de frijol",  1.00),
        MenuItemUi("Pupusa de loroco",  1.25),
    ),
    "Bebidas" to listOf(
        MenuItemUi("Fresco de tamarindo", 0.75),
        MenuItemUi("Fresco de horchata",  0.75),
        MenuItemUi("Agua pura",           0.50),
    )
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewOrderScreen(onNavigateBack: () -> Unit = {}) {
    var clientName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva orden", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector        = Icons.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint               = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start  = 16.dp,
                end    = 16.dp,
                top    = innerPadding.calculateTopPadding() + 12.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Client name field
            item {
                OutlinedTextField(
                    value         = clientName,
                    onValueChange = { clientName = it },
                    label         = { Text("Nombre del cliente") },
                    placeholder   = { Text("Opcional") },
                    singleLine    = true,
                    shape         = RoundedCornerShape(10.dp),
                    modifier      = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))
                SectionHeader("Seleccionar productos")
                Spacer(Modifier.height(8.dp))
            }

            // Product categories
            sampleMenuItems.forEach { (category, items) ->
                item {
                    Text(
                        text       = category,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.primary,
                        modifier   = Modifier.padding(bottom = 6.dp)
                    )
                }
                items.forEach { product ->
                    item {
                        ProductRow(product)
                        Spacer(Modifier.height(6.dp))
                    }
                }
                item { Spacer(Modifier.height(10.dp)) }
            }

            // Order summary
            item {
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(12.dp))
                OrderSummaryCard()
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick  = {},
                    shape    = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Registrar orden", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun ProductRow(item: MenuItemUi) {
    Card(
        shape  = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(
                    text  = "\$%.2f".format(item.price),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            QuantityControl(quantity = item.quantity)
        }
    }
}

@Composable
private fun OrderSummaryCard() {
    Card(
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text       = "Resumen",
                fontWeight = FontWeight.SemiBold,
                fontSize   = 13.sp,
                modifier   = Modifier.padding(bottom = 8.dp)
            )
            SummaryRow("Pupusa de queso × 2", "$2.00")
            SummaryRow("Pupusa revuelta × 1",  "$1.25")
            Spacer(Modifier.height(6.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Total", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text       = "$3.25",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, amount: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(label,  fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(amount, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview
@Composable
fun PreviewNewOrder() {
    NewOrderScreen()
}