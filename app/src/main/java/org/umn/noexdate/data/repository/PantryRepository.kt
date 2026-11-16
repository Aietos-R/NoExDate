package org.umn.noexdate.data.repository

import kotlinx.coroutines.flow.Flow
import org.umn.noexdate.data.local.dao.PantryDao
import org.umn.noexdate.data.local.entity.PantryEntity

class PantryRepository(private val pantryDao: PantryDao) {

    // Mengambil semua data makanan yang belum dikonsumsi (aktif)
    val activePantryItems: Flow<List<PantryEntity>> = pantryDao.getActivePantryItems()

    suspend fun insert(item: PantryEntity) {
        pantryDao.insertPantryItem(item)
    }

    suspend fun update(item: PantryEntity) {
        pantryDao.updatePantryItem(item)
    }

    suspend fun delete(item: PantryEntity) {
        pantryDao.deletePantryItem(item)
    }
}