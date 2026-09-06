package com.applenotes.ai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.applenotes.ai.data.local.converters.Converters
import com.applenotes.ai.data.local.dao.FolderDao
import com.applenotes.ai.data.local.dao.NoteDao
import com.applenotes.ai.data.local.dao.NoteHistoryDao
import com.applenotes.ai.data.local.model.FolderEntity
import com.applenotes.ai.data.local.model.NoteEntity
import com.applenotes.ai.data.local.model.NoteHistoryEntity

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [NoteEntity::class, FolderEntity::class, NoteHistoryEntity::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val noteDao: NoteDao
    abstract val folderDao: FolderDao
    abstract val noteHistoryDao: NoteHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN isLocked INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE notes ADD COLUMN drawingPath TEXT")
                db.execSQL("ALTER TABLE notes ADD COLUMN audioPath TEXT")
                db.execSQL("ALTER TABLE notes ADD COLUMN reminderTime INTEGER")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN icon TEXT")
                db.execSQL("ALTER TABLE notes ADD COLUMN coverUrl TEXT")
                db.execSQL("ALTER TABLE notes ADD COLUMN kanbanColumn TEXT")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS note_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        noteId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_note_history_noteId ON note_history(noteId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_note_history_timestamp ON note_history(timestamp)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN priority TEXT")
                db.execSQL("ALTER TABLE notes ADD COLUMN status TEXT")
                db.execSQL("ALTER TABLE notes ADD COLUMN progress INTEGER")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "apple_notes_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
