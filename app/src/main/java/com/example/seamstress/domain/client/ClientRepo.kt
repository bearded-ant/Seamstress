package com.example.seamstress.domain.client

interface ClientRepo {
    fun getClients(): List<Client>
    fun getBalance(client: Client): Float?
    fun setBalance(client: Client)
}