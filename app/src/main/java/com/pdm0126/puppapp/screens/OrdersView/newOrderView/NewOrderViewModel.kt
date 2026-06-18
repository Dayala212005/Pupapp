package com.pdm0126.puppapp.screens.OrdersView.newOrderView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdm0126.puppapp.data.dto.CreateOrderItemRequest
import com.pdm0126.puppapp.data.dto.CreateOrderRequest
import com.pdm0126.puppapp.data.model.Product
import com.pdm0126.puppapp.data.remote.PupappAPI.OrdersAPI
import com.pdm0126.puppapp.data.remote.PupappAPI.ProductsAPI
import com.pdm0126.puppapp.data.repositories.OrdersAPIImpl
import com.pdm0126.puppapp.data.repositories.ProductsAPIImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NewOrderViewModel(
    private val ordersAPI: OrdersAPI = OrdersAPIImpl(),
    private val productsAPI: ProductsAPI = ProductsAPIImpl()
) : ViewModel() {

    // Productos del menú
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    // Mapa de productId -> cantidad seleccionada
    private val _quantities = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val quantities = _quantities.asStateFlow()

    // Campos de la orden
    private val _customerName = MutableStateFlow("")
    val customerName = _customerName.asStateFlow()

    private val _adjustmentNote = MutableStateFlow("")
    val adjustmentNote = _adjustmentNote.asStateFlow()

    // Estados UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _orderSuccess = MutableStateFlow(false)
    val orderSuccess = _orderSuccess.asStateFlow()

    // Productos agrupados por categoría
    val groupedProducts: StateFlow<Map<String, List<Product>>> = _products
        .map { list -> list.groupBy { it.category ?: "Sin categoría" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Items seleccionados (quantity > 0)
    val selectedItems: StateFlow<List<Pair<Product, Int>>> = combine(_products, _quantities) { products, quantities ->
        products.mapNotNull { product ->
            val qty = quantities[product.id] ?: 0
            if (qty > 0) product to qty else null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Total calculado
    val total: StateFlow<Double> = selectedItems
        .map { items -> items.sumOf { (product, qty) -> product.priceBase * qty } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _products.value = productsAPI.getProducts(page = 1, limit = 100)
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar productos: ${e.localizedMessage ?: "Error desconocido"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onCustomerNameChange(value: String) { _customerName.value = value }
    fun onAdjustmentNoteChange(value: String) { _adjustmentNote.value = value }

    fun onIncrease(productId: Int) {
        val current = _quantities.value.toMutableMap()
        current[productId] = (current[productId] ?: 0) + 1
        _quantities.value = current
    }

    fun onDecrease(productId: Int) {
        val current = _quantities.value.toMutableMap()
        val qty = (current[productId] ?: 0) - 1
        if (qty <= 0) current.remove(productId) else current[productId] = qty
        _quantities.value = current
    }

    fun resetOrderSuccess() { _orderSuccess.value = false }

    fun onCreateOrderClick() {
        val items = selectedItems.value
        if (items.isEmpty()) {
            _errorMessage.value = "Agrega al menos un producto"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                ordersAPI.createOrder(
                    CreateOrderRequest(
                        orderReference = null,
                        customerName = _customerName.value.ifBlank { null },
                        finalTotal = total.value,
                        totalAdjustmentNote = _adjustmentNote.value.ifBlank { null },
                        items = items.map { (product, qty) ->
                            CreateOrderItemRequest(
                                productId = product.id,
                                quantity = qty
                            )
                        }
                    )
                )
                _orderSuccess.value = true
                resetForm()
            } catch (e: Exception) {
                _errorMessage.value = "Error al crear orden: ${e.localizedMessage ?: "Error desconocido"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun resetForm() {
        _customerName.value  = ""
        _adjustmentNote.value = ""
        _quantities.value    = emptyMap()
    }
}