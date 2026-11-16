package org.umn.noexdate.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import org.umn.noexdate.data.local.database.NoexDatabase
import org.umn.noexdate.data.local.entity.NoteEntity
import org.umn.noexdate.data.local.entity.PantryEntity
import org.umn.noexdate.utils.AppLogger
import org.umn.noexdate.utils.AppPreferences
import org.umn.noexdate.utils.NotificationHelper
import java.util.Calendar

class DailyReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        AppLogger.init(context)
        AppLogger.logActivity("WORKER", "DailyReminderWorker mulai berjalan di background")

        return try {
            val prefs = AppPreferences(context)

            // 1. Cek Master Switch
            if (!prefs.prefs.getBoolean("notif_master", true)) {
                AppLogger.logActivity("WORKER", "Dibatalkan karena Master Switch notifikasi mati")
                return Result.success()
            }

            val database = NoexDatabase.getDatabase(context)
            val now = System.currentTimeMillis()
            val threeDaysInMillis = 3L * 24 * 60 * 60 * 1000

            // 2. Pengecekan PANTRY
            if (prefs.prefs.getBoolean("notif_pantry", true)) {
                val pantryList: List<PantryEntity> = database.pantryDao().getActivePantryItems().first()
                val expiringItems = mutableListOf<String>()

                pantryList.forEach { item ->
                    val diff = item.expirationDate - now
                    if (diff in 0..threeDaysInMillis || diff < 0) {
                        expiringItems.add(item.name)
                    }
                }

                if (expiringItems.isNotEmpty()) {
                    val names = expiringItems.joinToString(", ")
                    NotificationHelper.showNotification(
                        context, 101,
                        "Peringatan Pantry 🍎",
                        "Cek makanan ini: $names"
                    )
                    AppLogger.logActivity("WORKER", "Notifikasi Pantry terkirim untuk: $names")
                }
            }

            // 3. Pengecekan NOTES
            if (prefs.prefs.getBoolean("notif_notes", true)) {
                val notesList: List<NoteEntity> = database.noteDao().getAllNotes().first()
                var todayEventCount = 0
                val todayCal = Calendar.getInstance()

                notesList.forEach { note ->
                    val noteCal = Calendar.getInstance().apply { timeInMillis = note.deadlineAt ?: note.createdAt }
                    if (note.showOnCalendar &&
                        noteCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                        noteCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)
                    ) {
                        todayEventCount++
                    }
                }

                if (todayEventCount > 0) {
                    NotificationHelper.showNotification(
                        context, 102,
                        "Jadwal Hari Ini 📅",
                        "Anda memiliki $todayEventCount catatan/acara hari ini."
                    )
                    AppLogger.logActivity("WORKER", "Notifikasi Notes terkirim ($todayEventCount jadwal)")
                }
            }

            AppLogger.logActivity("WORKER", "DailyReminderWorker selesai dengan sukses")
            Result.success()
        } catch (e: Exception) {
            AppLogger.logError("WORKER", "Eksekusi DailyReminderWorker", e)
            Result.retry()
        }
    }
}