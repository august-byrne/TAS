package com.augustbyrne.tas.ui.notes

import androidx.room.TypeConverter
import java.util.*

class Converters {
    @TypeConverter
    fun toCalendar(l: Long?): Calendar? =
        if (l == null) null else Calendar.getInstance().apply { timeInMillis = l }

    @TypeConverter
    fun fromCalendar(c: Calendar?): Long? = c?.time?.time
}

enum class TimerState {
    Stopped, Paused, Running
}

enum class SortType {
    Creation, LastEdited, Order, Default
}

enum class EditDialogType {
    Title, Description, DialogClosed
}