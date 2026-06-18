package com.pdm0126.puppapp.screens.OrdersView.newOrderView

import com.pdm0126.puppapp.components.QuantityControl
import com.pdm0126.puppapp.components.SectionHeader
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdm0126.puppapp.data.model.Product

@Composable
fun NewOrderScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: NewOrderViewModel = viewModel()
) {
    val customerName    by viewModel.customerName.collectAsState()
    val groupedProducts by viewModel.groupedProducts.collectAsState()
    val quantities      by viewModel.quantities.collectAsState()
    val selectedItems   by viewModel.selectedItems.collectAsState()
    val total           by viewModel.total.collectAsState()
    val isLoading       by viewModel.isLoading.collectAsState()
    val errorMessage    by viewModel.errorMessage.collectAsState()
    val orderSuccess    by viewModel.orderSuccess.collectAsState()

    val focusManager = LocalFocusManager.current

    // Navegar de vuelta cuando la orden se crea exitosamente
    LaunchedEffect(orderSuccess) {
        if (orderSuccess) {
            viewModel.resetOrderSuccess()
            onNavigateBack()
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(bottom = 32.dp)
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { focusManager.clearFocus() }
    ) {
        when {
            isLoading && groupedProducts.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            errorMessage != null && groupedProducts.isEmpty() -> {
                Column(
                    modifier            = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadProducts() }) {
                        Text("Reintentar")
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Campo nombre cliente
                    item {
                        OutlinedTextField(
                            value         = customerName,
                            onValueChange = { viewModel.onCustomerNameChange(it) },
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

                    // Productos agrupados por categoría
                    groupedProducts.forEach { (category, items) ->
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
                                ProductRow(
                                    product  = product,
                                    quantity = quantities[product.id] ?: 0,
                                    onIncrease = { viewModel.onIncrease(product.id) },
                                    onDecrease = { viewModel.onDecrease(product.id) }
                                )
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                        item { Spacer(Modifier.height(10.dp)) }
                    }

                    // Resumen y botón solo si hay items seleccionados
                    if (selectedItems.isNotEmpty()) {
                        item {
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color     = MaterialTheme.colorScheme.outlineVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            OrderSummaryCard(
                                selectedItems = selectedItems,
                                total         = total
                            )
                            Spacer(Modifier.height(16.dp))

                            // Error message
                            errorMessage?.let {
                                Text(
                                    text     = it,
                                    color    = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            Button(
                                onClick  = { viewModel.onCreateOrderClick() },
                                enabled  = !isLoading,
                                shape    = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                else Text("Registrar orden", fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductRow(
    product: Product,
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Card(
        shape    = RoundedCornerShape(10.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border   = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier              = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(
                    text     = "\$%.2f".format(product.priceBase),
                    fontSize = 12.sp,
                    color    = MaterialTheme.colorScheme.primary
                )
            }
            if (quantity == 0) {
                FilledTonalButton(
                    onClick        = onIncrease,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier       = Modifier.height(32.dp)
                ) {
                    Text("Agregar", fontSize = 12.sp)
                }
            } else {
                QuantityControl(
                    quantity   = quantity,
                    onIncrease = onIncrease,
                    onDecrease = onDecrease
                )
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(
    selectedItems: List<Pair<Product, Int>>,
    total: Double
) {
    Card(
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text       = "Resumen",
                fontWeight = FontWeight.SemiBold,
                fontSize   = 13.sp,
                modifier   = Modifier.padding(bottom = 8.dp)
            )
            selectedItems.forEach { (product, qty) ->
                SummaryRow(
                    label  = "${product.name} × $qty",
                    amount = "\$%.2f".format(product.priceBase * qty)
                )
            }
            Spacer(Modifier.height(6.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth()
            ) {
                Text("Total", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text       = "\$%.2f".format(total),
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
        modifier              = Modifier
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
