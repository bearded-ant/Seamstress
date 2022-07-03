package com.example.seamstress.ui.clients.listFragment.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.seamstress.R
import com.example.seamstress.domain.client.Client

class ClientsRecyclerAdapter (val listener: ClientsClickListener): RecyclerView.Adapter<ClientsRecyclerViewHolder>() {
    private var clientsData: ArrayList<Client> = arrayListOf()

    fun getData(clientsData:ArrayList<Client>) {
        this.clientsData = clientsData
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientsRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_client_list, parent, false)
        return ClientsRecyclerViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: ClientsRecyclerViewHolder, position: Int) {
        holder.bindValues(clientsData[position])
    }

    override fun getItemCount(): Int = clientsData.size

}