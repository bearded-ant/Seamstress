package com.example.seamstress.domain.measured.taken

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "taken_measurements")
data class TakenMeasurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = "metric_id")
    val metricId:Long,
    @ColumnInfo(name = "measured_id")
    val measuredId:Long,
    @ColumnInfo(name= "customer_id")
    val customerId:Long,
    val value: Float
)
