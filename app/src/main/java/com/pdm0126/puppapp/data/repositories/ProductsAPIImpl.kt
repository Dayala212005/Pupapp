package com.pdm0126.puppapp.data.repositories

import com.pdm0126.puppapp.data.dto.ProductDTO
import com.pdm0126.puppapp.data.dto.toModel
import com.pdm0126.puppapp.data.local.dao.ProductDao
import com.pdm0126.puppapp.data.local.entities.toEntity
import com.pdm0126.puppapp.data.local.entities.toModel
import com.pdm0126.puppapp.data.model.Product
import com.pdm0126.puppapp.data.remote.KtorClient
import com.pdm0126.puppapp.data.remote.PupappAPI.ProductsAPI
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ProductsAPIImpl(private val productDao: ProductDao) : ProductsAPI {
    private val client = KtorClient.client

    override val productsFlow: Flow<List<Product>> = productDao.getAllProducts().map { entities ->
        entities.map { it.toModel() }
    }

    override suspend fun getProducts(page: Int, limit: Int): List<Product> {
        return try {
            val response: List<ProductDTO> = client.get("/api/products") {
                parameter("page", page)
                parameter("limit", limit)
            }.body()
            val products = response.map { it.toModel() }
            
            // Simplemente insertamos. Room reemplazará los existentes por su ID.
            productDao.insertProducts(products.map { it.toEntity() })
            
            return products
        } catch (e: Exception) {
            // Si falla el internet, devolvemos lo que hay en Room
            productDao.getAllProducts().first().map { it.toModel() }
        }
    }

    override suspend fun getProductById(id: Int): Product {
        return try {
            val response: ProductDTO = client.get("/api/products/$id").body()
            val product = response.toModel()
            productDao.insertProduct(product.toEntity())
            product
        } catch (e: Exception) {
            productDao.getProductById(id)?.toModel() 
                ?: throw Exception("Producto no encontrado localmente ni en red")
        }
    }

    override suspend fun createProduct(
        name: String,
        priceBase: String,
        category: String,
        imageBytes: ByteArray?,
        imageName: String?
    ): Product {
        // 1. Generar ID temporal y guardar localmente
        val tempId = -(System.currentTimeMillis() % 1000000).toInt()
        val tempProduct = Product(
            id = tempId,
            restaurantId = 0, // El servidor suele asignar esto
            name = name,
            priceBase = priceBase.toDoubleOrNull() ?: 0.0,
            category = category,
            imageUrl = null
        )
        productDao.insertProduct(tempProduct.toEntity(isSynced = false, pendingAction = "CREATE"))

        return try {
            val response: ProductDTO = client.post("/api/products") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("name", name)
                            append("price_base", priceBase)
                            append("category", category)

                            if (imageBytes != null && imageName != null) {
                                append("image", imageBytes, Headers.Companion.build {
                                    append(HttpHeaders.ContentDisposition, "filename=\"$imageName\"")
                                    append(HttpHeaders.ContentType, "image/jpeg")
                                })
                            }
                        }
                    )
                )
            }.body()
            
            val product = response.toModel()
            // 2. Borrar el temporal e insertar el real
            productDao.deleteProduct(tempProduct.toEntity())
            productDao.insertProduct(product.toEntity(isSynced = true))
            product
        } catch (e: Exception) {
            // Si falla el internet, devolvemos el temporal. 
            // La UI lo mostrará porque productsFlow observa Room.
            tempProduct
        }
    }

    override suspend fun syncPendingProducts() {
        val pending = productDao.getUnsyncedProducts()
        pending.forEach { entity ->
            try {
                // Solo manejamos CREATE por ahora para simplificar
                if (entity.pendingAction == "CREATE") {
                    val response: ProductDTO = client.post("/api/products") {
                        setBody(
                            MultiPartFormDataContent(
                                formData {
                                    append("name", entity.name)
                                    append("price_base", entity.priceBase.toString())
                                    append("category", entity.category ?: "")
                                    // Nota: Las imágenes offline son más complejas, se omiten por ahora
                                }
                            )
                        )
                    }.body()
                    
                    val syncedProduct = response.toModel()
                    productDao.deleteProduct(entity) // Borra temporal
                    productDao.insertProduct(syncedProduct.toEntity(isSynced = true))
                }
            } catch (e: Exception) {
                // Ignorar y seguir con el siguiente
            }
        }
    }

    override suspend fun updateProduct(
        id: Int,
        name: String,
        priceBase: String,
        category: String,
        imageBytes: ByteArray?,
        imageName: String?
    ): Product {
        val response: ProductDTO = client.put("/api/products/$id") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("name", name)
                        append("price_base", priceBase)
                        append("category", category)

                        if (imageBytes != null && imageName != null) {
                            append("image", imageBytes, Headers.Companion.build {
                                append(HttpHeaders.ContentDisposition, "filename=\"$imageName\"")
                                append(HttpHeaders.ContentType, "image/jpeg")
                            })
                        }
                    }
                )
            )
        }.body()
        val product = response.toModel()
        productDao.insertProduct(product.toEntity())
        return product
    }

    override suspend fun deleteProduct(id: Int) {
        // Borramos en red y luego en local
        client.delete("/api/products/$id")
        val product = productDao.getProductById(id)
        if (product != null) {
            productDao.deleteProduct(product)
        }
    }
}
