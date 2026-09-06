package com.applenotes.ai.data.local.dao

import androidx.room.*
import com.applenotes.ai.data.local.model.NoteHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: NoteHistoryEntity): Long

    @Query("SELECT * FROM note_history WHERE noteId = :noteId ORDER BY timestamp DESC LIMIT 50")
    fun getHistoryForNote(noteId: Long): Flow<List<NoteHistoryEntity>>

    @Query("DELETE FROM note_history WHERE noteId = :noteId")
    suspend fun deleteHistoryForNote(noteId: Long)

    @Query("SELECT COUNT(*) FROM note_history WHERE noteId = :noteId")
    suspend fun getHistoryCount(noteId: Long): Int
}
