package com.example.seamstress.domain.dress

data class Dress(
    val name: String,
    val type: String,
    val picture: ArrayList<Int>,
    val bluePrint: ArrayList<Int>,
    val creationDate: String,
    val dimensions: ArrayList<String>
)
