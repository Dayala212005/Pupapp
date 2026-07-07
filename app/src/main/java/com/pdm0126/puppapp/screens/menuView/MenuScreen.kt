package com.pdm0126.puppapp.screens.menuView

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                        text = "Aún no tienes productos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Pulsa el botón de + para agregar tu primer producto al menú",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                    groupedProducts.forEach { (category, items) ->
                        item(span = { GridItemSpan(2) }) {
                            CategoryHeader(
                                title    = category,
                                onAdd    = { viewModel.openCreateDialog(category) }
                            )
                        }
                        items(items) { product ->
                            MenuProductCard(
                                product  = product,
                                onEdit   = { viewModel.openEditDialog(product) },
                                onDelete = { viewModel.onDeleteClick(product.id) }
                            )
                        }
                    }
                    item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.openCreateDialog("") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add, 
                contentDescription = "Agregar producto",
                modifier = Modifier.size(28.dp)
            )
        }

        if (showDialog) {
            ProductDialog(viewModel = viewModel)
        }
    }
}

// ── Category header ──────────────────────────────────────────────────────────

@Composable
private fun CategoryHeader(title: String, onAdd: () -> Unit) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier              = Modifier.fillMaxWidth()
    ) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        TextButton(
            onClick        = onAdd,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Agregar", fontSize = 12.sp)
        }
    }
}

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
