package com.example.seamstress.data

import androidx.lifecycle.LiveData
import com.example.seamstress.domain.client.Clients
import com.example.seamstress.domain.client.ClientsDao

class SeamstressRepository(private val clientsDao: ClientsDao) {

    val getAll: LiveData<List<Clients>> = clientsDao.getAllClients()

    suspend fun selectById(id: Int): Clients = clientsDao.selectById(id)

    suspend fun insert(clients: Clients) {
        clientsDao.insert(clients)
    }

    suspend fun delete(clients: Clients) {
        clientsDao.delete(clients)
    }

    suspend fun update(clients: Clients) {
        clientsDao.update(clients)
    }

}