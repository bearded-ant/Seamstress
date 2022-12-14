package com.example.seamstress.data

import androidx.lifecycle.LiveData
import com.example.seamstress.domain.customers.Customers
import com.example.seamstress.domain.customers.CustomersDao

class CustomersRepository(private val customersDao: CustomersDao) {

    val getAllCustomers: LiveData<List<Customers>> = customersDao.getAllCustomers()
    suspend fun getCustomerById(id: Long): Customers = customersDao.getCustomerById(id)
    suspend fun insertCustomer(customers: Customers): Long = customersDao.insertCustomer(customers)

    suspend fun deleteCustomer(customers: Customers) {
        customersDao.deleteCustomer(customers)
    }

    suspend fun updateCustomer(customers: Customers) {
        customersDao.updateCustomer(customers)
    }

}