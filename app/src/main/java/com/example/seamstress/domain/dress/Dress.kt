package com.example.seamstress.domain.dress

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Dress(
    val name: String,
    val type: String,
    val picture: ArrayList<Int>,
    val bluePrint: ArrayList<Int>,
    val creationDate: String,
    val dimensions: ArrayList<String>
) : Parcelable