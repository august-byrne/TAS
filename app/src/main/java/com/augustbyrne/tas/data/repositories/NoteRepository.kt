package com.augustbyrne.tas.data.repositories

import com.augustbyrne.tas.data.db.NoteDao
import com.augustbyrne.tas.data.db.entities.DataItem
import com.augustbyrne.tas.data.db.entities.NoteItem
import com.augustbyrne.tas.data.db.entities.NoteWithItems
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NoteRepository @Inject constructor(private val noteDao: NoteDao) {

    suspend fun upsert(item: NoteItem): Long = noteDao.upsert(item)
    suspend fun upsertData(items: List<DataItem>) = noteDao.upsertData(items)
    suspend fun updateNote(note: NoteItem) = noteDao.updateNote(note)
    suspend fun upsertDataItem(item: DataItem) = noteDao.upsertDataItem(item)
    suspend fun deleteNoteWithData(id: Int) = noteDao.deleteNoteAndData(id)
    suspend fun cascadeDeleteNote(id: Int) = noteDao.deleteNote(id)
    val allNotes: Flow<List<NoteItem>> = noteDao.getAllNotes()
    val allNotesWithItems: Flow<List<NoteWithItems>> = noteDao.getAllNotesWithItems()
    fun getNoteWithItemsById(noteId: Int): Flow<NoteWithItems> = noteDao.getNoteWithItems(noteId)

}
