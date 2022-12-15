package com.example.seamstress.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.seamstress.data.TakenMeasurementsRepository
import com.example.seamstress.domain.SeamstressDataBase
import com.example.seamstress.domain.measured.taken.TakenMeasurement
import com.example.seamstress.domain.measured.taken.TakenMeasurementDao
import kotlinx.coroutines.launch

class TakenMeasurementsViewModel(application: Application) : AndroidViewModel(application) {

    private val taketnRepository: TakenMeasurementsRepository

    init {
        val db = SeamstressDataBase.getDatabase(application)

        val takenMeasurementDao: TakenMeasurementDao = db.takenMeasurementsDao()
        taketnRepository = TakenMeasurementsRepository(takenMeasurementDao)
    }

    private var _getTakenMesLiveData = MutableLiveData<List<TakenMeasurement>>()
    val measurementLiveData: LiveData<List<TakenMeasurement>> = _getTakenMesLiveData

    fun getMeasurementByCustomerId(measurementId: Long) {
        viewModelScope.launch {
            val response = taketnRepository.getTakenMeasById(measurementId)
            _getTakenMesLiveData.postValue(response)
        }
    }
}
