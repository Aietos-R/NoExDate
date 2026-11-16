package org.umn.noexdate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.umn.noexdate.data.local.entity.PantryEntity
import org.umn.noexdate.data.repository.PantryRepository
import org.umn.noexdate.utils.AppLogger

class PantryViewModel(private val repository: PantryRepository) : ViewModel() {

    val activePantryItems: StateFlow<List<PantryEntity>> = repository.activePantryItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addPantryItem(name: String, category: String, quantity: String, expirationDate: Long) {
        viewModelScope.launch {
            try {
                if (name.isBlank()) {
                    throw IllegalArgumentException("Nama bahan makanan tidak boleh kosong")
                }
                val newItem = PantryEntity(
                    name = name,
                    category = category,
                    quantity = quantity,
                    expirationDate = expirationDate
                )
                repository.insert(newItem)
                AppLogger.logSuccess("PANTRY", "Bahan makanan berhasil ditambahkan", "Nama: $name")
            } catch (e: Exception) {
                AppLogger.logError("PANTRY", "Tambah makanan", e)
            }
        }
    }

    fun updatePantryItem(item: PantryEntity) {
        viewModelScope.launch {
            try {
                repository.update(item)
                AppLogger.logSuccess("PANTRY", "Data makanan berhasil diperbarui", "Nama: ${item.name}")
            } catch (e: Exception) {
                AppLogger.logError("PANTRY", "Perbarui makanan", e)
            }
        }
    }

    fun consumeItem(item: PantryEntity) {
        viewModelScope.launch {
            try {
                repository.update(item.copy(isConsumed = true))
                AppLogger.logSuccess("PANTRY", "${item.name} ditandai sudah habis/dikonsumsi")
            } catch (e: Exception) {
                AppLogger.logError("PANTRY", "Tandai makanan habis", e)
            }
        }
    }

    fun deleteItem(item: PantryEntity) {
        viewModelScope.launch {
            try {
                repository.delete(item)
                AppLogger.logSuccess("PANTRY", "Bahan makanan berhasil dihapus", "Nama: ${item.name}")
            } catch (e: Exception) {
                AppLogger.logError("PANTRY", "Hapus makanan", e)
            }
        }
    }

    fun deletePantryItems(items: List<PantryEntity>) {
        viewModelScope.launch {
            try {
                items.forEach { repository.delete(it) }
                AppLogger.logSuccess("PANTRY", "${items.size} bahan makanan berhasil dihapus")
            } catch (e: Exception) {
                AppLogger.logError("PANTRY", "Hapus beberapa makanan", e)
            }
        }
    }
}

class PantryViewModelFactory(private val repository: PantryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PantryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PantryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}