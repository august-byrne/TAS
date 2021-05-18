package com.example.protosuite.ui.notes

import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.room.TypeConverter
import com.example.protosuite.data.db.entities.NoteItem
import java.util.*

@BindingAdapter("SetTitle")
fun TextView.setTitle(item: NoteItem?){
    item?.let {
        text = item.title
        //val action = NotesDirections.actionNotesToCreateNote()
        //Navigation.findNavController(it).navigate(action)
    }
}

@BindingAdapter("SetDescription")
fun TextView.setDescription(item: NoteItem?){
    item?.let {
        text = item.description
    }
}


class Converters {
    @TypeConverter
    fun toCalendar(l: Long?): Calendar? =
        if (l == null) null else Calendar.getInstance().apply { timeInMillis = l }

    @TypeConverter
    fun fromCalendar(c: Calendar?): Long? = c?.time?.time
}