package com.pdm0126.puppapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pdm0126.puppapp.data.local.converters.DateConverters
import com.pdm0126.puppapp.data.local.dao.OrderDao
import com.pdm0126.puppapp.data.local.dao.OrderItemDao
import com.pdm0126.puppapp.data.local.dao.ProductDao
import com.pdm0126.puppapp.data.local.entities.OrderEntity
import com.pdm0126.puppapp.data.local.entities.OrderItemEntity
import com.pdm0126.puppapp.data.local.entities.ProductEntity

@Database(
    entities = [
        ProductEntity::class,
        OrderEntity::class,
        OrderItemEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun orderItemDao(): OrderItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "puppapp_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
