package com.example.seamstress.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.seamstress.data.MeasurementsRepository
import com.example.seamstress.domain.SeamstressDataBase
import com.example.seamstress.domain.dress.DressClassDao
import com.example.seamstress.domain.dress.DressClassItem
import com.example.seamstress.domain.measured.measurements.Measurement
import com.example.seamstress.domain.measured.measurements.MeasurementDao
import com.example.seamstress.domain.measured.measurements.MeasurementRow
import com.example.seamstress.domain.measured.metric.Metric
import com.example.seamstress.domain.measured.metric.MetricDao
import com.example.seamstress.domain.measured.taken.TakenMeasurement
import com.example.seamstress.domain.measured.taken.TakenMeasurementDao
import com.example.seamstress.domain.measured.taken.TakenMetricRow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class MeasurementsViewModel(application: Application) : AndroidViewModel(application) {

    private val measurementsRepository: MeasurementsRepository
    private val dressClassDao: DressClassDao
    private val metricDao: MetricDao

    val allClasses: LiveData<List<DressClassItem>>
    val allMetrics: LiveData<List<Metric>>

    init {
        val db = SeamstressDataBase.getDatabase(application)

        val measurementDao: MeasurementDao = db.measurementsDao()
        val takenMeasurementDao: TakenMeasurementDao = db.takenMeasurementsDao()
        measurementsRepository = MeasurementsRepository(measurementDao, takenMeasurementDao)
        dressClassDao = db.dressClassDao()
        metricDao = db.metricDao()
        allClasses = dressClassDao.getAllClasses()
        allMetrics = metricDao.getAllMetrics()
    }

    private val _classMetricsLiveData = MutableLiveData<List<Metric>>()
    val classMetricsLiveData: LiveData<List<Metric>> = _classMetricsLiveData

    /** Метрики класса в порядке снятия — из них строятся поля формы замера. */
    fun loadClassMetrics(classId: Long) {
        viewModelScope.launch {
            val ids = dressClassDao.getMetricIds(classId)
            val byId = metricDao.getMetricsByIds(ids).associateBy { it.id }
            _classMetricsLiveData.postValue(ids.mapNotNull { byId[it] })
        }
    }

    /** Замеры клиента из Room: список обновляется сам после записи. */
    fun measurementsByCustomer(customerId: Long): LiveData<List<MeasurementRow>> =
        measurementsRepository.getMeasurementsByCustomerId(customerId)

    /** Карточка замера: замер и его мерки приходят из Room и обновляются после правки. */
    fun measurementById(id: Long): LiveData<MeasurementRow?> =
        measurementsRepository.getMeasurementById(id)

    fun takenMeasurements(id: Long): LiveData<List<TakenMetricRow>> =
        measurementsRepository.getTakenMeasurements(id)

    private val _measurementSaved = Channel<Long>(Channel.BUFFERED)
    val measurementSaved: Flow<Long> = _measurementSaved.receiveAsFlow()

    /**
     * Сохраняет замер и снятые мерки одной транзакцией.
     * [values] — пары «id метрики → значение» для заполненных полей.
     */
    fun addMeasurement(
        customerId: Long,
        name: String,
        classId: Long,
        dateCreation: String,
        values: List<Pair<Long, Float>>
    ) {
        viewModelScope.launch {
            val measurement = Measurement(
                id = 0L,
                name = name,
                dateCreation = dateCreation,
                classId = classId,
                customerId = customerId
            )
            val taken = values.map { (metricId, value) ->
                TakenMeasurement(
                    id = 0L,
                    metricId = metricId,
                    measuredId = 0L,
                    customerId = customerId,
                    value = value
                )
            }
            _measurementSaved.send(
                measurementsRepository.insertMeasurementWithTaken(measurement, taken)
            )
        }
    }

    private val _measurementUpdated = Channel<Long>(Channel.BUFFERED)
    val measurementUpdated: Flow<Long> = _measurementUpdated.receiveAsFlow()

    fun updateMeasurement(
        measurementId: Long,
        customerId: Long,
        name: String,
        classId: Long,
        dateCreation: String,
        values: List<Pair<Long, Float>>
    ) {
        viewModelScope.launch {
            val measurement = Measurement(
                id = measurementId,
                name = name,
                dateCreation = dateCreation,
                classId = classId,
                customerId = customerId
            )
            val taken = values.map { (metricId, value) ->
                TakenMeasurement(
                    id = 0L,
                    metricId = metricId,
                    measuredId = measurementId,
                    customerId = customerId,
                    value = value
                )
            }
            measurementsRepository.updateMeasurementWithTaken(measurement, taken)
            _measurementUpdated.send(measurementId)
        }
    }
}
