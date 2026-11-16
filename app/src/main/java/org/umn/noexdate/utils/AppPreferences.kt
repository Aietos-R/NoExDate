package org.umn.noexdate.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class AppPreferences(context: Context) {
    val prefs: SharedPreferences = context.getSharedPreferences("noexdate_prefs", Context.MODE_PRIVATE)

    fun getLabels(type: String, defaultLabels: Set<String>): List<String> {
        return prefs.getStringSet(type, defaultLabels)?.toList()?.sorted() ?: defaultLabels.toList()
    }

    fun saveLabel(type: String, label: String, defaultLabels: Set<String>) {
        val current = prefs.getStringSet(type, defaultLabels)?.toMutableSet() ?: defaultLabels.toMutableSet()
        if (label.isNotBlank() && !current.contains(label)) {
            current.add(label)
            prefs.edit { putStringSet(type, current) }
        }
    }

    fun removeLabel(type: String, label: String, defaultLabels: Set<String>) {
        val current = prefs.getStringSet(type, defaultLabels)?.toMutableSet() ?: defaultLabels.toMutableSet()
        current.remove(label)
        prefs.edit { putStringSet(type, current) }
    }

    // Simpan pilihan tema: "SYSTEM", "LIGHT", atau "DARK"
    fun getThemeMode(): String {
        return prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM"
    }

    fun setThemeMode(mode: String) {
        prefs.edit { putString("theme_mode", mode) }
    }
}