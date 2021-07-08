package com.example.protosuite.ui.notes

import android.content.Context
import android.os.CountDownTimer
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.protosuite.data.db.entities.DataItem
import com.example.protosuite.data.db.entities.NoteItem
import com.example.protosuite.data.db.entities.NoteWithItems
import com.example.protosuite.data.repositories.NoteRepository
import com.example.protosuite.ui.timer.PrefUtil
import com.example.protosuite.ui.timer.TimerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repo: NoteRepository
): ViewModel() {
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
                    dataItems.replaceAll { dataItem ->
                        dataItem.copy(
                            id = dataItem.id,
                            parent_id = noteDataId.toInt(),
                            activity = dataItem.activity,
                            order = dataItems.lastIndex - dataItems.indexOf(dataItem),
                            time = dataItem.time
                        )
                    }
                repo.upsertData(dataItems)
            } else {
                repo.upsert(noteItem)
                repo.upsertData(dataItems)
            }
        }

    fun deleteNote(id: Int) = CoroutineScope(Dispatchers.Main).launch {
        repo.deleteNoteWithData(id)
    }

/*
    fun updateNoteItems(items: List<NoteItem>) = CoroutineScope(Dispatchers.Main).launch {
        repo.updateNoteItems(items)
    }
 */

    fun updateNoteItemOrderInDatabase(noteListCopy: MutableList<NoteItem>) =
        CoroutineScope(Dispatchers.Main).launch {
                noteListCopy.replaceAll { noteItem ->
                    noteItem.copy(
                        id = noteItem.id,
                        creation_date = noteItem.creation_date,
                        last_edited_on = noteItem.last_edited_on,
                        order = noteListCopy.lastIndex - noteListCopy.indexOf(noteItem),
                        title = noteItem.title,
                        description = noteItem.description
                    )
                }
                repo.updateNoteItems(noteListCopy)
            /*
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
                repo.updateNoteItems(reorderedCopy)
            */
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

    private lateinit var timer: CountDownTimer

    @ExperimentalAnimationApi
    fun startTimer(timerLength: Long,context: Context) {
        timer = object: CountDownTimer(timerLength*1000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                setTimerLength(millisUntilFinished/1000)
            }
            override fun onFinish() {
                setTimerState(TimerState.Stopped, context)
                //_timerState.value = TimerState.Stopped
                setTimerLength(PrefUtil.getPreviousTimerLengthSeconds(context))
                //set the length of the timer to be the one set in SettingsActivity
                //if the length was changed when the timer was running
                //myViewModel.setTimerLength(PrefUtil.getPreviousTimerLengthSeconds())

                //binding.progressBar.progress = 0

                //PrefUtil.setSecondsRemaining(timerLengthSeconds, requireContext())
                //secondsRemaining = timerLengthSeconds
            }
        }.start()
    }

    @ExperimentalAnimationApi
    fun stopTimer() {
        timer.cancel()
    }

    // LiveData holds state which is observed by the UI
    // (state flows down from ViewModel)
    private var _timerLength = MutableLiveData(0L)
    val timerLength: LiveData<Long> = _timerLength

    // setTimerLength is an event we're defining that the UI can invoke
    // (events flow up from UI)
    fun setTimerLength(timerLength: Long) {
        if (timerLength >= 0) {
            _timerLength.value = timerLength
        } else {
            _timerLength.value = 0
        }
    }

    // LiveData holds state which is observed by the UI
    // (state flows down from ViewModel)
    private var _timerState = MutableLiveData(TimerState.Stopped)
    val timerState: LiveData<TimerState> = _timerState

    // setTimerLength is an event we're defining that the UI can invoke
    // (events flow up from UI)
    @ExperimentalAnimationApi
    fun setTimerState(timerState: TimerState, context: Context) {
        _timerState.value = timerState
        PrefUtil.setTimerState(timerState,context)
    }

    // onNameChange is an event we're defining that the UI can invoke
    // (events flow up from UI)
    //fun onNameChange(newName: String) {
        //_name.value = newName
    //}
}
