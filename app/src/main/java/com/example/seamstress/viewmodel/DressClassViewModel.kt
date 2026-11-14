package com.example.seamstress.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.seamstress.data.DressClassesRepository
import com.example.seamstress.domain.SeamstressDataBase
import com.example.seamstress.domain.dress.DressClass
import com.example.seamstress.domain.dress.DressClassDao
import com.example.seamstress.domain.dress.DressClassItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class DressClassViewModel(application: Application) : AndroidViewModel(application) {

    private val dressClassesRepository: DressClassesRepository
    val getAllClasses: LiveData<List<DressClassItem>>

    init {
        val db = SeamstressDataBase.getDatabase(application)

        val dressClassDao: DressClassDao = db.dressClassDao()
        dressClassesRepository = DressClassesRepository(dressClassDao)
        getAllClasses = dressClassesRepository.getAllClasses
    }

    private val _classSaved = Channel<Long>(Channel.BUFFERED)
    val classSaved: Flow<Long> = _classSaved.receiveAsFlow()

    fun insertClass(name: String, metricIds: List<Long>) {
        viewModelScope.launch {
            val id = dressClassesRepository.insertClassWithMetrics(
                DressClass(id = 0L, name = name),
                metricIds
            )
            _classSaved.send(id)
        }
    }
}
