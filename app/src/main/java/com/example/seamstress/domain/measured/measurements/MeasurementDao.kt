package com.example.seamstress.domain.measured.measurements

import androidx.room.*

@Dao
interface MeasurementDao {

    @Query("select * from measurements where customer_id = :customerId")
    suspend fun getMeasurementsByCustomerId(customerId: Long): List<Measurement>

    @Update
    suspend fun updateMeasurements(measurement: Measurement)

    @Insert
    suspend fun insertMeasurements(measurement: Measurement)

    @Delete
    suspend fun deleteMeasurements(measurement: Measurement)
}