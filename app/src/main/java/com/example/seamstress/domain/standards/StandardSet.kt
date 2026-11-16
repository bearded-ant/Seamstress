package com.example.seamstress.domain.standards

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Именной набор общих мерок («мужчина 54», «женщина 48»…). */
@Entity(tableName = "standard_set")
data class StandardSet(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String
)

/** Набор со счётчиком мерок — для списка справочника. */
data class StandardSetItem(
    val id: Long,
    val name: String,
    @ColumnInfo(name = "metrics_count")
    val metricsCount: Int
)
