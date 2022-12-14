package com.example.seamstress.data

import androidx.lifecycle.LiveData
import com.example.seamstress.domain.customers.Customers
import com.example.seamstress.domain.customers.CustomersDao

class SeamstressRepository(private val customersDao: CustomersDao) {

    val getAll: LiveData<List<Customers>> = customersDao.getAllClients()

    suspend fun selectById(id: Long): Customers = customersDao.selectById(id)

    suspend fun insert(customers: Customers): Long = customersDao.insert(customers)

    suspend fun delete(customers: Customers) {
        customersDao.delete(customers)
    }

    suspend fun update(customers: Customers) {
        customersDao.update(customers)
    }

}