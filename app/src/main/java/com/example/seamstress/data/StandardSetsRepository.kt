package com.example.seamstress.data

import androidx.lifecycle.LiveData
import com.example.seamstress.domain.standards.StandardMetric
import com.example.seamstress.domain.standards.StandardMetricRow
import com.example.seamstress.domain.standards.StandardSet
import com.example.seamstress.domain.standards.StandardSetDao
import com.example.seamstress.domain.standards.StandardSetItem

class StandardSetsRepository(private val standardSetDao: StandardSetDao) {

    val getAllSets: LiveData<List<StandardSetItem>> = standardSetDao.getAllSets()

    fun getSetById(id: Long): LiveData<StandardSet?> = standardSetDao.getSetById(id)

    fun getSetMetrics(setId: Long): LiveData<List<StandardMetricRow>> =
        standardSetDao.getSetMetrics(setId)

    suspend fun getSetMetricsOnce(setId: Long): List<StandardMetric> =
        standardSetDao.getSetMetricsOnce(setId)

    suspend fun insertSetWithMetrics(set: StandardSet, metrics: List<StandardMetric>): Long =
        standardSetDao.insertSetWithMetrics(set, metrics)

    suspend fun updateSetWithMetrics(set: StandardSet, metrics: List<StandardMetric>) =
        standardSetDao.updateSetWithMetrics(set, metrics)

    suspend fun deleteSetById(id: Long) = standardSetDao.deleteSetById(id)
}
