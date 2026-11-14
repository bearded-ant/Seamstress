package com.example.seamstress.domain.dress

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Класс изделия: поясное, плечевое, нижнее бельё и т.п. */
@Entity(tableName = "dress_classes")
data class DressClass(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String
)

/** Метрика класса изделия с порядком снятия. */
@Entity(tableName = "dress_class_metrics", primaryKeys = ["class_id", "metric_id"])
data class DressClassMetric(
    @ColumnInfo(name = "class_id")
    val classId: Long,
    @ColumnInfo(name = "metric_id")
    val metricId: Long,
    val position: Int
)

/** Класс изделия со счётчиком метрик — для списка справочника. */
data class DressClassItem(
    val id: Long,
    val name: String,
    @ColumnInfo(name = "metrics_count")
    val metricsCount: Int
)
