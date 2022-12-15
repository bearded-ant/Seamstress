package com.example.seamstress.ui.fragments.measurement.add

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.seamstress.R
import com.example.seamstress.domain.measured.metric.Metric

class MetricRecyclerAdapter(
    private val metrics: List<Metric>
) : RecyclerView.Adapter<MetricViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetricViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_metrics_list, parent, false)
        return MetricViewHolder(view)
    }

    override fun onBindViewHolder(holder: MetricViewHolder, position: Int) {
        holder.binding(metrics[position])
    }

    override fun getItemCount(): Int = metrics.size
}