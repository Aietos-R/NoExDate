package org.umn.noexdate.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Pengingat Catatan"
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "Waktunya untuk menyelesaikan atau memeriksa catatan ini!"
        val id = intent.getIntExtra("EXTRA_ID", 200)

        // Memanggil pop-up notifikasi yang sudah kita buat sebelumnya
        NotificationHelper.showNotification(context, id, title, message)
    }
}