package com.example.seamstress.domain.client

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Clients(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val phone: String?,
    val img: Int,
    val balance: Float = 0F
)