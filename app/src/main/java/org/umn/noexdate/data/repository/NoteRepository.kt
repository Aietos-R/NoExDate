package org.umn.noexdate.data.repository

import kotlinx.coroutines.flow.Flow
import org.umn.noexdate.data.local.dao.NoteDao
import org.umn.noexdate.data.local.entity.NoteEntity

class NoteRepository(private val noteDao: NoteDao) {

    // Mengambil semua catatan dalam bentuk aliran data (Flow)
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()

    suspend fun insert(note: NoteEntity) {
        noteDao.insertNote(note)
    }

    suspend fun update(note: NoteEntity) {
        noteDao.updateNote(note)
    }

    suspend fun delete(note: NoteEntity) {
        noteDao.deleteNote(note)
    }
}