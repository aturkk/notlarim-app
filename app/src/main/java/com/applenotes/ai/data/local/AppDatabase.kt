package com.applenotes.ai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.applenotes.ai.data.local.converters.Converters
import com.applenotes.ai.data.local.dao.FolderDao
import com.applenotes.ai.data.local.dao.NoteDao
import com.applenotes.ai.data.local.model.FolderEntity
import com.applenotes.ai.data.local.model.NoteEntity

@Database(
    entities = [NoteEntity::class, FolderEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val noteDao: NoteDao
    abstract val folderDao: FolderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "apple_notes_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
