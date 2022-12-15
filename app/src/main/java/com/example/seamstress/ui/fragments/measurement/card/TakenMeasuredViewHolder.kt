package com.example.seamstress.ui.fragments.measurement.card

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.seamstress.R
import com.example.seamstress.domain.measured.metric.Metric
import com.example.seamstress.domain.measured.taken.TakenMeasurement
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView

class TakenMeasuredViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val textLayout =
        itemView.findViewById<TextInputLayout>(R.id.item_list_measured_metric_text_layout)
    private val textView =
        itemView.findViewById<MaterialTextView>(R.id.item_list_measured_metric_text_view)

    fun binding(taken: TakenMeasurement) {
        textLayout.helperText = taken.metricId.toString()
        textView.text = taken.value.toString()
    }
}