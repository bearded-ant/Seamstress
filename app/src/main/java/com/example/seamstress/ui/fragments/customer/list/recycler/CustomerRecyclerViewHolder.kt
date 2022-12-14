package com.example.seamstress.ui.fragments.customer.list.recycler

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.seamstress.R
import com.example.seamstress.domain.client.Customers

class CustomerRecyclerViewHolder(
    itemView: View,
    private val cardClick: CustomerClickListener
) : RecyclerView.ViewHolder(itemView) {

    private val customerName = itemView.findViewById<TextView>(R.id.item_customer_list_name)
    private val customerBalance = itemView.findViewById<TextView>(R.id.item_customer_list_balance)
    private val customerPhone = itemView.findViewById<TextView>(R.id.item_customer_list_phone)
    private val customerImg = itemView.findViewById<ImageView>(R.id.item_customer_list_photo)
    private val customerCard = itemView.findViewById<CardView>(R.id.item_customer_list_card)

    fun bindValues(customersEntity: Customers) {
        customerName.text = customersEntity.name
        customerBalance.text = customersEntity.balance.toString()
        customerPhone.text = customersEntity.phone
        customerImg.setImageResource(R.drawable.ic_baseline_add_a_photo_24)
        customerCard.setOnClickListener {
            cardClick.onCustomerCardClick(customersEntity.id)
        }
    }
}