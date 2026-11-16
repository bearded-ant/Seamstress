package com.example.seamstress.domain.standards

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Значение метрики внутри именного набора; в изделие копируется, не ссылается. */
@Entity(
    tableName = "standard_metric",
    foreignKeys = [
        ForeignKey(
            entity = StandardSet::class,
            parentColumns = ["id"],
            childColumns = ["set_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("set_id"), Index("metric_id")]
)
data class StandardMetric(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = "set_id")
    val setId: Long,
    @ColumnInfo(name = "metric_id")
    val metricId: Long,
    val value: Float
)

/** Мерка набора с названием метрики — для формы набора. */
data class StandardMetricRow(
    @ColumnInfo(name = "metric_id")
    val metricId: Long,
    val value: Float,
    @ColumnInfo(name = "short_name")
    val shortName: String,
    @ColumnInfo(name = "metric_name")
    val metricName: String
)
