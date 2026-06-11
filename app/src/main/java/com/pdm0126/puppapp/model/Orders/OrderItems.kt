package com.pdm0126.puppapp.model.Orders

import com.pdm0126.puppapp.model.Menu.Product

data class OrderItem(
    val product: Product,
    val quantity: Int,
    val unitPrice: Double = product.price
) {
    val subtotal: Double get() = unitPrice * quantity
}
