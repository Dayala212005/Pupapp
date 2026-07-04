package com.pdm0126.puppapp.screens.historyView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.puppapp.PupappApplication
import com.pdm0126.puppapp.components.OrderPreview
import com.pdm0126.puppapp.data.model.Order
import com.pdm0126.puppapp.data.remote.PupappAPI.OrdersAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class HistoryPeriod(val label: String) {
    WEEK("Semana"),
    MONTH("Mes"),
    QUARTER("Trimestre"),
    YEAR("Año")
}

data class HistorySummary(
    val totalRevenue: Double = 0.0,
    val totalOrders: Int = 0,
    val cancelledOrders: Int = 0
)

data class HistoryUiState(
    val allOrders: List<OrderPreview> = emptyList(),
    val deliveredOrders: List<OrderPreview> = emptyList(),
    val summary: HistorySummary = HistorySummary(),
    val isLoadingAll: Boolean = false,
    val isLoadingDelivered: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val hasReachedEnd: Boolean = false,
    val selectedPeriod: HistoryPeriod = HistoryPeriod.WEEK
)

class HistoryViewModel(
    private val ordersAPI: OrdersAPI
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val pageSize = 10

    init {
        // Opción B: Observar Room
        viewModelScope.launch {
            ordersAPI.ordersFlow.collect { orders ->
                // Actualizamos la lista completa (allOrders) desde Room
                // Nota: Esto ignora la paginación de la UI por simplicidad, 
                // pero asegura que los datos no "desaparezcan" sin red.
                _uiState.value = _uiState.value.copy(
                    allOrders = orders.map { it.toPreview() }
                )
            }
        }
        loadInitialData()
    }

    private fun loadInitialData() {
        loadAllOrders(reset = true)
        loadDeliveredOrders()
    }

    fun loadAllOrders(reset: Boolean = false) {
        if (_uiState.value.isLoadingAll || (_uiState.value.hasReachedEnd && !reset)) return

        viewModelScope.launch {
            val pageToLoad = if (reset) 1 else _uiState.value.currentPage
            _uiState.value = _uiState.value.copy(
                isLoadingAll = !reset, // No mostramos el skeleton principal si es un refresh manual
                currentPage = pageToLoad,
                hasReachedEnd = if (reset) false else _uiState.value.hasReachedEnd
            )

            try {
                try {
                    ordersAPI.syncPendingOrders()
                } catch (e: Exception) { /* Ignorar error de sync para no bloquear carga */ }
                
                val orders = ordersAPI.getOrders(page = pageToLoad, limit = pageSize)
                val newPreviews = orders.map { it.toPreview() }
                
                _uiState.value = _uiState.value.copy(
                    allOrders = if (reset) newPreviews else _uiState.value.allOrders + newPreviews,
                    isLoadingAll = false,
                    currentPage = pageToLoad + 1,
                    hasReachedEnd = orders.size < pageSize
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingAll = false,
                    error = if (_uiState.value.allOrders.isEmpty()) (e.message ?: "Error al cargar el historial") else null
                )
            }
        }
    }

    fun setPeriod(period: HistoryPeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadDeliveredOrders()
    }

    fun loadDeliveredOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingDelivered = true)
            
            val (startDate, endDate) = getDatesForPeriod(_uiState.value.selectedPeriod)
            
            try {
                try {
                    ordersAPI.syncPendingOrders()
                } catch (e: Exception) { /* Ignorar */ }

                val orders = ordersAPI.getDeliveredOrdersByPeriod(startDate, endDate)
                
                val totalRevenue = orders.sumOf { it.finalTotal }
                val totalCount = orders.size
                
                _uiState.value = _uiState.value.copy(
                    deliveredOrders = orders.map { it.toPreview() },
                    summary = HistorySummary(
                        totalRevenue = totalRevenue,
                        totalOrders = totalCount,
                        cancelledOrders = 0 
                    ),
                    isLoadingDelivered = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingDelivered = false,
                    error = e.message ?: "Error al cargar órdenes entregadas"
                )
            }
        }
    }

    private fun getDatesForPeriod(period: HistoryPeriod): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        
        val endDate = formatter.format(calendar.time)
        
        when (period) {
            HistoryPeriod.WEEK -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            HistoryPeriod.MONTH -> calendar.add(Calendar.MONTH, -1)
            HistoryPeriod.QUARTER -> calendar.add(Calendar.MONTH, -3)
            HistoryPeriod.YEAR -> calendar.add(Calendar.YEAR, -1)
        }
        
        val startDate = formatter.format(calendar.time)
        return Pair(startDate, endDate)
    }

    private fun Order.toPreview(): OrderPreview {
        val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
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
            showId = true
        )
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            loadAllOrders(reset = true)
            loadDeliveredOrders()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PupappApplication
                HistoryViewModel(app.appProvider.provideOrdersRepository())
            }
        }
    }
}
