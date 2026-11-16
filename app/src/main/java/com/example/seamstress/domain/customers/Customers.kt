package com.example.seamstress.domain.customers

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Customers(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val phone: String?,
    /** Путь к файлу фото в хранилище приложения; null — фото нет. */
    val img: String?,
    val balance: Long = 0L
)