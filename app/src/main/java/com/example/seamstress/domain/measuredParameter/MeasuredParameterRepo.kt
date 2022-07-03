package com.example.seamstress.domain.measuredParameter

interface MeasuredParameterRepo {
    fun create(parameter: MeasuredParameter)
    fun getParameters(): List<MeasuredParameter>
    fun delete(parameter: MeasuredParameter)
}