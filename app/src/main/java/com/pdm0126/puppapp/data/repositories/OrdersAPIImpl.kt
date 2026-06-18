package com.pdm0126.puppapp.data.repositories

import com.pdm0126.puppapp.data.dto.CreateOrderRequest
import com.pdm0126.puppapp.data.dto.OrderDTO
import com.pdm0126.puppapp.data.dto.UpdateOrderStatusRequest
import com.pdm0126.puppapp.data.dto.toModel
import com.pdm0126.puppapp.data.model.Order
import com.pdm0126.puppapp.data.remote.KtorClient
import com.pdm0126.puppapp.data.remote.PupappAPI.OrdersAPI
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class OrdersAPIImpl : OrdersAPI {
    private val client = KtorClient.client

    override suspend fun getOrders(page: Int, limit: Int): List<Order> {
        val response: List<OrderDTO> = client.get("/api/orders") {
            parameter("page", page)
            parameter("limit", limit)
        }.body()
        return response.map { it.toModel() }
    }

    override suspend fun getActiveOrders(): List<Order> {
        val response: List<OrderDTO> = client.get("/api/orders/active").body()
        return response.map { it.toModel() }
    }

    override suspend fun getOrderById(id: Int): Order {
        val response: OrderDTO = client.get("/api/orders/$id").body()
        return response.toModel()
    }

    override suspend fun getDeliveredOrdersByPeriod(startDate: String, endDate: String): List<Order> {
        val response: List<OrderDTO> = client.get("/api/orders/delivered") {
            parameter("startDate", startDate)
            parameter("endDate", endDate)
        }.body()
        return response.map { it.toModel() }
    }

    override suspend fun createOrder(request: CreateOrderRequest): Order {
        val response: OrderDTO = client.post("/api/orders") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        return response.toModel()
    }

    override suspend fun updateOrderStatus(id: Int, request: UpdateOrderStatusRequest): Order {
        val response: OrderDTO = client.patch("/api/orders/$id/status") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        return response.toModel()
    }

    override suspend fun deleteOrder(id: Int) {
        client.delete("/api/orders/$id")
    }
}