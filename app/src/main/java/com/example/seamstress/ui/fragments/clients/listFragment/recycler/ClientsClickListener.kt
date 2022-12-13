package com.example.seamstress.ui.fragments.clients.listFragment.recycler

import com.example.seamstress.domain.client.Clients

interface ClientsClickListener {
    fun onClintCardClick(clients: Clients, position: Int)
}