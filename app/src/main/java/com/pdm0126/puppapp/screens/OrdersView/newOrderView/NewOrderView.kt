package com.pdm0126.puppapp.screens.OrdersView.newOrderView

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.pdm0126.puppapp.data.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewOrderScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: NewOrderViewModel = viewModel(factory = NewOrderViewModel.Factory)
) {
    val customerName    by viewModel.customerName.collectAsState()
    val orderReference  by viewModel.orderReference.collectAsState()
    val customTotal     by viewModel.customTotal.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val categories      by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery     by viewModel.searchQuery.collectAsState()
    val quantities      by viewModel.quantities.collectAsState()
    val selectedItems   by viewModel.selectedItems.collectAsState()
    val total           by viewModel.total.collectAsState()
    val isLoading       by viewModel.isLoading.collectAsState()
    val errorMessage    by viewModel.errorMessage.collectAsState()
    val orderSuccess    by viewModel.orderSuccess.collectAsState()

    var showSummary by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Navegar de vuelta cuando la orden se crea exitosamente
    LaunchedEffect(orderSuccess) {
        if (orderSuccess) {
            viewModel.resetOrderSuccess()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                // Barra de búsqueda con fondo neutro
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        placeholder = { Text("Buscar producto...", fontSize = 14.sp, color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp), tint = Color.Gray) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF3F4F6),
                            unfocusedContainerColor = Color(0xFFF3F4F6),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.weight(1f).height(52.dp)
                    )
                }

                // Selector de Categoría
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        CategoryTab(
                            text = "Todos",
                            isSelected = selectedCategory == null,
                            onClick = { viewModel.onCategorySelect(null) }
                        )
                    }
                    items(categories) { category ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            VerticalDivider(
                                modifier = Modifier.height(24.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                            CategoryTab(
                                text = category,
                                isSelected = selectedCategory == category,
                                onClick = { viewModel.onCategorySelect(category) }
                            )
                        }
                    }
                }
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
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
            if (filteredProducts.isEmpty() && !isLoading) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Inbox, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                    Text("No hay productos", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts) { product ->
                        MinimalProductCard(
                            product = product,
                            quantity = quantities[product.id] ?: 0,
                            onIncrease = { viewModel.onIncrease(product.id) },
                            onDecrease = { viewModel.onDecrease(product.id) }
                        )
                    }
                }
            }

            // Barra Flotante
            AnimatedVisibility(
                visible = selectedItems.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    onClick = { showSummary = true },
                    modifier = Modifier
                        .padding(16.dp)
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = selectedItems.sumOf { it.second }.toString(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Ver Resumen",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "$%.2f".format(total),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            if (isLoading && filteredProducts.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    if (showSummary) {
        Dialog(
            onDismissRequest = { showSummary = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            OrderSummaryDialog(
                customerName = customerName,
                orderReference = orderReference,
                selectedItems = selectedItems,
                total = total,
                customTotal = customTotal,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onCustomerNameChange = { viewModel.onCustomerNameChange(it) },
                onOrderReferenceChange = { viewModel.onOrderReferenceChange(it) },
                onCustomTotalChange = { viewModel.onCustomTotalChange(it) },
                onConfirm = { viewModel.onCreateOrderClick() },
                onDismiss = { showSummary = false }
            )
        }
    }
}

@Composable
private fun CategoryTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent,
        contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun MinimalProductCard(
    product: Product,
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(90.dp)) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                    error = painterResource(id = android.R.drawable.ic_menu_gallery),
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
                )
                if (quantity > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.padding(6.dp).size(24.dp).align(Alignment.TopEnd)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(quantity.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    product.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$%.2f".format(product.priceBase),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Box(
                        contentAlignment = Alignment.CenterEnd,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (quantity > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
                        ) {
                            if (quantity > 0) {
                                IconButton(
                                    onClick = onDecrease,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                                Text(
                                    text = quantity.toString(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                            IconButton(
                                onClick = onIncrease,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderSummaryDialog(
    customerName: String,
    orderReference: String,
    selectedItems: List<Pair<Product, Int>>,
    total: Double,
    customTotal: String,
    isLoading: Boolean,
    errorMessage: String?,
    onCustomerNameChange: (String) -> Unit,
    onOrderReferenceChange: (String) -> Unit,
    onCustomTotalChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Confirmar Orden", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null)
                }
            }
            
            Spacer(Modifier.height(16.dp))

            // Información del Cliente en el Resumen
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = onCustomerNameChange,
                    label = { Text("Cliente", fontSize = 11.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.5f)
                )
                OutlinedTextField(
                    value = orderReference,
                    onValueChange = onOrderReferenceChange,
                    label = { Text("Ref.", fontSize = 11.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Box(modifier = Modifier.heightIn(max = 200.dp)) {
                        Column {
                            selectedItems.forEach { (product, qty) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${qty}x ${product.name}", fontSize = 14.sp)
                                    Text("$%.2f".format(product.priceBase * qty), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                    
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Calculado", fontSize = 12.sp, color = Color.Black)
                            Text("$%.2f".format(total), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                        
                        OutlinedTextField(
                            value = customTotal,
                            onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null || it.last() == '.') onCustomTotalChange(it) },
                            placeholder = { Text("Opcional", fontSize = 12.sp) },
                            label = { Text("Total Final", fontSize = 10.sp) },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End, fontWeight = FontWeight.Bold),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.width(110.dp)
                        )
                    }
                }
            }

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(size = 24.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Confirmar y Registrar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun CircularProgressIndicator(size: androidx.compose.ui.unit.Dp, color: Color) {
    CircularProgressIndicator(modifier = Modifier.size(size), color = color)
}

@Preview
@Composable
fun PreviewNewOrder() {
    NewOrderScreen()
}
