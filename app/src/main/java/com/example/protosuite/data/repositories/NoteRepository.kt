package com.example.protosuite.data.repositories

import com.example.protosuite.data.db.NoteDao
import com.example.protosuite.data.db.entities.DataItem
import com.example.protosuite.data.db.entities.NoteItem
import com.example.protosuite.data.db.entities.NoteWithItems
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NoteRepository @Inject constructor(private val noteDao: NoteDao) {

    suspend fun upsert(item: NoteItem): Long = noteDao.upsert(item)
    suspend fun upsertData(items: List<DataItem>) = noteDao.upsertData(items)
    @Deprecated("Replaced with deleteNote(Int): Unit using cascading deletion in sql",ReplaceWith("deleteNote(id)"))
    suspend fun deleteNoteWithData(id: Int) = noteDao.deleteNoteAndData(id)
    suspend fun deleteNote(id: Int) = noteDao.deleteNote(id)
    val allNotes: Flow<List<NoteItem>> = noteDao.getAllNotes()
    fun getNoteWithItemsById(noteId: Int): Flow<NoteWithItems> = noteDao.getNoteWithItems(noteId)
    suspend fun updateNoteItems(noteItems: List<NoteItem>) = noteDao.updateNoteItems(noteItems)

}
