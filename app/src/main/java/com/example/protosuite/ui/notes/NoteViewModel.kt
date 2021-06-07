package com.example.protosuite.ui.notes

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.protosuite.data.db.entities.DataItem
import com.example.protosuite.data.db.entities.NoteItem
import com.example.protosuite.data.db.entities.NoteWithItems
import com.example.protosuite.data.repositories.NoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class NoteViewModel (private val repo: NoteRepository): ViewModel() {
    /*
    var noteDataId: Long = 0
    fun upsert(item: NoteItem) = CoroutineScope(Dispatchers.Main).launch {
        noteDataId = repo.upsert(item)
    }
    fun upsertData(items: List<DataItem>) = CoroutineScope(Dispatchers.Main).launch {
        repo.upsertData(items)
    }
    */
    fun upsertNoteAndData(noteItem: NoteItem, dataItems: MutableList<DataItem>) =
        CoroutineScope(Dispatchers.Main).launch {
            if (noteItem.id == 0) {
                val noteDataId = repo.upsert(noteItem)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    dataItems.replaceAll { dataItem ->
                        dataItem.copy(
                            id = dataItem.id,
                            parent_id = noteDataId.toInt(),
                            activity = dataItem.activity,
                            order = dataItems.lastIndex - dataItems.indexOf(dataItem),
                            time = dataItem.time
                        )
                    }
                }
                repo.upsertData(dataItems)
            } else {
                repo.upsert(noteItem)
                repo.upsertData(dataItems)
            }
        }

    //private val tasksEventChannel = Channel<TasksEvent> {  }
    //val tasksEvent = tasksEventChannel.receiveAsFlow()
    //private val tasksFlow = combine(

    //)
    //val tasks = tasksFlow.asLiveData()

    fun deleteNote(id: Int) = CoroutineScope(Dispatchers.Main).launch {
        repo.deleteNoteWithData(id)
        //tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(task))
    }

    //sealed class TasksEvent {
    //    data class ShowUndoDeleteTaskMessage(val task: Task): TasksEvent()
    //}

    fun updateNoteItems(items: List<NoteItem>) = CoroutineScope(Dispatchers.Main).launch {
        repo.updateNoteItems(items)
    }

    fun updateNoteItemOrderInDatabase(noteListCopy: MutableList<NoteItem>) =
        CoroutineScope(Dispatchers.Main).launch {
            val reorderedCopy: MutableList<NoteItem> = mutableListOf()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                reorderedCopy.addAll(noteListCopy)
                reorderedCopy.replaceAll { noteItem ->
                    noteItem.copy(
                        id = noteItem.id,
                        creation_date = noteItem.creation_date,
                        last_edited_on = noteItem.last_edited_on,
                        order = reorderedCopy.lastIndex - reorderedCopy.indexOf(noteItem),
                        title = noteItem.title,
                        description = noteItem.description
                    )
                }
            } else {              //TODO: Make a for loop version for lower android apis::: Done, currently UNTESTED
                for (i in 0..noteListCopy.lastIndex) {
                    if (noteListCopy[i].order != noteListCopy.lastIndex - i) {
                        reorderedCopy.add(
                            noteListCopy[i].copy(
                                id = noteListCopy[i].id,
                                creation_date = noteListCopy[i].creation_date,
                                last_edited_on = noteListCopy[i].last_edited_on,
                                order = noteListCopy.lastIndex - i,
                                title = noteListCopy[i].title,
                                description = noteListCopy[i].description
                            )
                        )
                    } else {
                        reorderedCopy.add(noteListCopy[i])
                    }
                }
            }
            repo.updateNoteItems(reorderedCopy)
        }

    var allNotes: LiveData<List<NoteItem>> = repo.allNotes

    fun getNoteWithItemsById(id: Int): LiveData<NoteWithItems> = repo.getNoteWithItemsById(id)

    private var _newLaunch: Boolean = true
    val newLaunch
        get() = _newLaunch

    fun newLaunchComplete(){
        _newLaunch = false
    }

    private var _noteListSize: Int = 0
    val noteListSize
        get() = _noteListSize

    fun setNoteListSize(input: Int) {
        _noteListSize = input
    }

}