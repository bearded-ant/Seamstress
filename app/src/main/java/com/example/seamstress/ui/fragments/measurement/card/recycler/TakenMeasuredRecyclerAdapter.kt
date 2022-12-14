package com.example.seamstress.ui.fragments.measurement.card.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.seamstress.R
import com.example.seamstress.domain.measured.taken.TakenMeasurement

class TakenMeasuredRecyclerAdapter(
    private val metrics: List<TakenMeasurement>
) : RecyclerView.Adapter<TakenMeasuredViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TakenMeasuredViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_list_measured_metrics, parent, false)
        return TakenMeasuredViewHolder(view)
    }

    override fun onBindViewHolder(holder: TakenMeasuredViewHolder, position: Int) {
        holder.binding(metrics[position])
    }

    override fun getItemCount(): Int = metrics.size
}