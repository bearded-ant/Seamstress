package com.example.seamstress.domain.dresses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Фото изделия: путь к файлу в хранилище приложения, не drawable. */
@Entity(
    tableName = "dress_photo",
    foreignKeys = [
        ForeignKey(
            entity = Dress::class,
            parentColumns = ["id"],
            childColumns = ["dress_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dress_id")]
)
data class DressPhoto(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = "dress_id")
    val dressId: Long,
    val path: String
)
