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
    suspend fun updateNotes(items: List<NoteItem>) = noteDao.updateNotes(items)
    suspend fun updateNote(note: NoteItem) = noteDao.updateNote(note)
    suspend fun upsertDataItem(item: DataItem) = noteDao.upsertDataItem(item)
    suspend fun cascadeDeleteNote(id: Int) = noteDao.deleteNote(id)
    suspend fun deleteDataItem(id: Int) = noteDao.deleteData(id)
    val allNotesWithItems: Flow<List<NoteWithItems>> = noteDao.getAllNotesWithItems()
    val allNotes: Flow<List<NoteItem>> = noteDao.getAllNotes()
    fun getNoteWithItemsById(noteId: Int): Flow<NoteWithItems> = noteDao.getNoteWithItems(noteId)
    suspend fun getNoteWithItemsByIdSynchronous(noteId: Int): NoteWithItems = noteDao.getNoteWithItemsSync(noteId)
    suspend fun getNumberOfNotes(): Int = noteDao.getNumberOfNotes()
}
