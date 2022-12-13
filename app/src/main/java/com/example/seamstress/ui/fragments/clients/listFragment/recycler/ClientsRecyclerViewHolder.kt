package com.example.seamstress.ui.fragments.clients.listFragment.recycler

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.seamstress.R
import com.example.seamstress.domain.client.Clients

class ClientsRecyclerViewHolder(
    itemView: View,
    private val cardClick: ClientsClickListener
) : RecyclerView.ViewHolder(itemView) {

    private val clientName = itemView.findViewById<TextView>(R.id.item_client_list_name)
    private val clientBalance = itemView.findViewById<TextView>(R.id.item_client_list_balance)
    private val clientPhone = itemView.findViewById<TextView>(R.id.item_client_list_phone)
    private val clientImg = itemView.findViewById<ImageView>(R.id.item_client_list_photo)
    private val clientCard = itemView.findViewById<CardView>(R.id.item_client_list_card)

    fun bindValues(clientsEntity: Clients) {
        clientName.text = clientsEntity.name
        clientBalance.text = clientsEntity.balance.toString()
        clientPhone.text = clientsEntity.phone
        clientImg.setImageResource(R.drawable.ic_baseline_add_a_photo_24)
        clientCard.setOnClickListener {
            cardClick.onClientCardClick(clientsEntity.id)
        }
    }
}