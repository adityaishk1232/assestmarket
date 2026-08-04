package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val name: String,
    val role: String, // "CUSTOMER" or "ADMIN"
    val password: String = "password123",
    val avatarUrl: String = "",
    val isBlocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

