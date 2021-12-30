package com.augustbyrne.tas.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.augustbyrne.tas.data.db.entities.DataItem
import com.augustbyrne.tas.data.db.entities.NoteItem
import com.augustbyrne.tas.util.Converters

// Annotates class to be a Room Database with a table (entity) of the Note class
@Database(
    entities = [NoteItem::class, DataItem::class],
    version = 11,
    autoMigrations = [
        //AutoMigration(from = 9, to = 10),
                     ],
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}