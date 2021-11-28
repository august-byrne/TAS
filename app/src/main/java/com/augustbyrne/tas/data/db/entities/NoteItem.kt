package com.augustbyrne.tas.data.db.entities

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import java.util.*

@Entity(tableName = "note_table")
data class NoteItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val creation_date: Calendar?,
    val last_edited_on: Calendar?,
    val order: Int,
    val title: String,
    val description: String
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
    val parent_id: Int,
    val order: Int,
    val activity: String,
    val time: Int,
    val unit: Int
)

data class NoteWithItems(
    @Embedded val note: NoteItem,
    @Relation(
        parentColumn = "id",
        entityColumn = "parent_id"
    )
    val dataItems: List<DataItem>
)