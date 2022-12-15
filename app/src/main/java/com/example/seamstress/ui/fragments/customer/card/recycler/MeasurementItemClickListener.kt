package com.example.seamstress.ui.fragments.customer.card.recycler

import com.example.seamstress.domain.measured.measurements.Measurement

interface MeasurementItemClickListener {
    fun onMeasureItemClick(measurement: Measurement)
}