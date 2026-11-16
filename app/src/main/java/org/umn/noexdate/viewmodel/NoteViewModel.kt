package org.umn.noexdate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.umn.noexdate.data.local.entity.NoteEntity
import org.umn.noexdate.data.repository.NoteRepository
import org.umn.noexdate.utils.AppLogger

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    // Menyediakan data secara realtime ke UI
    val allNotes: StateFlow<List<NoteEntity>> = repository.allNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Fungsi untuk menambah catatan baru dari UI
    fun insertNoteFull(note: NoteEntity) {
        viewModelScope.launch {
            try {
                if (note.title.isBlank() && note.content.isBlank()) {
                    throw IllegalArgumentException("Judul atau isi catatan tidak boleh kosong")
                }
                repository.insert(note)
                AppLogger.logSuccess("NOTES", "Catatan berhasil disimpan", "Judul: ${note.title}")
            } catch (e: Exception) {
                AppLogger.logError("NOTES", "Simpan catatan", e)
            }
        }
    }

    // Fungsi untuk memperbarui catatan yang sudah ada
    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            try {
                repository.update(note)
                AppLogger.logSuccess("NOTES", "Catatan berhasil diperbarui", "ID: ${note.id}")
            } catch (e: Exception) {
                AppLogger.logError("NOTES", "Perbarui catatan", e)
            }
        }
    }

    // Fungsi untuk menghapus catatan
    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            try {
                repository.delete(note)
                AppLogger.logSuccess("NOTES", "Catatan berhasil dihapus", "Judul: ${note.title}")
            } catch (e: Exception) {
                AppLogger.logError("NOTES", "Hapus catatan", e)
            }
        }
    }
    // Fungsi hapus banyak catatan sekaligus (Update 3)
    fun deleteNotes(notes: List<NoteEntity>) {
        viewModelScope.launch {
            notes.forEach { repository.delete(it) }
        }
    }
}

// Factory digunakan untuk membuat ViewModel karena kita membutuhkan parameter Repository
class NoteViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}