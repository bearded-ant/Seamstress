package com.example.seamstress.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.seamstress.data.CustomersRepository
import com.example.seamstress.domain.SeamstressDataBase
import com.example.seamstress.domain.customers.Customers
import com.example.seamstress.domain.customers.CustomersDao
import kotlinx.coroutines.launch

class SeamstressViewModel(application: Application) : AndroidViewModel(application) {

    val getAllCustomers: LiveData<List<Customers>>
    private val customerRepo: CustomersRepository

    init {
        val db = SeamstressDataBase.getDatabase(application)

        val customersDao: CustomersDao = db.customersDao()
        customerRepo = CustomersRepository(customersDao)
        getAllCustomers = customerRepo.getAllCustomers
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
}
