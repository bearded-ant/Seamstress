package com.example.seamstress.domain.measured.measurements

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements")
data class Measurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    @ColumnInfo(name = "create_date")
    val createDate: Long,
    @ColumnInfo(name = "dress_type")
    val dressType: Long,
    @ColumnInfo(name = "customer_id")
    val customerId:Long
)