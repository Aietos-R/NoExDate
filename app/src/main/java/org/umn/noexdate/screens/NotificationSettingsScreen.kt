package org.umn.noexdate.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import org.umn.noexdate.utils.AppPreferences
@Composable
fun NotificationSettingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val prefs = remember { AppPreferences(context) }

    // Membaca status awal dari SharedPreferences
    var masterEnabled by remember { mutableStateOf(prefs.prefs.getBoolean("notif_master", true)) }
    var notesEnabled by remember { mutableStateOf(prefs.prefs.getBoolean("notif_notes", true)) }
    var pantryEnabled by remember { mutableStateOf(prefs.prefs.getBoolean("notif_pantry", true)) }
    var expenseEnabled by remember { mutableStateOf(prefs.prefs.getBoolean("notif_expense", true)) }

    // Launcher untuk meminta izin notifikasi dari sistem (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            masterEnabled = true
            prefs.prefs.edit().putBoolean("notif_master", true).apply()
            Toast.makeText(context, "Izin notifikasi diberikan", Toast.LENGTH_SHORT).show()
        } else {
            masterEnabled = false
            prefs.prefs.edit().putBoolean("notif_master", false).apply()
            Toast.makeText(context, "Izin ditolak. Aktifkan manual di Pengaturan HP.", Toast.LENGTH_LONG).show()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(20.dp)) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surface, CircleShape).clickable { navController.popBackStack() }, contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Notifikasi & Pengingat", color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- MASTER SWITCH NOTIFIKASI ---
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Izinkan Notifikasi", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Peringatan utama aplikasi", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Switch(
                checked = masterEnabled,
                onCheckedChange = { isChecked ->
                    if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            masterEnabled = true
                            prefs.prefs.edit().putBoolean("notif_master", true).apply()
                            Toast.makeText(context, "Notifikasi utama diaktifkan", Toast.LENGTH_SHORT).show()
                        } else {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    } else {
                        masterEnabled = isChecked
                        prefs.prefs.edit().putBoolean("notif_master", isChecked).apply()
                        val pesan = if (isChecked) "Notifikasi utama diaktifkan" else "Notifikasi utama dimatikan"
                        Toast.makeText(context, pesan, Toast.LENGTH_SHORT).show()
                    }
                },
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        }

        if (masterEnabled) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("PENGATURAN SPESIFIK", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(16.dp))

            // --- SUB-SWITCH PENGATURAN (Di-looping agar rapi) ---
            val subSettings = listOf(
                Triple("Catatan & Kalender", "Pengingat jadwal dan deadline", notesEnabled) to { v: Boolean -> notesEnabled = v; prefs.prefs.edit().putBoolean("notif_notes", v).apply() },
                Triple("Pantry", "Peringatan makanan kedaluwarsa", pantryEnabled) to { v: Boolean -> pantryEnabled = v; prefs.prefs.edit().putBoolean("notif_pantry", v).apply() },
                Triple("Keuangan", "Batas pengeluaran & tagihan", expenseEnabled) to { v: Boolean -> expenseEnabled = v; prefs.prefs.edit().putBoolean("notif_expense", v).apply() }
            )

            subSettings.forEach { (info, action) ->
                val (title, subtitle, state) = info
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    Switch(
                        checked = state,
                        onCheckedChange = { isChecked ->
                            action(isChecked)
                            val statusText = if (isChecked) "diaktifkan" else "dimatikan"
                            Toast.makeText(context, "Pengingat $title berhasil $statusText", Toast.LENGTH_SHORT).show() // Handler Toast
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SettingSwitchItem(title: String, subtitle: String, checked: Boolean, enabled: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
        Switch(
            checked = checked && enabled,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.background, checkedTrackColor = MaterialTheme.colorScheme.primary)
        )
    }
}