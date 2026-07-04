package com.pdm0126.puppapp.data.remote.PupappAPI

import com.pdm0126.puppapp.data.dto.CreateOrderRequest
import com.pdm0126.puppapp.data.dto.UpdateOrderStatusRequest
import com.pdm0126.puppapp.data.model.Order
import kotlinx.coroutines.flow.Flow

interface OrdersAPI {
    val ordersFlow: Flow<List<Order>>
    suspend fun getOrders(page: Int = 1, limit: Int = 10): List<Order>
    suspend fun getActiveOrders(): List<Order>
    suspend fun getOrderById(id: Int): Order

    suspend fun getDeliveredOrdersByPeriod(startDate: String, endDate: String): List<Order>
    suspend fun createOrder(request: CreateOrderRequest): Order
    suspend fun updateOrderStatus(id: Int, request: UpdateOrderStatusRequest): Order
    suspend fun deleteOrder(id: Int)
    suspend fun syncPendingOrders()
}
