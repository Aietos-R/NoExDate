package org.umn.noexdate.screens

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
import androidx.navigation.NavHostController
import org.umn.noexdate.utils.AppPreferences

@Composable
fun ThemeSettingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val prefs = remember { AppPreferences(context) }

    var selectedTheme by remember { mutableStateOf(prefs.getThemeMode()) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(20.dp)) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surface, CircleShape).clickable { navController.popBackStack() }, contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Tema Aplikasi", color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        val options = listOf(
            Triple("SYSTEM", "Ikuti Sistem Android", "Menyesuaikan dengan pengaturan perangkat Anda"),
            Triple("LIGHT", "Light Mode (Terang)", "Selalu gunakan tampilan terang"),
            Triple("DARK", "Dark Mode (Gelap)", "Selalu gunakan tampilan gelap bernuansa hijau")
        )

        options.forEach { (key, title, subtitle) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable {
                        selectedTheme = key
                        prefs.setThemeMode(key)
                        // Catatan: Agar instan memperbarui seluruh activity, Anda bisa memicu restart atau recreate activity jika diperlukan
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
                RadioButton(
                    selected = selectedTheme == key,
                    onClick = {
                        selectedTheme = key
                        prefs.setThemeMode(key)
                        android.widget.Toast.makeText(context, "Tema berhasil diubah", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                )
            }
        }
        Spacer(modifier = Modifier.height(170.dp))
    }
}