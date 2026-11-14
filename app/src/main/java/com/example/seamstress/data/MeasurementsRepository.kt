package com.example.seamstress.data

import androidx.lifecycle.LiveData
import com.example.seamstress.domain.measured.measurements.Measurement
import com.example.seamstress.domain.measured.measurements.MeasurementDao
import com.example.seamstress.domain.measured.measurements.MeasurementRow
import com.example.seamstress.domain.measured.taken.TakenMeasurement
import com.example.seamstress.domain.measured.taken.TakenMeasurementDao
import com.example.seamstress.domain.measured.taken.TakenMetricRow

class MeasurementsRepository(
    private val measurementDao: MeasurementDao,
    private val takenMeasurementDao: TakenMeasurementDao
) {

    fun getMeasurementsByCustomerId(customerId: Long): LiveData<List<MeasurementRow>> =
        measurementDao.getMeasurementsByCustomerId(customerId)

    fun getMeasurementById(id: Long): LiveData<MeasurementRow?> =
        measurementDao.getMeasurementById(id)

    fun getTakenMeasurements(measurementId: Long): LiveData<List<TakenMetricRow>> =
        takenMeasurementDao.getTakenMeasurement(measurementId)

    suspend fun insertMeasurementWithTaken(
        measurement: Measurement,
        taken: List<TakenMeasurement>
    ): Long = measurementDao.insertMeasurementWithTaken(measurement, taken)

    suspend fun updateMeasurementWithTaken(
        measurement: Measurement,
        taken: List<TakenMeasurement>
    ) = measurementDao.updateMeasurementWithTaken(measurement, taken)
}
