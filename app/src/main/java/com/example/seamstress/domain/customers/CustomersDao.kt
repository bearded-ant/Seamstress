package com.example.seamstress.domain.customers

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CustomersDao {

    @Query("select * from customers")
    fun getAllCustomers(): LiveData<List<Customers>>

    @Query("select * from customers where id= :id")
    suspend fun getCustomerById(id: Long): Customers

    @Insert
    suspend fun insertCustomer(customers: Customers): Long

    @Delete
    suspend fun deleteCustomer(customers: Customers)

    @Update
    suspend fun updateCustomer(customers: Customers)
}