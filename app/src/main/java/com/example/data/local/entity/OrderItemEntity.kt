package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val productId: String,
    val productTitle: String,
    val priceAtPurchase: Double,
    val versionAtPurchase: String,
    val imageResName: String
)
