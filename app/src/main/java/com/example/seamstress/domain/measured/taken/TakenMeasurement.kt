package com.example.seamstress.domain.measured.taken

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.seamstress.domain.customers.Customers
import com.example.seamstress.domain.measured.measurements.Measurement
import com.example.seamstress.domain.measured.metric.Metric

@Entity(
    tableName = "taken_measurements",
    foreignKeys = [
        ForeignKey(
            entity = Measurement::class,
            parentColumns = ["id"],
            childColumns = ["measured_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Metric::class,
            parentColumns = ["id"],
            childColumns = ["metric_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Customers::class,
            parentColumns = ["id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("measured_id"), Index("metric_id"), Index("customer_id")]
)
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
