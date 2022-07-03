package com.example.seamstress.domain.measuredParameter

import com.example.seamstress.domain.measuredParameter.MeasuredParameter

class SizesSource {
    var measuredParameters: ArrayList<MeasuredParameter> = arrayListOf()

    fun bindSources() {
        measuredParameters.add(0, MeasuredParameter(0,	"высота проймы сзади",	"Впр","",""))
        measuredParameters.add(0, MeasuredParameter(0,	"длина талии по спинке",	"Дтс","",""))
        measuredParameters.add(0, MeasuredParameter(0,	"длина талии по переду",	"Дтп","",""))
    }
}