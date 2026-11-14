package com.example.seamstress.domain.dress

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface DressClassDao {

    @Query(
        """
        select c.id, c.name,
               (select count(*) from dress_class_metrics cm where cm.class_id = c.id) as metrics_count
        from dress_classes c
        order by c.name
        """
    )
    fun getAllClasses(): LiveData<List<DressClassItem>>

    @Query("select * from dress_classes where id = :id")
    suspend fun getClassById(id: Long): DressClass?

    @Query("select metric_id from dress_class_metrics where class_id = :classId order by position")
    suspend fun getMetricIds(classId: Long): List<Long>

    @Insert
    suspend fun insertClass(dressClass: DressClass): Long

    @Insert
    suspend fun insertClassMetrics(rows: List<DressClassMetric>)

    @Transaction
    suspend fun insertClassWithMetrics(dressClass: DressClass, metricIds: List<Long>): Long {
        val classId = insertClass(dressClass)
        insertClassMetrics(
            metricIds.mapIndexed { index, metricId ->
                DressClassMetric(classId = classId, metricId = metricId, position = index)
            }
        )
        return classId
    }
}
