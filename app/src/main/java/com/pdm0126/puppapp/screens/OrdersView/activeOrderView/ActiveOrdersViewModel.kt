package com.pdm0126.puppapp.screens.OrdersView.activeOrderView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.puppapp.PupappApplication
import com.pdm0126.puppapp.components.OrderPreview
import com.pdm0126.puppapp.data.dto.UpdateOrderStatusRequest
import com.pdm0126.puppapp.data.model.Order
import com.pdm0126.puppapp.data.remote.PupappAPI.OrdersAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

data class ActiveOrdersUiState(
    val orders: List<OrderPreview> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

class ActiveOrdersViewModel(
    private val ordersAPI: OrdersAPI
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveOrdersUiState())
    val uiState: StateFlow<ActiveOrdersUiState> = _uiState.asStateFlow()

    init {
        // Observar Room continuamente
        viewModelScope.launch {
            ordersAPI.ordersFlow.collect { orders ->
                // Filtramos las órdenes activas si es necesario, 
                // o confiamos en que ordersFlow ya trae lo que necesitamos.
                // Según OrdersAPIImpl.getActiveOrders(), son status < 4.
                val activeOnes = orders.filter { it.statusId < 4 }
                _uiState.value = _uiState.value.copy(
                    orders = activeOnes.map { it.toPreview() }
                )
            }
        }
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            fetchOrders()
        }
    }

    fun refreshOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            fetchOrders()
        }
    }

    private suspend fun fetchOrders() {
        try {
            // Intentar sincronizar antes de pedir las nuevas, sin bloquear si falla
            try {
                ordersAPI.syncPendingOrders()
            } catch (e: Exception) { /* sync error */ }

            // Solo disparamos la petición. El Flow de arriba capturará los cambios en Room.
            ordersAPI.getActiveOrders()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isRefreshing = false
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isRefreshing = false,
                error = e.message ?: "Error al cargar las órdenes"
            )
        }
    }

    private fun Order.toPreview(): OrderPreview {
        val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        return OrderPreview(
            id = id,
            orderNumber = orderNumber,
            reference = orderReference,
            clientName = customerName,
            time = createdAt?.let { dateFormat.format(it) } ?: "--:--",
            itemCount = items.sumOf { it.quantity },
            total = finalTotal,
            statusId = statusId,
            items = items,
            showId = false
        )
    }

    fun updateOrderStatus(orderId: Int, statusId: Int) {
        viewModelScope.launch {
            try {
                ordersAPI.updateOrderStatus(orderId, UpdateOrderStatusRequest(statusId))
                fetchOrders()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al actualizar estado: ${e.localizedMessage ?: "Error desconocido"}"
                )
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PupappApplication
                ActiveOrdersViewModel(app.appProvider.provideOrdersRepository())
            }
        }
    }
}
