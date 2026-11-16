package com.example.seamstress.domain.measured.measurements

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.seamstress.domain.customers.Customers

@Entity(
    tableName = "measurements",
    foreignKeys = [
        ForeignKey(
            entity = Customers::class,
            parentColumns = ["id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customer_id")]
)
data class Measurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    @ColumnInfo(name = "date_creation")
    val dateCreation: String,
    @ColumnInfo(name = "class_id")
    val classId: Long,
    @ColumnInfo(name = "customer_id")
    val customerId: Long
)

/** Замер вместе с названием класса изделия — для списков и карточки. */
data class MeasurementRow(
    val id: Long,
    val name: String,
    @ColumnInfo(name = "date_creation")
    val dateCreation: String,
    @ColumnInfo(name = "class_id")
    val classId: Long,
    @ColumnInfo(name = "class_name")
    val className: String,
    @ColumnInfo(name = "customer_id")
    val customerId: Long
)
