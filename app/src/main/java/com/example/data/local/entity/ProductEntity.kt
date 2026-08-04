package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val categoryId: String,
    val price: Double,
    val discountPrice: Double? = null,
    val version: String,
    val fileSize: String,
    val fileType: String,
    val fileDownloadUrl: String,
    val imageResName: String,
    val tags: String,
    val rating: Float = 5.0f,
    val reviewCount: Int = 0,
    val salesCount: Int = 0,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
