package com.example.seamstress.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.seamstress.data.MeasurementsRepository
import com.example.seamstress.domain.SeamstressDataBase
import com.example.seamstress.domain.measured.measurements.Measurement
import com.example.seamstress.domain.measured.measurements.MeasurementDao
import kotlinx.coroutines.launch

class MeasurementsViewModel(application: Application) : AndroidViewModel(application) {

    private val measurementsRepository: MeasurementsRepository

    init {
        val db = SeamstressDataBase.getDatabase(application)

        val measurementDao: MeasurementDao = db.measurementsDao()
        measurementsRepository = MeasurementsRepository(measurementDao)
    }

    private var _getMeasurementLiveDataByCustomerId = MutableLiveData<List<Measurement>>()
    val measurementLiveData: LiveData<List<Measurement>> = _getMeasurementLiveDataByCustomerId

    fun getMeasurementByCustomerId(id: Long) {
        viewModelScope.launch {
            val response = measurementsRepository.getMeasurementsByCustomerId(id)
            _getMeasurementLiveDataByCustomerId.postValue(response)
        }
    }

    fun deleteMeasurement(measurement: Measurement) {
        viewModelScope.launch {
            measurementsRepository.deleteMeasurement(measurement)
        }
    }

    fun updateMeasurement(measurement: Measurement) {
        viewModelScope.launch {
            measurementsRepository.updateMeasurement(measurement)
        }
    }

    fun insertMeasurement(measurement: Measurement) {
        viewModelScope.launch {
            measurementsRepository.insertMeasurement(measurement)
        }
    }
}
