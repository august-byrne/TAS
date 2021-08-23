package com.example.protosuite.ui.notes

import android.os.CountDownTimer
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.protosuite.data.db.entities.DataItem
import com.example.protosuite.data.db.entities.NoteItem
import com.example.protosuite.data.db.entities.NoteWithItems
import com.example.protosuite.data.repositories.NoteRepository
import com.example.protosuite.ui.timer.TimerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import javax.inject.Inject
import kotlin.math.pow

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repo: NoteRepository
): ViewModel() {

    var noteDataId: Long = 0
    fun upsert(item: NoteItem?) = CoroutineScope(Dispatchers.Main).launch {
        if (item != null) {
            noteDataId = repo.upsert(item)
        } else {
            Log.d("DB Interactions", "upsert failed: item was null")
        }
    }

    fun upsertData(items: List<DataItem>) = CoroutineScope(Dispatchers.Main).launch {
        repo.upsertData(items)
    }

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
        repo.deleteNote(id)
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

    var allNotes: LiveData<List<NoteItem>> = repo.allNotes.asLiveData()

    //var noteListFlow: Flow<List<NoteItem>> = repo.allNotes
    var recompCounter: Int = 0

    fun getNoteWithItemsById(id: Int): LiveData<NoteWithItems> =
        repo.getNoteWithItemsById(id).asLiveData()

    var beginTyping by mutableStateOf(false)
    var currentNote by mutableStateOf(NoteItem(0, null, null, 0, "", ""))
    var currentNoteItems = mutableStateListOf<DataItem>()
    var tempSavedNote: NoteWithItems? = null

    var openSortPopup by mutableStateOf(false)
    var sortType by mutableStateOf(0)

    val simpleDateFormat: DateFormat = SimpleDateFormat.getDateInstance()

    var noteListScrollIndex = 0
    var noteListScrollOffset = 0

    private var timer: CountDownTimer? = null

    //takes care of all time unit (and some timer state) manipulation
    fun startTimer(items: List<DataItem>, itemIndex: Int) {
        setActiveItemIndex(itemIndex)
        val activeItem = items[itemIndex]
        var activeTimeLengthMilli = activeItem.time.times(1000L) * 60F.pow(activeItem.unit).toLong()
        setTotalTimerLengthMilli(activeTimeLengthMilli)
        if (isPaused) {
            isPaused = false
            activeTimeLengthMilli = tempSavedTimerLengthMilli
        }
        setTimerLength(activeTimeLengthMilli)
        setTimerState(TimerState.Running)
        timer = object : CountDownTimer(activeTimeLengthMilli, 10L) {
            override fun onTick(millisUntilFinished: Long) {
                // important ticking for ui, separate from total time
                //timerLengthMilli = millisUntilFinished
                //Log.d("theTime", "Timer - tick -----------------")
                setTimerLength(millisUntilFinished)
            }

            override fun onFinish() {
                if (itemIndex < items.lastIndex) {
                    incrementItemIndex()
                    startTimer(items, itemIndex.inc())
                } else {
                    setTimerState(TimerState.Stopped)
                }
                //setTimerLength(PreferenceManager(context).timeInMillis)
            }
        }.start()
    }

    fun stopTimer() {
        setTimerState(TimerState.Stopped)
        isPaused = false
        timer?.cancel()
    }

    fun modifyTimer(noteItems: List<DataItem>, index: Int) {
        stopTimer()
        if (index in 0..noteItems.lastIndex) {
            startTimer(noteItems, index)
        }
    }

    fun pauseTimer(currentTimerLength: Long) {
        timer?.cancel()
        setTimerState(TimerState.Paused)
        isPaused = true
        tempSavedTimerLengthMilli = currentTimerLength
    }

    private var tempSavedTimerLengthMilli = 0L
    private var isPaused: Boolean = false

/*
    private var timerTask: Deferred<Unit>? = null
    private var timerJob: Job? = null
    private fun coTimerStart(items: List<DataItem>, initItemIndex: Int, milliDelay: Long = 5L) {
        var itemIndex = initItemIndex
        timerTask = CoroutineScope(Dispatchers.Main).async {
            setActiveItemIndex(itemIndex)
            val activeItem = items[itemIndex]
            var activeTimeLengthMilli =
                activeItem.time.times(1000L) * 60F.pow(activeItem.unit).toLong()
            setTotalTimerLengthMilli(activeTimeLengthMilli)
            if (isPaused) {
                isPaused = false
                activeTimeLengthMilli = tempSavedTimerLengthMilli
            }
            setTimerState(TimerState.Running)
            val totalLength: Long = activeTimeLengthMilli
            Log.d("theTime", "total time is $totalLength")
            for (activeTime in totalLength..0 step milliDelay) {
                delay(milliDelay)
                Log.d("theTime", "active time is $activeTime")
                setTimerLength(activeTime)
            }
        }

        timerJob = CoroutineScope(Dispatchers.Main).launch {
            timerTask?.await()
            while (timerTask?.isCancelled != true && itemIndex < items.lastIndex) {
                incrementItemIndex()
                itemIndex += 1
                timerTask?.await()
            }
            setTimerState(TimerState.Stopped)
        }
    }

    fun coTimerStop() {
        timerTask?.cancel()
        timerJob?.cancel()
    }

    var flowTime: Flow<Long> = flowOf()
    @ExperimentalCoroutinesApi
    fun startFlowTimer(items: List<DataItem>, itemIndex: Int) {
        setActiveItemIndex(itemIndex)
        val activeItem = items[itemIndex]
        var activeTimeLengthMilli = activeItem.time.times(1000L) * 60F.pow(activeItem.unit).toLong()
        setTotalTimerLengthMilli(activeTimeLengthMilli)
        if (isPaused) {
            isPaused = false
            activeTimeLengthMilli = tempSavedTimerLengthMilli
        }
        setTimerState(TimerState.Running)

        flowTime = flowTimer(activeTimeLengthMilli)
        //ticker(5L, 5L, mode = TickerMode.FIXED_DELAY)
    }


    private fun flowTimer(timerLengthMilli: Long, milliDelay: Long = 10L): Flow<Long> =
        (timerLengthMilli downTo 0 step milliDelay).asFlow()
            .onEach { delay(milliDelay) }
            .onStart { emit(timerLengthMilli) }
            .conflate()
            .transform { remainingTime: Long ->
                emit(remainingTime)
                //(activeNoteId != 5) //for when the timer is active
            }
            .flowOn(Dispatchers.Default)
 */

    fun setActiveDataAndStartTimer(
        note: NoteItem,
        noteItems: List<DataItem>,
        index: Int = 0
    ) {
        stopTimer()
        activeNoteId = note.id
        setActiveItemIndex(index)

        startTimer(noteItems, index)
        //startFlowTimer(noteItems, index)

        //coTimerStart(noteItems, index)

    }

    private var _prevTimeType = 0
    val prevTimeType: Int
        get() = _prevTimeType

    fun setPrevTimeType(timeType: Int) {
        _prevTimeType = if (timeType in 0..2) {
            timeType
        } else {
            0
        }
    }

    // LiveData holds state which is observed by the UI
    // (state flows down from ViewModel)
    private var _timerLengthMilli: MutableLiveData<Long> = MutableLiveData(1L)
    val timerLengthMilli: LiveData<Long> = _timerLengthMilli

    //var totalTimerLengthMilli = MutableLiveData(1L)

    // setTimerLength is an event we're defining that the UI can invoke
    // (events flow up from UI)
    fun setTimerLength(timerLength: Long) {
        if (timerLength >= 0L) {
            _timerLengthMilli.value = timerLength
        } else {
            _timerLengthMilli.value = 0L
        }
    }
    //var timerLengthMilli2 by mutableStateOf(1L)

    // LiveData holds state which is observed by the UI
    // (state flows down from ViewModel)
    private var _timerState = MutableLiveData(TimerState.Stopped)
    val timerState: LiveData<TimerState> = _timerState

    // setTimerLength is an event we're defining that the UI can invoke
    // (events flow up from UI)
    fun setTimerState(timerState: TimerState) {
        _timerState.value = timerState
    }

    // LiveData holds state which is observed by the UI
    // (state flows down from ViewModel)
    private var _itemIndex = MutableLiveData(0)
    val itemIndex: LiveData<Int> = _itemIndex

    // setTimerLength is an event we're defining that the UI can invoke
    // (events flow up from UI)
    fun setActiveItemIndex(itemIndex: Int) {
        if (itemIndex >= 0) {
            _itemIndex.value = itemIndex
        }
    }

    fun incrementItemIndex() {
        itemIndex.value?.let {
            _itemIndex.value = it + 1
        }
    }

    var activeNoteId: Int = 0


    private var _totalTimerLengthMilli: MutableLiveData<Long> = MutableLiveData(1L)
    val totalTimerLengthMilli: LiveData<Long> = _totalTimerLengthMilli

    private fun setTotalTimerLengthMilli(timeInMilli: Long) {
        _totalTimerLengthMilli.value = timeInMilli
    }

}
