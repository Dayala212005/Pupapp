package com.pdm0126.puppapp.screens.menuView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.puppapp.PupappApplication
import com.pdm0126.puppapp.data.model.Product
import com.pdm0126.puppapp.data.remote.PupappAPI.ProductsAPI
import com.pdm0126.puppapp.utils.toUserFriendlyMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MenuViewModel(
    private val productsAPI: ProductsAPI
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _showDialog = MutableStateFlow(false)
    val showDialog = _showDialog.asStateFlow()

    private val _editingProduct = MutableStateFlow<Product?>(null)
    val editingProduct = _editingProduct.asStateFlow()

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _priceBase = MutableStateFlow("")
    val priceBase = _priceBase.asStateFlow()

    private val _category = MutableStateFlow("")
    val category = _category.asStateFlow()

    private val _imageBytes = MutableStateFlow<ByteArray?>(null)
    val imageBytes = _imageBytes.asStateFlow()

    private val _imageName = MutableStateFlow<String?>(null)
    val imageName = _imageName.asStateFlow()

    init {
        viewModelScope.launch {
            productsAPI.productsFlow.collect { list ->
                _products.value = list
            }
        }
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                productsAPI.syncPendingProducts()
                productsAPI.getProducts(page = 1, limit = 50)
            } catch (e: Exception) {
                _errorMessage.value = e.toUserFriendlyMessage()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onNameChange(value: String)      { _name.value = value }
    fun onPriceBaseChange(value: String) { _priceBase.value = value }
    fun onCategoryChange(value: String)  { _category.value = value }
    fun onImageSelected(bytes: ByteArray, fileName: String) {
        _imageBytes.value = bytes
        _imageName.value  = fileName
    }

    fun openCreateDialog(category: String) {
        _editingProduct.value = null
        _name.value      = ""
        _priceBase.value = ""
        _category.value  = category
        _imageBytes.value = null
        _imageName.value  = null
        _showDialog.value = true
    }

    fun openEditDialog(product: Product) {
        _editingProduct.value = product
        _name.value      = product.name
        _priceBase.value = product.priceBase.toString()
        _category.value = product.category ?: ""
        _imageBytes.value = null
        _imageName.value  = null
        _showDialog.value = true
    }

    fun closeDialog() {
        _showDialog.value = false
    }

    fun onSaveClick() {
        if (_name.value.isBlank() || _priceBase.value.isBlank() || _category.value.isBlank()) {
            _errorMessage.value = "Por favor, completa todos los campos"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val editing = _editingProduct.value
                if (editing == null) {
                    productsAPI.createProduct(
                        name       = _name.value,
                        priceBase  = _priceBase.value,
                        category   = _category.value,
                        imageBytes = _imageBytes.value,
                        imageName  = _imageName.value
                    )
                } else {
                    productsAPI.updateProduct(
                        id         = editing.id,
                        name       = _name.value,
                        priceBase  = _priceBase.value,
                        category   = _category.value,
                        imageBytes = _imageBytes.value,
                        imageName  = _imageName.value
                    )
                }
                _showDialog.value = false
                loadProducts()
            } catch (e: Exception) {
                _errorMessage.value = e.toUserFriendlyMessage()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onDeleteClick(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                productsAPI.deleteProduct(id)
                loadProducts()
            } catch (e: Exception) {
                _errorMessage.value = e.toUserFriendlyMessage()
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PupappApplication
                MenuViewModel(app.appProvider.provideProductsRepository())
            }
        }
    }
}
