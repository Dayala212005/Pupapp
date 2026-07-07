package com.pdm0126.puppapp.screens.menuView

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.pdm0126.puppapp.data.model.Product
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

@Composable
fun MenuScreen(
    padding: PaddingValues = PaddingValues(),
    viewModel: MenuViewModel = viewModel(factory = MenuViewModel.Factory)
) {
    val products     by viewModel.products.collectAsState()
    val isLoading    by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val showDialog   by viewModel.showDialog.collectAsState()

    var collapsedCategories by remember { mutableStateOf(setOf<String>()) }

    // Agrupar productos por categoría
    val groupedProducts = products.groupBy { it.category ?: "Sin categoría" }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
        // Icono de fondo decorativo
        Icon(
            imageVector = Icons.Default.Fastfood,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.BottomCenter)
                .offset(y = 100.dp)
        )

        when {
            isLoading && products.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            errorMessage != null && products.isEmpty() -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadProducts() }) {
                        Text("Reintentar")
                    }
                }
            }
            products.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Fastfood,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "¡Tu menú está esperando!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Agrega tus productos y organízalos por categorías para empezar a vender.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.openCreateDialog("") },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(0.8f).height(54.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Agregar mi primer producto", fontWeight = FontWeight.Bold)
                    }
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item(span = { GridItemSpan(2) }) {
                        Card(
                            onClick = { viewModel.openCreateDialog("") },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Add, 
                                    null, 
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Nuevo Producto", 
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    groupedProducts.forEach { (category, items) ->
                        val isCollapsed = collapsedCategories.contains(category)

                        item(span = { GridItemSpan(2) }) {
                            CategoryHeader(
                                title    = category,
                                isCollapsed = isCollapsed,
                                onToggle = {
                                    collapsedCategories = if (isCollapsed) {
                                        collapsedCategories - category
                                    } else {
                                        collapsedCategories + category
                                    }
                                },
                                onAdd = { viewModel.openCreateDialog(category) }
                            )
                        }
                        
                        if (!isCollapsed) {
                            items(items) { product ->
                                MenuProductCard(
                                    product  = product,
                                    onEdit   = { viewModel.openEditDialog(product) },
                                    onDelete = { viewModel.onDeleteClick(product.id) }
                                )
                            }
                        }
                    }
                    item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
        
        // El FAB se eliminó para evitar duplicidad con el de órdenes
        
        if (showDialog) {
            ProductDialog(viewModel = viewModel)
        }
    }
}

// ── Category header ──────────────────────────────────────────────────────────

@Composable
private fun CategoryHeader(
    title: String, 
    isCollapsed: Boolean,
    onToggle: () -> Unit,
    onAdd: () -> Unit
) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier              = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            IconButton(onClick = onAdd) {
                Icon(
                    imageVector = Icons.Default.Add, 
                    contentDescription = "Añadir a $title",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Se eliminó AddProductPlaceholder

// ── Menu product card ────────────────────────────────────────────────────────────
@Composable
private fun MenuProductCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
                    .height(110.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model         = product.imageUrl,
                    contentDescription = product.name,
                    contentScale  = ContentScale.Crop,
                    modifier      = Modifier.fillMaxSize(),
                    error         = painterResource(id = android.R.drawable.ic_menu_gallery),
                    placeholder   = painterResource(id = android.R.drawable.ic_menu_gallery)
                )
                
                // Botones de acción flotantes sobre la imagen
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                ) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Outlined.Edit, null, Modifier.size(16.dp), MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Outlined.Delete, null, Modifier.size(16.dp), MaterialTheme.colorScheme.error)
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    product.name, 
                    fontSize = 14.sp, 
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text     = "$%.2f".format(product.priceBase),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color    = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ── Product dialog ────────────────────────────────────────────────────────────
@Composable
private fun ProductDialog(viewModel: MenuViewModel) {
    val name           by viewModel.name.collectAsState()
    val priceBase      by viewModel.priceBase.collectAsState()
    val category       by viewModel.category.collectAsState()
    val editingProduct by viewModel.editingProduct.collectAsState()
    val isLoading      by viewModel.isLoading.collectAsState()
    val imageBytes     by viewModel.imageBytes.collectAsState()
    val products       by viewModel.products.collectAsState()

    val categories = remember(products) {
        products.mapNotNull { it.category }.distinct()
    }

    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap != null) {
                val compressedBytes = compressBitmap(originalBitmap)
                viewModel.onImageSelected(compressedBytes, "image.jpg")
            }
        }
    }

    AlertDialog(
        onDismissRequest = { viewModel.closeDialog() },
        title = {
            Text(if (editingProduct == null) "Agregar producto" else "Editar producto")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value         = name,
                    onValueChange = { viewModel.onNameChange(it) },
                    label         = { Text("Nombre") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value         = priceBase,
                    onValueChange = { viewModel.onPriceBaseChange(it) },
                    label         = { Text("Precio base") },
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier      = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value         = category,
                    onValueChange = { viewModel.onCategoryChange(it) },
                    label         = { Text("Categoría") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )

                if (categories.isNotEmpty()) {
                    Text("Categorías existentes:", style = MaterialTheme.typography.labelSmall)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick  = { viewModel.onCategoryChange(cat) },
                                label    = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick  = {
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Seleccionar imagen")
                }

                // Preview de la imagen seleccionada
                if (imageBytes != null) {
                    val bitmap = remember(imageBytes) {
                        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes!!.size)
                    }
                    Image(
                        bitmap      = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier    = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.onSaveClick() },
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp))
                else Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.closeDialog() }) {
                Text("Cancelar")
            }
        }
    )
}
private fun compressBitmap(bitmap: Bitmap, maxDimension: Int = 800, quality: Int = 80): ByteArray {
    // Redimensionar si es muy grande
    val ratio = minOf(
        maxDimension.toFloat() / bitmap.width,
        maxDimension.toFloat() / bitmap.height,
        1f // no agrandar imágenes pequeñas
    )

    val resizedBitmap = if (ratio < 1f) {
        Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).toInt(),
            (bitmap.height * ratio).toInt(),
            true
        )
    } else {
        bitmap
    }

    val outputStream = ByteArrayOutputStream()
    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
    return outputStream.toByteArray()
}

@Preview
@Composable
fun PreviewMenu() {
    MenuScreen()
}
