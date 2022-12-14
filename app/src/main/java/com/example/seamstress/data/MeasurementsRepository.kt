package com.example.seamstress.data

import com.example.seamstress.domain.measured.measurements.MeasuredParameter
import com.example.seamstress.domain.measured.measurements.MeasuredParameterDao

class MeasurementsRepository(private val measuredParameterDao: MeasuredParameterDao) {

    suspend fun getMeasurementsByCustomerId(customerId: Long): List<MeasuredParameter> =
        measuredParameterDao.getMeasurementsByCustomerId(customerId)

    suspend fun updateMeasurement(measuredParameter: MeasuredParameter) {
        measuredParameterDao.updateMeasurements(measuredParameter)
    }

    suspend fun insertMeasurement(measuredParameter: MeasuredParameter) {
        measuredParameterDao.insertMeasurements(measuredParameter)
    }

    suspend fun deleteMeasurement(measuredParameter: MeasuredParameter) {
        measuredParameterDao.deleteMeasurements(measuredParameter)
    }
}