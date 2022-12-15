package com.example.seamstress.ui.fragments.measurement.add

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.seamstress.R
import com.example.seamstress.domain.measured.metric.Metric

class MeasurementRecyclerAdapter(
    private val metrics: List<Metric>
) : RecyclerView.Adapter<MeasurementRecyclerHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeasurementRecyclerHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_metrics_list, parent, false)
        return MeasurementRecyclerHolder(view)
    }

    override fun onBindViewHolder(holder: MeasurementRecyclerHolder, position: Int) {
        holder.binding(metrics[position])
    }

    override fun getItemCount(): Int = metrics.size
}