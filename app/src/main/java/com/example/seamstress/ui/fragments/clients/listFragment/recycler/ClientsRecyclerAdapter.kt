package com.example.seamstress.ui.fragments.clients.listFragment.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.seamstress.R
import com.example.seamstress.domain.client.Clients
import com.example.seamstress.ui.fragments.clients.listFragment.ClientListFragmentDirections
import com.example.seamstress.ui.fragments.clients.mainFragment.ClientFragmentDirections

class ClientsRecyclerAdapter(
    private val clients: List<Clients>,
    private val cardClick: ClientsClickListener
) : RecyclerView.Adapter<ClientsRecyclerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientsRecyclerViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_client_list, parent, false)
        return ClientsRecyclerViewHolder(view, cardClick)
    }

    override fun onBindViewHolder(holder: ClientsRecyclerViewHolder, position: Int) {
        holder.bindValues(clients[position])
    }

    override fun getItemCount(): Int = clients.size

}