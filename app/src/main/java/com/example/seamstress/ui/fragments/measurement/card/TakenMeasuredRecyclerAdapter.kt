package com.example.seamstress.ui.fragments.measurement.card

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
            LayoutInflater.from(parent.context).inflate(R.layout.item_metrics_list, parent, false)
        return TakenMeasuredViewHolder(view)
    }

    override fun onBindViewHolder(holder: TakenMeasuredViewHolder, position: Int) {
        holder.binding(metrics[position])
    }

    override fun getItemCount(): Int = metrics.size
}