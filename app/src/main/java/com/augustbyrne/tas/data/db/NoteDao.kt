package com.augustbyrne.tas.data.db

import androidx.room.*
import com.augustbyrne.tas.data.db.entities.DataItem
import com.augustbyrne.tas.data.db.entities.NoteItem
import com.augustbyrne.tas.data.db.entities.NoteWithItems
import kotlinx.coroutines.flow.Flow


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

    @Update
    suspend fun updateNote(note: NoteItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDataItem(item: DataItem): Long

    @Query("DELETE FROM note_table WHERE id = :key")
    suspend fun deleteNote(key: Int)

    @Query("DELETE FROM data_table WHERE id = :key")
    suspend fun deleteData(key: Int)

/*    @Transaction
    suspend fun deleteNoteAndData(key: Int) {
        deleteNote(key)
        deleteData(key)
    }*/

    @Query("SELECT * FROM note_table ORDER BY `creation_date` DESC")
    fun getAllNotes(): Flow<List<NoteItem>>

    @Transaction
    @Query("SELECT * FROM note_table ORDER BY `creation_date` DESC")
    fun getAllNotesWithItems(): Flow<List<NoteWithItems>>

    @Transaction
    @Query("SELECT * FROM note_table WHERE id = :key ORDER BY `id` DESC")
    fun getNoteWithItems(key: Int): Flow<NoteWithItems>

}