package org.umn.noexdate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pantry_items")
data class PantryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: String = "Bahan Makanan",
    val quantity: String = "1",
    val expirationDate: Long, // Disimpan dalam bentuk timestamp
    val isConsumed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)