package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userEmail: String,
    val userName: String,
    val totalAmount: Double,
    val discountAmount: Double,
    val couponCode: String?,
    val paymentGateway: String, // "Stripe", "PayPal", "Razorpay"
    val status: String, // "COMPLETED", "REFUNDED", "CANCELLED"
    val createdAt: Long = System.currentTimeMillis()
)
