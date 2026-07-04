package com.pdm0126.puppapp.data.remote.PupappAPI

import com.pdm0126.puppapp.data.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductsAPI {
    val productsFlow: Flow<List<Product>>
    suspend fun getProducts(page: Int = 1, limit: Int = 10): List<Product>
    suspend fun getProductById(id: Int): Product
    suspend fun createProduct(
        name: String,
        priceBase: String,
        category: String,
        imageBytes: ByteArray?,
        imageName: String?
    ): Product
    suspend fun updateProduct(
        id: Int,
        name: String,
        priceBase: String,
        category: String,
        imageBytes: ByteArray?,
        imageName: String?
    ): Product
    suspend fun deleteProduct(id: Int)
    suspend fun syncPendingProducts()
}
