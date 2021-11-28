package com.augustbyrne.tas.ui.notes

import android.content.Context
import androidx.room.Room
import com.augustbyrne.tas.data.db.NoteDao
import com.augustbyrne.tas.data.db.NotesDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): NotesDatabase {
        return Room.databaseBuilder(
            appContext,
            NotesDatabase::class.java,
            "notes_database.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideDao(database: NotesDatabase): NoteDao = database.noteDao()

}
