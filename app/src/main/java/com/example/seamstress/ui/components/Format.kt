package com.example.seamstress.ui.components

/** 92.0f → «92», 92.5f → «92.5». */
fun formatMeasure(value: Float): String =
    if (value == value.toLong().toFloat()) value.toLong().toString() else value.toString()
