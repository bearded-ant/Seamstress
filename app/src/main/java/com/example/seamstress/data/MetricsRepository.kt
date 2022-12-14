package com.example.seamstress.data

import androidx.lifecycle.LiveData
import com.example.seamstress.domain.measured.metric.Metric
import com.example.seamstress.domain.measured.metric.MetricDao

class MetricsRepository(private val metricDao: MetricDao) {
    val getAllMetrics: LiveData<List<Metric>> = metricDao.getAllMetrics()
    suspend fun getMetricById(metricId: Long): Metric = metricDao.getMetricById(metricId)
}