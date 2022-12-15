package com.example.seamstress.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.seamstress.data.CustomersRepository
import com.example.seamstress.data.MetricsRepository
import com.example.seamstress.domain.SeamstressDataBase
import com.example.seamstress.domain.customers.Customers
import com.example.seamstress.domain.customers.CustomersDao
import com.example.seamstress.domain.measured.metric.Metric
import com.example.seamstress.domain.measured.metric.MetricDao
import kotlinx.coroutines.launch

class MetricViewModel(application: Application) : AndroidViewModel(application) {

    val getAllMetrics: LiveData<List<Metric>>
    private val metricsRepo: MetricsRepository

    init {
        val db = SeamstressDataBase.getDatabase(application)

        val metricDao: MetricDao = db.metricDao()
        metricsRepo = MetricsRepository(metricDao)
        getAllMetrics = metricsRepo.getAllMetrics
    }

    private var _getByIdMetricLiveData = MutableLiveData<Metric>()
    val getByIdMetricLiveData: LiveData<Metric> = _getByIdMetricLiveData
    fun getMetricById(id: Long) {
        viewModelScope.launch {
            val response = metricsRepo.getMetricById(id)
            _getByIdMetricLiveData.postValue(response)
        }
    }
}
