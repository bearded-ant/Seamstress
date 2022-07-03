package com.example.seamstress.domain.measuredParameter

data class MeasuredParameter(
    var id: Int,
    var name: String,
    var abbreviation: String,
    var value: String?,
    var comment: String?
)