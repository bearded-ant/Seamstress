package com.example.seamstress.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.seamstress.data.StandardSetsRepository
import com.example.seamstress.domain.SeamstressDataBase
import com.example.seamstress.domain.dress.DressClassDao
import com.example.seamstress.domain.dress.DressClassItem
import com.example.seamstress.domain.measured.metric.Metric
import com.example.seamstress.domain.measured.metric.MetricDao
import com.example.seamstress.domain.standards.StandardMetric
import com.example.seamstress.domain.standards.StandardMetricRow
import com.example.seamstress.domain.standards.StandardSet
import com.example.seamstress.domain.standards.StandardSetItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class StandardSetViewModel(application: Application) : AndroidViewModel(application) {

    private val standardSetsRepository: StandardSetsRepository
    private val dressClassDao: DressClassDao
    private val metricDao: MetricDao
    val allSets: LiveData<List<StandardSetItem>>
    val allClasses: LiveData<List<DressClassItem>>
    val allMetrics: LiveData<List<Metric>>

    init {
        val db = SeamstressDataBase.getDatabase(application)
        standardSetsRepository = StandardSetsRepository(db.standardSetDao())
        dressClassDao = db.dressClassDao()
        metricDao = db.metricDao()
        allSets = standardSetsRepository.getAllSets
        allClasses = dressClassDao.getAllClasses()
        allMetrics = metricDao.getAllMetrics()
    }

    private val _classMetricsLiveData = MutableLiveData<List<Metric>>()
    val classMetricsLiveData: LiveData<List<Metric>> = _classMetricsLiveData

    /** Метрики класса в порядке снятия — каркас формы набора. */
    fun loadClassMetrics(classId: Long) {
        viewModelScope.launch {
            val ids = dressClassDao.getMetricIds(classId)
            val byId = metricDao.getMetricsByIds(ids).associateBy { it.id }
            _classMetricsLiveData.value = ids.mapNotNull { byId[it] }
        }
    }

    fun setById(id: Long): LiveData<StandardSet?> = standardSetsRepository.getSetById(id)

    fun setMetrics(id: Long): LiveData<List<StandardMetricRow>> =
        standardSetsRepository.getSetMetrics(id)

    /** Значения именного набора для копирования в форму изделия. */
    suspend fun metricsForSet(setId: Long): List<StandardMetric> =
        standardSetsRepository.getSetMetricsOnce(setId)

    private val _setSaved = Channel<Long>(Channel.BUFFERED)
    val setSaved: Flow<Long> = _setSaved.receiveAsFlow()

    /** [values] — пары «id метрики → значение». */
    fun insertSet(name: String, values: List<Pair<Long, Float>>) {
        viewModelScope.launch {
            val metrics = values.map { (metricId, value) ->
                StandardMetric(id = 0L, setId = 0L, metricId = metricId, value = value)
            }
            _setSaved.send(
                standardSetsRepository.insertSetWithMetrics(StandardSet(id = 0L, name = name), metrics)
            )
        }
    }

    private val _setUpdated = Channel<Long>(Channel.BUFFERED)
    val setUpdated: Flow<Long> = _setUpdated.receiveAsFlow()

    fun updateSet(setId: Long, name: String, values: List<Pair<Long, Float>>) {
        viewModelScope.launch {
            val metrics = values.map { (metricId, value) ->
                StandardMetric(id = 0L, setId = setId, metricId = metricId, value = value)
            }
            standardSetsRepository.updateSetWithMetrics(StandardSet(id = setId, name = name), metrics)
            _setUpdated.send(setId)
        }
    }

    private val _setDeleted = Channel<Long>(Channel.BUFFERED)
    val setDeleted: Flow<Long> = _setDeleted.receiveAsFlow()

    fun deleteSet(setId: Long) {
        viewModelScope.launch {
            standardSetsRepository.deleteSetById(setId)
            _setDeleted.send(setId)
        }
    }
}
