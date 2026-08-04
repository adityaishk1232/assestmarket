package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val productId: String,
    val orderId: String,
    val downloadedAt: Long = System.currentTimeMillis(),
    val versionDownloaded: String
)
