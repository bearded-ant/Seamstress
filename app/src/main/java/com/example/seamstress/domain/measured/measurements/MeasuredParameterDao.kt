package com.example.seamstress.domain.measured.measurements

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MeasuredParameterDao {

    @Query("select * from measurements where customer_id = :customerId")
    suspend fun getMeasurementsByCustomerId(customerId: Long): List<MeasuredParameter>

    @Update
    suspend fun updateMeasurements(measuredParameter: MeasuredParameter)

    @Insert
    suspend fun insertMeasurements(measuredParameter: MeasuredParameter)

    @Delete
    suspend fun deleteMeasurements(measuredParameter: MeasuredParameter)
}