package com.augustbyrne.tas.data.db.entities

import androidx.room.*
import androidx.room.ForeignKey.Companion.CASCADE
import java.time.LocalDateTime

@Entity(tableName = "note_table")
data class NoteItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val creation_date: LocalDateTime? = null,
    val last_edited_on: LocalDateTime? = null,
    val order: Int = 0,
    val title: String = "",
    val description: String = ""
)

@Entity(
    tableName = "data_table",
    foreignKeys = [ForeignKey(
        entity = NoteItem::class,
        parentColumns = ["id"],
        childColumns = ["parent_id"],
        onDelete = CASCADE
    )],
    indices = [Index("parent_id")]
)
data class DataItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val parent_id: Int = 0,
    val order: Int = 0,
    val activity: String = "",
    val time: Int = 0,
    val unit: Int = 0
)

data class NoteWithItems(
    @Embedded val note: NoteItem,
    @Relation(
        parentColumn = "id",
        entityColumn = "parent_id"
    )
    val dataItems: List<DataItem>
)