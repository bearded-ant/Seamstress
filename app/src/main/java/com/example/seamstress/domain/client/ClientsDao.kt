package com.example.seamstress.domain.client

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ClientsDao {

    @Query("select * from clients")
    fun getAllClients(): LiveData<List<Clients>>

    @Query("select * from clients where id= :id")
    suspend fun selectById(id: Int): Clients

    @Insert
    suspend fun insert(clients: Clients)

    @Delete
    suspend fun delete(clients: Clients)

    @Update
    suspend fun update(clients: Clients)

//    fun getClients(): List<Client>
//    fun getBalance(client: Client): Float?
//    fun setBalance(client: Client)
}