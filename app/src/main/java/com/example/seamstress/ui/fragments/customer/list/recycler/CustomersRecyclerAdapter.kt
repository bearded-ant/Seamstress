package com.example.seamstress.ui.fragments.customer.list.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.seamstress.R
import com.example.seamstress.domain.customers.Customers

class CustomersRecyclerAdapter(
    private val clients: List<Customers>,
    private val cardClick: CustomerClickListener
) : RecyclerView.Adapter<CustomerRecyclerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerRecyclerViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_customer_list, parent, false)
        return CustomerRecyclerViewHolder(view, cardClick)
    }

    override fun onBindViewHolder(holder: CustomerRecyclerViewHolder, position: Int) {
        holder.bindValues(clients[position])
    }

    override fun getItemCount(): Int = clients.size

}