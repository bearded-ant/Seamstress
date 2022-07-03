package com.example.seamstress.ui.clients.listFragment.recycler

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.seamstress.R
import com.example.seamstress.domain.client.Client

class ClientsRecyclerViewHolder(
    itemView: View,
    private val listener: ClientsClickListener
) : RecyclerView.ViewHolder(itemView) {

    private val clientName = itemView.findViewById<TextView>(R.id.item_client_list_name)
    private val clientBalance = itemView.findViewById<TextView>(R.id.item_client_list_balance)
    private val clientPhone = itemView.findViewById<TextView>(R.id.item_client_list_phone)
    private val clientImg = itemView.findViewById<ImageView>(R.id.item_client_list_photo)
    private val clientCard = itemView.findViewById<CardView>(R.id.item_client_list_card)

    fun bindValues(clientEntity: Client) {
        clientName.text = clientEntity.name
        clientBalance.text = clientEntity.balance.toString()
        clientPhone.text = clientEntity.phone
        clientImg.setImageResource(clientEntity.img)

        clientCard.setOnClickListener {
            listener.onClintCardClick(clientEntity, adapterPosition)
        }
    }
}