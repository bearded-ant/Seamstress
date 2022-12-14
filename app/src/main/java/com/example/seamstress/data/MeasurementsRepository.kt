package com.example.seamstress.data

import com.example.seamstress.domain.measured.measurements.Measurement
import com.example.seamstress.domain.measured.measurements.MeasurementDao

class MeasurementsRepository(private val measurementDao: MeasurementDao) {

    suspend fun getMeasurementsByCustomerId(customerId: Long): List<Measurement> =
        measurementDao.getMeasurementsByCustomerId(customerId)

    suspend fun updateMeasurement(measurement: Measurement) {
        measurementDao.updateMeasurements(measurement)
    }

    suspend fun insertMeasurement(measurement: Measurement) {
        measurementDao.insertMeasurements(measurement)
    }

    suspend fun deleteMeasurement(measurement: Measurement) {
        measurementDao.deleteMeasurements(measurement)
    }
}