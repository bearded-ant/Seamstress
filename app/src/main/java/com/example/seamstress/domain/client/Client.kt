package com.example.seamstress.domain.client

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Client(
    val id: Int,
    val name: String,
    val phone: String?,
    val img: Int,
    val balance: Float = 0F
) : Parcelable