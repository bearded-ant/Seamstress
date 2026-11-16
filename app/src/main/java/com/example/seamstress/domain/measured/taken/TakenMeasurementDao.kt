package com.example.seamstress.domain.measured.taken

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TakenMeasurementDao {

    @Query(
        """
        select t.metric_id as metric_id, t.value, m.short_name as short_name, m.name as metric_name,
               coalesce(cm.position, 1000) as position
        from taken_measurements t
        join metrics m on m.id = t.metric_id
        join measurements ms on ms.id = t.measured_id
        left join dress_class_metrics cm on cm.metric_id = t.metric_id and cm.class_id = ms.class_id
        where t.measured_id = :measuredId
        order by position, t.id
        """
    )
    fun getTakenMeasurement(measuredId: Long): LiveData<List<TakenMetricRow>>

    /** Разовое чтение мерок замера — для копирования значений в форму изделия. */
    @Query(
        """
        select t.metric_id as metric_id, t.value, m.short_name as short_name, m.name as metric_name,
               coalesce(cm.position, 1000) as position
        from taken_measurements t
        join metrics m on m.id = t.metric_id
        join measurements ms on ms.id = t.measured_id
        left join dress_class_metrics cm on cm.metric_id = t.metric_id and cm.class_id = ms.class_id
        where t.measured_id = :measuredId
        order by position, t.id
        """
    )
    suspend fun getTakenMeasurementOnce(measuredId: Long): List<TakenMetricRow>

    @Insert
    suspend fun insertTakenMeasurements(taken: List<TakenMeasurement>): List<Long>
}

/** Снятая мерка с названием метрики — для карточки замера. */
data class TakenMetricRow(
    @ColumnInfo(name = "metric_id")
    val metricId: Long,
    val value: Float,
    @ColumnInfo(name = "short_name")
    val shortName: String,
    @ColumnInfo(name = "metric_name")
    val metricName: String,
    val position: Int
)
