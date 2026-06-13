package com.pdm0126.puppapp.data.repositories

import com.pdm0126.puppapp.data.dto.ProductDTO
import com.pdm0126.puppapp.data.dto.toModel
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

class ProductsAPIImpl : ProductsAPI {
    private val client = KtorClient.client

    override suspend fun getProducts(page: Int, limit: Int): List<Product> {
        val response: List<ProductDTO> = client.get("/api/products") {
            parameter("page", page)
            parameter("limit", limit)
        }.body()
        return response.map { it.toModel() }
    }

    override suspend fun getProductById(id: Int): Product {
        val response: ProductDTO = client.get("/api/products/$id").body()
        return response.toModel()
    }

    override suspend fun createProduct(
        name: String,
        priceBase: String,
        category: String,
        imageBytes: ByteArray?,
        imageName: String?
    ): Product {
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
        return response.toModel()
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
        return response.toModel()
    }

    override suspend fun deleteProduct(id: Int) {
        client.delete("/api/products/$id")
    }
}