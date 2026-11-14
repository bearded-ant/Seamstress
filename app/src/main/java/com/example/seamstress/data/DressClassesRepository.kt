package com.example.seamstress.data

import androidx.lifecycle.LiveData
import com.example.seamstress.domain.dress.DressClass
import com.example.seamstress.domain.dress.DressClassDao
import com.example.seamstress.domain.dress.DressClassItem

class DressClassesRepository(private val dressClassDao: DressClassDao) {

    val getAllClasses: LiveData<List<DressClassItem>> = dressClassDao.getAllClasses()

    suspend fun getClassById(id: Long): DressClass? = dressClassDao.getClassById(id)

    suspend fun getMetricIds(classId: Long): List<Long> = dressClassDao.getMetricIds(classId)

    suspend fun insertClassWithMetrics(dressClass: DressClass, metricIds: List<Long>): Long =
        dressClassDao.insertClassWithMetrics(dressClass, metricIds)
}
