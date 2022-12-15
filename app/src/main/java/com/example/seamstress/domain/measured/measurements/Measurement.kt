package com.example.seamstress.domain.measured.measurements

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "measurements")
data class Measurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    @ColumnInfo(name = "date_creation")
    val dateCreation: String,
    @ColumnInfo(name = "dress_type")
    val dressType: String,
    @ColumnInfo(name = "customer_id")
    val customerId:Long
):Parcelable