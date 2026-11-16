package com.example.seamstress.domain.standards

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface StandardSetDao {

    @Query(
        """
        select s.id, s.name,
               (select count(*) from standard_metric sm where sm.set_id = s.id) as metrics_count
        from standard_set s
        order by s.name
        """
    )
    fun getAllSets(): LiveData<List<StandardSetItem>>

    @Query("select * from standard_set where id = :id")
    fun getSetById(id: Long): LiveData<StandardSet?>

    @Query(
        """
        select sm.metric_id as metric_id, sm.value, m.short_name as short_name, m.name as metric_name
        from standard_metric sm
        join metrics m on m.id = sm.metric_id
        where sm.set_id = :setId
        order by sm.id
        """
    )
    fun getSetMetrics(setId: Long): LiveData<List<StandardMetricRow>>

    /** Значения набора для копирования в форму изделия. */
    @Query("select * from standard_metric where set_id = :setId order by id")
    suspend fun getSetMetricsOnce(setId: Long): List<StandardMetric>

    @Insert
    suspend fun insertSet(set: StandardSet): Long

    @Insert
    suspend fun insertSetMetrics(metrics: List<StandardMetric>): List<Long>

    /** Набор и его значения пишутся одной транзакцией. */
    @Transaction
    suspend fun insertSetWithMetrics(set: StandardSet, metrics: List<StandardMetric>): Long {
        val setId = insertSet(set)
        if (metrics.isNotEmpty()) {
            insertSetMetrics(metrics.map { it.copy(setId = setId) })
        }
        return setId
    }

    @Update
    suspend fun updateSet(set: StandardSet)

    @Query("delete from standard_metric where set_id = :setId")
    suspend fun deleteSetMetrics(setId: Long)

    /** Правка набора: имя обновляется, значения заменяются целиком. */
    @Transaction
    suspend fun updateSetWithMetrics(set: StandardSet, metrics: List<StandardMetric>) {
        updateSet(set)
        deleteSetMetrics(set.id)
        if (metrics.isNotEmpty()) {
            insertSetMetrics(metrics.map { it.copy(setId = set.id) })
        }
    }

    /** Набор удаляется вместе со значениями — ON DELETE CASCADE. */
    @Query("delete from standard_set where id = :id")
    suspend fun deleteSetById(id: Long)
}
