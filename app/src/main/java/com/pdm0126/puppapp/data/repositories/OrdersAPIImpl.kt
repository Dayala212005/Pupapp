package com.pdm0126.puppapp.data.repositories

import com.pdm0126.puppapp.data.dto.CreateOrderItemRequest
import com.pdm0126.puppapp.data.dto.CreateOrderRequest
import com.pdm0126.puppapp.data.dto.OrderDTO
import com.pdm0126.puppapp.data.dto.UpdateOrderStatusRequest
import com.pdm0126.puppapp.data.dto.toModel
import com.pdm0126.puppapp.data.local.dao.OrderDao
import com.pdm0126.puppapp.data.local.dao.OrderItemDao
import com.pdm0126.puppapp.data.local.entities.toEntity
import com.pdm0126.puppapp.data.local.entities.toModel
import com.pdm0126.puppapp.data.model.Order
import com.pdm0126.puppapp.data.remote.KtorClient
import com.pdm0126.puppapp.data.remote.PupappAPI.OrdersAPI
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class OrdersAPIImpl(
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao
) : OrdersAPI {
    private val client get() = KtorClient.client

    override val ordersFlow: Flow<List<Order>> = orderDao.getAllOrdersWithItems().map { list ->
        list.map { it.toModel() }
    }

    override suspend fun getOrders(page: Int, limit: Int): List<Order> {
        val response = client.get("/api/orders") {
            parameter("page", page)
            parameter("limit", limit)
        }

        if (response.status.value != 200 && response.status.value != 201) {
            try {
                return loadOrdersFromLocal()
            } catch (e: Exception) {
                throw ResponseException(response, "Failed to fetch orders")
            }
        }

        val orderDTOs: List<OrderDTO> = response.body()
        val orders = orderDTOs.map { it.toModel() }
        saveOrdersToLocal(orders)
        return orders
    }

    override suspend fun getActiveOrders(): List<Order> {
        val response = client.get("/api/orders/active")

        if (response.status.value != 200 && response.status.value != 201) {
            try {
                return loadOrdersFromLocal().filter { it.statusId < 4 }
            } catch (e: Exception) {
                throw ResponseException(response, "Failed to fetch active orders")
            }
        }

        val orderDTOs: List<OrderDTO> = response.body()
        val orders = orderDTOs.map { it.toModel() }
        saveOrdersToLocal(orders)
        return orders
    }

    override suspend fun getOrderById(id: Int): Order {
        val response = client.get("/api/orders/$id")

        if (response.status.value != 200 && response.status.value != 201) {
            val withItems = orderDao.getOrderWithItemsById(id)
            return withItems?.toModel() ?: throw ResponseException(response, "Order not found")
        }

        val orderDTO: OrderDTO = response.body()
        val order = orderDTO.toModel()
        val existing = orderDao.getOrderWithItemsById(id)
        if (existing != null && !existing.order.isSynced) {
            return existing.toModel()
        }
        orderDao.insertOrder(order.toEntity(isSynced = true))
        orderItemDao.deleteItemsForOrder(order.id)
        val entities = order.items.map { it.toEntity().copy(orderId = order.id) }
        orderItemDao.insertOrderItems(entities)
        return order
    }

    override suspend fun getDeliveredOrdersByPeriod(startDate: String, endDate: String): List<Order> {
        val response = client.get("/api/orders/delivered") {
            parameter("startDate", startDate)
            parameter("endDate", endDate)
        }

        if (response.status.value != 200 && response.status.value != 201) {
            try {
                return loadOrdersFromLocal().filter { it.statusId == 4 }
            } catch (e: Exception) {
                throw ResponseException(response, "Failed to fetch delivered orders")
            }
        }

        val orderDTOs: List<OrderDTO> = response.body()
        val orders = orderDTOs.map { it.toModel() }
        saveOrdersToLocal(orders)
        return orders
    }

    override suspend fun createOrder(request: CreateOrderRequest): Order {
        val tempId = -(System.currentTimeMillis() % 1000000).toInt()
        val tempOrder = Order(
            id = tempId,
            restaurantId = 0,
            statusId = 1,
            orderNumber = 0,
            orderReference = request.orderReference,
            customerName = request.customerName,
            calculatedSubtotal = 0.0,
            finalTotal = request.finalTotal ?: 0.0,
            totalAdjustmentNote = request.totalAdjustmentNote,
            createdAt = java.util.Date(),
            updatedAt = java.util.Date(),
            items = request.items.map { itemReq ->
                com.pdm0126.puppapp.data.model.OrderItem(
                    id = 0,
                    orderId = tempId,
                    productId = itemReq.productId,
                    productName = itemReq.productName ?: "Producto",
                    quantity = itemReq.quantity,
                    pricePerDish = 0.0
                )
            }
        )
        orderDao.insertOrder(tempOrder.toEntity(isSynced = false, pendingAction = "CREATE"))
        orderItemDao.insertOrderItems(tempOrder.items.map { it.toEntity() })
        return try {
            val response: OrderDTO = client.post("/api/orders") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            val order = response.toModel()
            orderDao.deleteOrder(tempOrder.toEntity())
            orderItemDao.deleteItemsForOrder(tempId)
            orderDao.insertOrder(order.toEntity(isSynced = true))
            val entities = order.items.map { it.toEntity().copy(orderId = order.id) }
            orderItemDao.insertOrderItems(entities)
            order
        } catch (e: Exception) {
            tempOrder
        }
    }

    override suspend fun syncPendingOrders() {
        val pending = orderDao.getUnsyncedOrders()
        pending.forEach { entity ->
            try {
                if (entity.pendingAction == "CREATE") {
                    val localItems = orderItemDao.getItemsForOrder(entity.id)
                    val request = CreateOrderRequest(
                        customerName = entity.customerName ?: "",
                        orderReference = entity.orderReference ?: "",
                        finalTotal = entity.finalTotal,
                        totalAdjustmentNote = entity.totalAdjustmentNote,
                        items = localItems.map {
                            CreateOrderItemRequest(
                                productId = it.productId,
                                quantity = it.quantity,
                                productName = it.productName
                            )
                        }
                    )
                    val response: OrderDTO = client.post("/api/orders") {
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }.body()
                    val synced = response.toModel()
                    orderDao.deleteOrder(entity)
                    orderItemDao.deleteItemsForOrder(entity.id)
                    orderDao.insertOrder(synced.toEntity(isSynced = true))
                    val entities = synced.items.map { it.toEntity().copy(orderId = synced.id) }
                    orderItemDao.insertOrderItems(entities)
                } else if (entity.pendingAction == "UPDATE") {
                    val request = UpdateOrderStatusRequest(entity.statusId)
                    client.patch("/api/orders/${entity.id}/status") {
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }
                    orderDao.insertOrder(entity.copy(isSynced = true, pendingAction = null))
                }
            } catch (e: Exception) { }
        }
    }

    override suspend fun updateOrderStatus(id: Int, request: UpdateOrderStatusRequest): Order {
        return try {
            val response: OrderDTO = client.patch("/api/orders/$id/status") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            val order = response.toModel()
            orderDao.insertOrder(order.toEntity(isSynced = true))
            order
        } catch (e: Exception) {
            val withItems = orderDao.getOrderWithItemsById(id)
            if (withItems != null) {
                val updatedEntity = withItems.order.copy(
                    statusId = request.statusId,
                    isSynced = false,
                    pendingAction = if (withItems.order.id < 0) "CREATE" else "UPDATE"
                )
                orderDao.insertOrder(updatedEntity)
                updatedEntity.toModel(withItems.items.map { it.toModel() })
            } else {
                throw e
            }
        }
    }

    override suspend fun deleteOrder(id: Int) {
        client.delete("/api/orders/$id")
        val withItems = orderDao.getOrderWithItemsById(id)
        if (withItems != null) {
            orderDao.deleteOrder(withItems.order)
            orderItemDao.deleteItemsForOrder(id)
        }
    }

    private suspend fun saveOrdersToLocal(orders: List<Order>) {
        orders.forEach { order ->
            if (order.id > 0) {
                val existing = orderDao.getOrderWithItemsById(order.id)
                if (existing != null && !existing.order.isSynced) {
                    return@forEach
                }
                orderDao.insertOrder(order.toEntity(isSynced = true))
                orderItemDao.deleteItemsForOrder(order.id)
                val entities = order.items.map { it.toEntity().copy(orderId = order.id) }
                orderItemDao.insertOrderItems(entities)
            }
        }
    }

    private suspend fun loadOrdersFromLocal(): List<Order> {
        return orderDao.getAllOrdersWithItems().first().map { it.toModel() }
    }
}
