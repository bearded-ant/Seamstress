package com.example.seamstress.domain.measured.measurements

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.seamstress.domain.measured.taken.TakenMeasurement

@Dao
abstract class MeasurementDao {

    @Query(
        """
        select m.id, m.name, m.date_creation, m.class_id, c.name as class_name, m.customer_id
        from measurements m
        left join dress_classes c on c.id = m.class_id
        where m.customer_id = :customerId
        order by m.id desc
        """
    )
    abstract suspend fun getMeasurementsByCustomerId(customerId: Long): List<MeasurementRow>

    @Query(
        """
        select m.id, m.name, m.date_creation, m.class_id, c.name as class_name, m.customer_id
        from measurements m
        left join dress_classes c on c.id = m.class_id
        where m.id = :id
        """
    )
    abstract suspend fun getMeasurementById(id: Long): MeasurementRow?

    @Insert
    abstract suspend fun insertMeasurements(measurement: Measurement): Long

    @Insert
    abstract suspend fun insertTakenMeasurements(taken: List<TakenMeasurement>): List<Long>

    @Update
    abstract suspend fun updateMeasurements(measurement: Measurement)

    @Delete
    abstract suspend fun deleteMeasurements(measurement: Measurement)

    /** Замер и его мерки пишутся одной транзакцией: id замера нужен для мерок. */
    @Transaction
    open suspend fun insertMeasurementWithTaken(
        measurement: Measurement,
        taken: List<TakenMeasurement>
    ): Long {
        val measurementId = insertMeasurements(measurement)
        if (taken.isNotEmpty()) {
            insertTakenMeasurements(taken.map { it.copy(measuredId = measurementId) })
        }
        return measurementId
    }
}
