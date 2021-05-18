package com.example.protosuite.data.repositories

import androidx.lifecycle.LiveData
import com.example.protosuite.data.db.NoteDao
import com.example.protosuite.data.db.entities.DataItem
import com.example.protosuite.data.db.entities.NoteItem
import com.example.protosuite.data.db.entities.NoteWithItems

class NoteRepository (private val noteDao: NoteDao) {
    suspend fun upsert(item: NoteItem): Long = noteDao.upsert(item)
    suspend fun upsertData(items: List<DataItem>) = noteDao.upsertData(items)
    suspend fun delete(item: NoteItem) = noteDao.delete(item)
    suspend fun deleteNoteWithData(id: Int) = noteDao.deleteNoteAndData(id)
    val allNotes: LiveData<List<NoteItem>> = noteDao.getAllNotes()
    fun getNoteWithItemsById(noteId: Int): LiveData<NoteWithItems> = noteDao.getNoteWithItems(noteId)   //will this work? yeeeees hehehehehehehehehehashdfhasjdfh
    fun getNoteById(noteId: Int): LiveData<NoteItem> = noteDao.getNoteWithId(noteId)
    suspend fun updateNoteItems(noteItems: List<NoteItem>) = noteDao.updateNoteItems(noteItems)

}
