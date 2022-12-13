package com.example.seamstress.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.seamstress.data.SeamstressRepository
import com.example.seamstress.domain.SeamstressDataBase
import com.example.seamstress.domain.client.Clients
import com.example.seamstress.domain.client.ClientsDao
import kotlinx.coroutines.launch

class SeamstressViewModel(application: Application) : AndroidViewModel(application) {

    val getAllClients: LiveData<List<Clients>>
    private val repository: SeamstressRepository

    init {
        val clientsDao: ClientsDao = SeamstressDataBase.getDatabase(application).clientsDao()
        repository = SeamstressRepository(clientsDao)
        getAllClients = repository.getAll
    }

    private var _selectByIdClientLiveData = MutableLiveData<Clients>()
    val selectByIdClientLiveData: LiveData<Clients> = _selectByIdClientLiveData

    fun selectClientById(id: Int) {
        viewModelScope.launch {
            val response = repository.selectById(id)
            _selectByIdClientLiveData.postValue(response)
        }
    }

    fun insert(client: Clients) {
        viewModelScope.launch {
            repository.insert(client)
        }
    }

    fun delete(client: Clients) {
        viewModelScope.launch {
            repository.delete(client)
        }
    }

    fun update(client: Clients) {
        viewModelScope.launch {
            repository.update(client)
        }
    }
}
