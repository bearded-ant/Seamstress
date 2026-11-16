package com.example.seamstress.domain.dresses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Значение мерки изделия — всегда копия, ссылкой на замер не привязана. */
@Entity(
    tableName = "dress_metric",
    foreignKeys = [
        ForeignKey(
            entity = Dress::class,
            parentColumns = ["id"],
            childColumns = ["dress_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dress_id"), Index("metric_id")]
)
data class DressMetric(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = "dress_id")
    val dressId: Long,
    @ColumnInfo(name = "metric_id")
    val metricId: Long,
    val value: Float
)

/** Мерка изделия с названием метрики — для карточки изделия. */
data class DressMetricRow(
    @ColumnInfo(name = "metric_id")
    val metricId: Long,
    val value: Float,
    @ColumnInfo(name = "short_name")
    val shortName: String,
    @ColumnInfo(name = "metric_name")
    val metricName: String,
    val position: Int
)
