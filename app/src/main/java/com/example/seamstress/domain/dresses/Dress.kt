package com.example.seamstress.domain.dresses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.seamstress.domain.customers.Customers

/**
 * Изделие клиента. Мерки хранятся копией значений в [DressMetric], источник копии не помним.
 * Удаление клиента убирает его изделия каскадом.
 */
@Entity(
    tableName = "dress",
    foreignKeys = [
        ForeignKey(
            entity = Customers::class,
            parentColumns = ["id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customer_id"), Index("class_id")]
)
data class Dress(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    @ColumnInfo(name = "class_id")
    val classId: Long,
    @ColumnInfo(name = "customer_id")
    val customerId: Long,
    /** Цена в целых рублях, может быть не задана. */
    val price: Long?,
    @ColumnInfo(name = "created_at")
    val createdAt: String
)

/** Изделие вместе с названием класса — для списков и карточки. */
data class DressRow(
    val id: Long,
    val name: String,
    @ColumnInfo(name = "class_id")
    val classId: Long,
    @ColumnInfo(name = "class_name")
    val className: String?,
    @ColumnInfo(name = "customer_id")
    val customerId: Long,
    val price: Long?,
    @ColumnInfo(name = "created_at")
    val createdAt: String
)
