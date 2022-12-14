package com.example.seamstress.domain.customers

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Customers(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val phone: String?,
    val img: Int,
    val balance: Float = 0F
)