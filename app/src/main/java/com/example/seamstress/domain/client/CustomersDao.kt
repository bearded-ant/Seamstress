package com.example.seamstress.domain.client

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CustomersDao {

    @Query("select * from customers")
    fun getAllClients(): LiveData<List<Customers>>

    @Query("select * from customers where id= :id")
    suspend fun selectById(id: Long): Customers

    @Insert
    suspend fun insert(customers: Customers): Long

    @Delete
    suspend fun delete(customers: Customers)

    @Update
    suspend fun update(customers: Customers)
}