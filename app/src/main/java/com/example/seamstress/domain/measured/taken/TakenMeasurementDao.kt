package com.example.seamstress.domain.measured.taken

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface TakenMeasurementDao {

    @Query("select * from taken_measurements where measured_id = :measuredId")
    suspend fun getTakenMeasurement(measuredId: Long): List<TakenMeasurement>

    //    @Insert
//    fun insertTakenMeasurement(taken: List<TakenMeasurement>)
//
//    @Delete
//    fun deleteTakenMeasurement(takenMeasurement: TakenMeasurement)
//
//    @Update
//    fun updateTakenMeasurement(takenMeasurement: TakenMeasurement)
}