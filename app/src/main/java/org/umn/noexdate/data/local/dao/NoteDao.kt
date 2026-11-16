package org.umn.noexdate.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.umn.noexdate.data.local.entity.NoteEntity

@Dao
interface NoteDao {
    // Membaca semua catatan secara realtime menggunakan Flow
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    // Menyimpan catatan baru
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    // Mengubah catatan yang sudah ada
    @Update
    suspend fun updateNote(note: NoteEntity)

    // Menghapus catatan
    @Delete
    suspend fun deleteNote(note: NoteEntity)
}