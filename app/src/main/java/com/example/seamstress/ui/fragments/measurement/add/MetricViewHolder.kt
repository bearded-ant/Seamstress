package com.example.seamstress.ui.fragments.measurement.add

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.seamstress.R
import com.example.seamstress.domain.measured.metric.Metric
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MetricViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val textLayout =
        itemView.findViewById<TextInputLayout>(R.id.item_metric_list_text_layout)
    private val inputText =
        itemView.findViewById<TextInputEditText>(R.id.item_metric_list_edit_text)

    fun binding(metric: Metric) {
        textLayout.hint = metric.shortName
        textLayout.helperText = metric.name
    }
}