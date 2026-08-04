package com.example.data.local.entity

import androidx.room.Entity

@Entity(tableName = "wishlists", primaryKeys = ["userId", "productId"])
data class WishlistEntity(
    val userId: String,
    val productId: String,
    val addedAt: Long = System.currentTimeMillis()
)
