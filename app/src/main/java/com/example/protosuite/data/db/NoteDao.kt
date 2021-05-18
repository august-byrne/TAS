package com.example.protosuite.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.protosuite.data.db.entities.DataItem
import com.example.protosuite.data.db.entities.NoteItem
import com.example.protosuite.data.db.entities.NoteWithItems


@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: NoteItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertData(items: List<DataItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(note: NoteItem, data: List<DataItem>)

    @Update
    suspend fun updateNoteItems(noteItems: List<NoteItem>)

    @Delete
    suspend fun delete(item: NoteItem)

    @Query("DELETE FROM note_table WHERE id = :key")
    suspend fun deleteNote(key: Int)

    @Query("DELETE FROM data_table WHERE parent_id = :key")
    suspend fun deleteData(key: Int)

    @Transaction
    suspend fun deleteNoteAndData(key: Int){
        deleteNote(key)
        deleteData(key)
    }


    @Query("SELECT * FROM note_table ORDER BY `order` DESC")
    fun getAllNotes(): LiveData<List<NoteItem>>

    @Transaction
    @Query("SELECT * FROM note_table WHERE id = :key ORDER BY `id` DESC")
    fun getNoteWithItems(key: Int): LiveData<NoteWithItems>

    /**
     * Selects and returns the note with given noteId.
     */
    @Query("SELECT * from note_table WHERE id = :key")
    fun getNoteWithId(key: Int): LiveData<NoteItem>

    @Query("SELECT * from note_table WHERE id = :key")
    fun getNoteWithIdSyn(key: Int): NoteItem

}