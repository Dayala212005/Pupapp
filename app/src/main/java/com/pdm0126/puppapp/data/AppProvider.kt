package com.pdm0126.puppapp.data

import android.content.Context
import com.pdm0126.puppapp.data.local.AppDatabase
import com.pdm0126.puppapp.data.remote.PupappAPI.AuthAPI
import com.pdm0126.puppapp.data.remote.PupappAPI.OrdersAPI
import com.pdm0126.puppapp.data.remote.PupappAPI.ProductsAPI
import com.pdm0126.puppapp.data.repositories.AuthAPIImpl
import com.pdm0126.puppapp.data.repositories.OrdersAPIImpl
import com.pdm0126.puppapp.data.repositories.ProductsAPIImpl

class AppProvider(context: Context) {

    private val appDatabase = AppDatabase.getDatabase(context)

    private val productDao = appDatabase.productDao()
    private val orderDao = appDatabase.orderDao()
    private val orderItemDao = appDatabase.orderItemDao()

    private val productsRepository: ProductsAPI = ProductsAPIImpl(productDao)
    private val ordersRepository: OrdersAPI = OrdersAPIImpl(orderDao, orderItemDao)
    private val authRepository: AuthAPI = AuthAPIImpl(appDatabase)

    fun provideProductsRepository(): ProductsAPI {
        return productsRepository
    }

    fun provideOrdersRepository(): OrdersAPI {
        return ordersRepository
    }

    fun provideAuthRepository(): AuthAPI {
        return authRepository
    }

    fun getDatabase() = appDatabase
}
