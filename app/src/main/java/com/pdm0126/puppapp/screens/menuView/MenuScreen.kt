package com.pdm0126.puppapp.screens.menuView

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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

@Composable
fun MenuScreen(
    padding: PaddingValues = PaddingValues(),
    viewModel: MenuViewModel = viewModel()
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
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    groupedProducts.forEach { (category, items) ->
                        item {
                            CategoryHeader(
                                title    = category,
                                onAdd    = { viewModel.openCreateDialog(category) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        items.forEach { product ->
                            item {
                                MenuItemRow(
                                    product  = product,
                                    onEdit   = { viewModel.openEditDialog(product) },
                                    onDelete = { viewModel.onDeleteClick(product.id) }
                                )
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
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

// ── Menu item row ────────────────────────────────────────────────────────────
@Composable
private fun MenuItemRow(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape    = RoundedCornerShape(10.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border   = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Imagen del producto
            AsyncImage(
                model         = product.imageUrl,
                contentDescription = product.name,
                contentScale  = ContentScale.Crop,
                modifier      = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                error         = painterResource(id = android.R.drawable.ic_menu_gallery),
                placeholder   = painterResource(id = android.R.drawable.ic_menu_gallery)
            )

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(
                    text     = product.category ?: "Sin categoría",
                    fontSize = 11.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text       = "$%.2f".format(product.priceBase),
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.primary,
                modifier   = Modifier.padding(end = 8.dp)
            )

            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector        = Icons.Outlined.Edit,
                    contentDescription = "Editar",
                    modifier           = Modifier.size(18.dp),
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
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

// ── Product dialog ────────────────────────────────────────────────────────────
@Composable
private fun ProductDialog(viewModel: MenuViewModel) {
    val name           by viewModel.name.collectAsState()
    val priceBase      by viewModel.priceBase.collectAsState()
    val category       by viewModel.category.collectAsState()
    val editingProduct by viewModel.editingProduct.collectAsState()
    val isLoading      by viewModel.isLoading.collectAsState()

    val context = LocalContext.current

    // Launcher para abrir la galería
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val bytes = context.contentResolver.openInputStream(it)?.readBytes()
            val fileName = it.lastPathSegment ?: "image.jpg"
            if (bytes != null) {
                viewModel.onImageSelected(bytes, fileName)
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

                // Botón para seleccionar imagen
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
                val imageBytes by viewModel.imageBytes.collectAsState()
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

@Preview
@Composable
fun PreviewMenu() {
    MenuScreen()
}
