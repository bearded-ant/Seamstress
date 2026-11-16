package com.example.seamstress.domain.customers

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CustomersDao {

    @Query("select * from customers")
    fun getAllCustomers(): LiveData<List<Customers>>

    @Query("select * from customers where id= :id")
    suspend fun getCustomerById(id: Long): Customers?

    @Query("select * from customers where id = :id")
    fun getCustomerByIdLiveData(id: Long): LiveData<Customers?>

    @Insert
    suspend fun insertCustomer(customers: Customers): Long

    @Delete
    suspend fun deleteCustomer(customers: Customers)

    @Query("delete from taken_measurements where customer_id = :customerId")
    suspend fun deleteTakenMeasurementsByCustomer(customerId: Long)

    @Query("delete from measurements where customer_id = :customerId")
    suspend fun deleteMeasurementsByCustomer(customerId: Long)

    /** Клиент удаляется вместе со своими замерами и снятыми мерками — без висячих строк. */
    @Transaction
    open suspend fun deleteCustomerWithData(customers: Customers) {
        deleteTakenMeasurementsByCustomer(customers.id)
        deleteMeasurementsByCustomer(customers.id)
        deleteCustomer(customers)
    }

    @Update
    suspend fun updateCustomer(customers: Customers)
}