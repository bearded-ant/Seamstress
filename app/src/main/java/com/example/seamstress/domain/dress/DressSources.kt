package com.example.seamstress.domain.dress


import com.example.seamstress.R
import java.sql.Timestamp
import java.time.LocalDateTime

class DressSources {

    private val dressList: ArrayList<Dress> = arrayListOf()
    val picture: ArrayList<Int> = arrayListOf(R.drawable.pants, R.drawable.shifon)
    val bluePrint: ArrayList<Int> = arrayListOf(R.drawable.bp1, R.drawable.bp2,R.drawable.bp3)
    val dimensions: ArrayList<String> = arrayListOf("100", "12", "67")
    val date = LocalDateTime.now().toString()

    fun bindDress(): ArrayList<Dress> {

        dressList.add(
            Dress("pants", "поясное", picture, bluePrint, date, dimensions))

            return dressList
    }
}