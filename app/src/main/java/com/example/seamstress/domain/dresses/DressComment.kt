package com.example.seamstress.domain.dresses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Комментарий к изделию. */
@Entity(
    tableName = "dress_comment",
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
data class DressComment(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = "dress_id")
    val dressId: Long,
    val text: String,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    /** Порядок внутри изделия: закреплённые показываются первыми, затем по position. */
    val position: Int = 0,
    val pinned: Boolean = false
)
