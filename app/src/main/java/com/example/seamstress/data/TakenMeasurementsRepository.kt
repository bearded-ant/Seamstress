package com.example.seamstress.data

import com.example.seamstress.domain.measured.taken.TakenMeasurement
import com.example.seamstress.domain.measured.taken.TakenMeasurementDao

class TakenMeasurementsRepository(private val takenMeasurementDao: TakenMeasurementDao) {

    suspend fun getTakenMeasById(measuredId: Long): List<TakenMeasurement> =
        takenMeasurementDao.getTakenMeasurement(measuredId)

}