package com.example.seamstress.domain.measured.metric

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface MetricDao {
    @Query("select * from metrics")
    fun getAllMetrics(): LiveData<List<Metric>>

    @Query("select * from metrics where id = :id")
    suspend fun getMetricById(id: Long): Metric
}