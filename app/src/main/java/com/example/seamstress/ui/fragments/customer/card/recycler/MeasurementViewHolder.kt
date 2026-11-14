package com.example.seamstress.ui.fragments.customer.card.recycler

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.seamstress.R
import com.example.seamstress.domain.measured.measurements.MeasurementRow
import com.google.android.material.card.MaterialCardView

class MeasurementViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
    private val card = itemView.findViewById<MaterialCardView>(R.id.item_list_measurement_card)
    private val date = itemView.findViewById<TextView>(R.id.item_list_measurement_date)
    private val type = itemView.findViewById<TextView>(R.id.item_list_measurement_type)
    private val name = itemView.findViewById<TextView>(R.id.item_list_measurement_name)

    fun bindingCardValues(measurement: MeasurementRow, click:MeasurementItemClickListener) {
        date.text = measurement.dateCreation
        type.text = measurement.className
        name.text = measurement.name
        card.setOnClickListener {
            click.onMeasureItemClick(measurement)
        }
    }
}