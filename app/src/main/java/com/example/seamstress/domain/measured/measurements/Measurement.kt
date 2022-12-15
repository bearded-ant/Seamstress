package com.example.seamstress.domain.measured.measurements

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements")
data class Measurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    @ColumnInfo(name = "date_creation")
    val dateCreation: String,
    @ColumnInfo(name = "dress_type")
    val dressType: String,
    @ColumnInfo(name = "customer_id")
    val customerId:Long
)