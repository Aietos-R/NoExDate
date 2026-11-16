package org.umn.noexdate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val noteType: String = "Note Only",
    val tags: String = "", // Tambahan untuk Update 2
    val showOnCalendar: Boolean = false,
    val reminderEnabled: Boolean = false,
    val deadlineAt: Long? = null,
    val reminderConfiguration: String = "Pada waktu yang ditentukan", // Tambahan untuk Update 2
    val imageUri: String? = null, // TAMBAHAN UPDATE 10: Menyimpan path foto
    val recurrence: String = "Tidak berulang", // TAMBAHAN UPDATE 12
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)