package com.example.seamstress.ui.clients.listFragment.recycler

import com.example.seamstress.domain.client.Client

interface ClientsClickListener {
    fun onClintCardClick(client: Client, position: Int)
}