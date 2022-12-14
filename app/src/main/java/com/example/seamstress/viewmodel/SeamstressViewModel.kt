package com.example.seamstress.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.seamstress.data.CustomersRepository
import com.example.seamstress.data.MeasurementsRepository
import com.example.seamstress.data.MetricsRepository
import com.example.seamstress.domain.SeamstressDataBase
import com.example.seamstress.domain.customers.Customers
import com.example.seamstress.domain.customers.CustomersDao
import com.example.seamstress.domain.measured.measurements.Measurement
import com.example.seamstress.domain.measured.measurements.MeasurementDao
import com.example.seamstress.domain.measured.metric.Metric
import com.example.seamstress.domain.measured.metric.MetricDao
import kotlinx.coroutines.launch

class SeamstressViewModel(application: Application) : AndroidViewModel(application) {

    val getAllCustomers: LiveData<List<Customers>>
    private val customerRepo: CustomersRepository

    val getAllMtrics: LiveData<List<Metric>>
    private val metricsRepo: MetricsRepository

    private val measurementsRepository: MeasurementsRepository

    init {
        val db = SeamstressDataBase.getDatabase(application)

        val customersDao: CustomersDao = db.customersDao()
        customerRepo = CustomersRepository(customersDao)
        getAllCustomers = customerRepo.getAllCustomers

        val metricDao: MetricDao = db.metricDao()
        metricsRepo = MetricsRepository(metricDao)
        getAllMtrics = metricsRepo.getAllMetrics

        val measurementDao: MeasurementDao = db.measurementsDao()
        measurementsRepository = MeasurementsRepository(measurementDao)
    }

    private var _selectByIdCustomerLiveData = MutableLiveData<Customers>()
    val selectByIdCustomerLiveData: LiveData<Customers> = _selectByIdCustomerLiveData
    fun selectCustomerById(id: Long) {
        viewModelScope.launch {
            val response = customerRepo.getCustomerById(id)
            _selectByIdCustomerLiveData.postValue(response)
        }
    }

    private var _newCustomerId = MutableLiveData<Long>()
    val newCustomerIdLiveData: LiveData<Long> = _newCustomerId
    fun insertCustomer(client: Customers) {
        viewModelScope.launch {
            val newEntityId = customerRepo.insertCustomer(client)
            _newCustomerId.postValue(newEntityId)
        }
    }

    fun deleteCustomer(client: Customers) {
        viewModelScope.launch {
            customerRepo.deleteCustomer(client)
        }
    }

    fun updateCustomer(client: Customers) {
        viewModelScope.launch {
            customerRepo.updateCustomer(client)
        }
    }

    private var _getByIdMetricLiveData = MutableLiveData<Metric>()
    val getByIdMetricLiveData: LiveData<Metric> = _getByIdMetricLiveData
    fun getMetricById(id: Long) {
        viewModelScope.launch {
            val response = metricsRepo.getMetricById(id)
            _getByIdMetricLiveData.postValue(response)
        }
    }

    private var _getByIdMeasurementLiveData = MutableLiveData<List<Measurement>>()
    val getByIdMeasurementLiveData: LiveData<List<Measurement>> = _getByIdMeasurementLiveData
    fun getMeasurementByCustomerId(id: Long) {
        viewModelScope.launch {
            val response = measurementsRepository.getMeasurementsByCustomerId(id)
            _getByIdMeasurementLiveData.postValue(response)
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
