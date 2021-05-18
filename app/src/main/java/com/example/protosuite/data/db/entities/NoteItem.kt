package com.example.protosuite.data.db.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
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

@Entity(tableName = "data_table")
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