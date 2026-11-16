package org.umn.noexdate.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.umn.noexdate.data.local.entity.PantryEntity

@Dao
interface PantryDao {
    // Mengambil data yang diurutkan dari tanggal kedaluwarsa paling dekat
    @Query("SELECT * FROM pantry_items WHERE isConsumed = 0 ORDER BY expirationDate ASC")
    fun getActivePantryItems(): Flow<List<PantryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPantryItem(item: PantryEntity)

    @Update
    suspend fun updatePantryItem(item: PantryEntity)

    @Delete
    suspend fun deletePantryItem(item: PantryEntity)
}