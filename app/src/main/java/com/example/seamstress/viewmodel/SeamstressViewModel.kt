package com.example.seamstress.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.seamstress.data.SeamstressRepository
import com.example.seamstress.domain.SeamstressDataBase
import com.example.seamstress.domain.customers.Customers
import com.example.seamstress.domain.customers.CustomersDao
import kotlinx.coroutines.launch

class SeamstressViewModel(application: Application) : AndroidViewModel(application) {

    val getAllCustomers: LiveData<List<Customers>>
    private val repository: SeamstressRepository

    init {
        val customersDao: CustomersDao = SeamstressDataBase.getDatabase(application).customersDao()
        repository = SeamstressRepository(customersDao)
        getAllCustomers = repository.getAll
    }

    private var _selectByIdCustomerLiveData = MutableLiveData<Customers>()
    val selectByIdCustomerLiveData: LiveData<Customers> = _selectByIdCustomerLiveData

    fun selectCustomerById(id: Long) {
        viewModelScope.launch {
            val response = repository.selectById(id)
            _selectByIdCustomerLiveData.postValue(response)
        }
    }


    private var _newId = MutableLiveData<Long>()
    val newIdLiveData: LiveData<Long> = _newId
    fun insert(client: Customers) {
        viewModelScope.launch {
            val newEntityId = repository.insert(client)
            _newId.postValue(newEntityId)
        }
    }

    fun delete(client: Customers) {
        viewModelScope.launch {
            repository.delete(client)
        }
    }

    fun update(client: Customers) {
        viewModelScope.launch {
            repository.update(client)
        }
    }
}
