package com.example.seamstress.domain.measured.metric

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MetricDao {
    @Query("select * from metrics")
    fun getAllMetrics(): LiveData<List<Metric>>

    /** Для карточки изделия: выбор новой мерки — разовое чтение, без подписки. */
    @Query("select * from metrics order by id")
    suspend fun getAllMetricsOnce(): List<Metric>

    @Query("select * from metrics where id = :id")
    suspend fun getMetricById(id: Long): Metric?

    @Query("select * from metrics where id in (:ids)")
    suspend fun getMetricsByIds(ids: List<Long>): List<Metric>

    @Insert
    suspend fun insertMetric(metric: Metric): Long
}
