package com.example.seamstress.ui.fragments.customer.card.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.seamstress.R
import com.example.seamstress.domain.measured.measurements.Measurement

class MeasurementRecyclerAdapter(
    private val measurement: List<Measurement>
) :
    RecyclerView.Adapter<MeasurementViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeasurementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_measurement, parent, false)
        return MeasurementViewHolder(view)
    }

    override fun onBindViewHolder(holder: MeasurementViewHolder, position: Int) {
        holder.bindingCardValues(measurement[position])
    }

    override fun getItemCount(): Int = measurement.size
}