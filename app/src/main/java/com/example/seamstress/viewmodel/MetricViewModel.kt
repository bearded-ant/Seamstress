package com.example.seamstress.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.seamstress.data.MetricsRepository
import com.example.seamstress.domain.SeamstressDataBase
import com.example.seamstress.domain.measured.metric.Metric
import com.example.seamstress.domain.measured.metric.MetricDao
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
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

    private val _metricSaved = Channel<Long>(Channel.BUFFERED)
    val metricSaved: Flow<Long> = _metricSaved.receiveAsFlow()

    fun insertMetric(shortName: String, name: String, description: String?) {
        viewModelScope.launch {
            val id = metricsRepo.insertMetric(
                Metric(
                    id = 0L,
                    typeId = 1L,
                    shortName = shortName,
                    name = name,
                    description = description
                )
            )
            _metricSaved.send(id)
        }
    }
}
