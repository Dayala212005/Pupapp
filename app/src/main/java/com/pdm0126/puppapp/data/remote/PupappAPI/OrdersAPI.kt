package com.pdm0126.puppapp.data.remote.PupappAPI

import com.pdm0126.puppapp.data.dto.CreateOrderRequest
import com.pdm0126.puppapp.data.dto.UpdateOrderStatusRequest
import com.pdm0126.puppapp.data.model.Order

interface OrdersAPI {
    suspend fun getOrders(page: Int = 1, limit: Int = 10): List<Order>
    suspend fun getActiveOrders(): List<Order>
    suspend fun getOrderById(id: Int): Order
    suspend fun createOrder(request: CreateOrderRequest): Order
    suspend fun updateOrderStatus(id: Int, request: UpdateOrderStatusRequest): Order
    suspend fun deleteOrder(id: Int)
}
