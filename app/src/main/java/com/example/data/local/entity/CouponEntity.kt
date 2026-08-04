package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coupons")
data class CouponEntity(
    @PrimaryKey val id: String,
    val code: String,
    val discountPercent: Int = 0,
    val discountFixed: Double = 0.0,
    val minPurchase: Double = 0.0,
    val expiryTimestamp: Long,
    val usageLimit: Int = 100,
    val timesUsed: Int = 0,
    val isActive: Boolean = true
)
