package com.pdm0126.puppapp.screens.OrdersView.newOrderView

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.pdm0126.puppapp.components.QuantityControl
import com.pdm0126.puppapp.components.SectionHeader
import com.pdm0126.puppapp.data.model.Product

@Composable
fun NewOrderScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: NewOrderViewModel = viewModel()
) {
    val customerName    by viewModel.customerName.collectAsState()
    val orderReference  by viewModel.orderReference.collectAsState()
    val customTotal     by viewModel.customTotal.collectAsState()
    val groupedProducts by viewModel.groupedProducts.collectAsState()
    val quantities      by viewModel.quantities.collectAsState()
    val selectedItems   by viewModel.selectedItems.collectAsState()
    val total           by viewModel.total.collectAsState()
    val isLoading       by viewModel.isLoading.collectAsState()
    val errorMessage    by viewModel.errorMessage.collectAsState()
    val orderSuccess    by viewModel.orderSuccess.collectAsState()

    var currentStep by remember { mutableIntStateOf(1) }
    val focusManager = LocalFocusManager.current

    // Navegar de vuelta cuando la orden se crea exitosamente
    LaunchedEffect(orderSuccess) {
        if (orderSuccess) {
            viewModel.resetOrderSuccess()
            onNavigateBack()
        }
    }

    Scaffold(
        bottomBar = {
            if (selectedItems.isNotEmpty()) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .navigationBarsPadding()
                    ) {
                        if (currentStep == 1) {
                            Button(
                                onClick = { currentStep = 2 },
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Text("Siguiente", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = { currentStep = 1 },
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                ) {
                                    Text("Atrás", fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { viewModel.onCreateOrderClick() },
                                    enabled = !isLoading,
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier
                                        .weight(2f)
                                        .height(56.dp)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else {
                                        Text("Registrar orden", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusManager.clearFocus() }
        ) {
            // Icono de fondo decorativo
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                modifier = Modifier
                    .size(400.dp)
                    .offset(y = 100.dp)
            )

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
                currentStep == 1 -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Campos iniciales
                        item(span = { GridItemSpan(2) }) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value         = customerName,
                                    onValueChange = { viewModel.onCustomerNameChange(it) },
                                    label         = { Text("Cliente", fontSize = 12.sp) },
                                    placeholder   = { Text("Opcional", fontSize = 12.sp) },
                                    singleLine    = true,
                                    shape         = RoundedCornerShape(16.dp),
                                    modifier      = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value         = orderReference,
                                    onValueChange = { viewModel.onOrderReferenceChange(it) },
                                    label         = { Text("Ref.", fontSize = 12.sp) },
                                    singleLine    = true,
                                    shape         = RoundedCornerShape(16.dp),
                                    modifier      = Modifier.weight(1f)
                                )
                            }
                        }

                        item(span = { GridItemSpan(2) }) {
                            SectionHeader("Seleccionar productos")
                        }

                        // Productos agrupados por categoría
                        groupedProducts.forEach { (category, items) ->
                            item(span = { GridItemSpan(2) }) {
                                Text(
                                    text       = category,
                                    fontSize   = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.primary,
                                    modifier   = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }
                            items(items) { product ->
                                ProductCard(
                                    product  = product,
                                    quantity = quantities[product.id] ?: 0,
                                    onIncrease = { viewModel.onIncrease(product.id) },
                                    onDecrease = { viewModel.onDecrease(product.id) }
                                )
                            }
                        }
                    }
                }
                currentStep == 2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        SectionHeader("Resumen de la orden")
                        Spacer(Modifier.height(16.dp))

                        OrderSummaryCard(
                            selectedItems = selectedItems,
                            total         = total,
                            customTotal   = customTotal,
                            onCustomTotalChange = { viewModel.onCustomTotalChange(it) }
                        )

                        errorMessage?.let {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text     = it,
                                color    = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ProductCard(
    product: Product,
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Card(
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border   = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Imagen del producto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = painterResource(id = android.R.drawable.ic_menu_gallery),
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
                )
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    product.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text     = "$%.2f".format(product.priceBase),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color    = MaterialTheme.colorScheme.primary
                )
                
                Spacer(Modifier.height(8.dp))

                if (quantity == 0) {
                    Button(
                        onClick        = onIncrease,
                        shape          = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier       = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Añadir", fontSize = 12.sp)
                    }
                } else {
                    QuantityControl(
                        quantity   = quantity,
                        onIncrease = onIncrease,
                        onDecrease = onDecrease,
                        modifier   = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(
    selectedItems: List<Pair<Product, Int>>,
    total: Double,
    customTotal: String,
    onCustomTotalChange: (String) -> Unit
) {
    Card(
        shape    = RoundedCornerShape(24.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
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
            Spacer(Modifier.height(10.dp))

            // Fila de Total Original (solo si hay un total personalizado)
            if (customTotal.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    Text("Subtotal calculado", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("\$%.2f".format(total), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(8.dp))
            }

            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth()
            ) {
                Text("Total Final", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                
                OutlinedTextField(
                    value         = customTotal,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null || it.last() == '.') onCustomTotalChange(it) },
                    placeholder   = { Text("\$%.2f".format(total), fontSize = 14.sp) },
                    singleLine    = true,
                    textStyle     = LocalTextStyle.current.copy(
                        textAlign  = androidx.compose.ui.text.style.TextAlign.End,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.primary,
                        fontSize   = 14.sp
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.width(100.dp).height(48.dp)
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
