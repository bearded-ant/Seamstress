package com.example.seamstress.domain.measured.metric

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "metrics")
data class Metric(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = "type_id")
    val typeId: Long,
    @ColumnInfo(name = "short_name")
    val shortName: String,
    val name: String,
    val description: String?
)
