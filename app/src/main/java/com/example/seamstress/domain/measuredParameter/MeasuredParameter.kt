package com.example.seamstress.domain.measuredParameter

data class MeasuredParameter(
    var id: Long,
    var name: String,
    var shortName: String,
    var value: String?,
    var description: String?
)