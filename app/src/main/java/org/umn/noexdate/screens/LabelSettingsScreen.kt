package org.umn.noexdate.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.umn.noexdate.utils.AppPreferences

@Composable
fun LabelSettingsScreen(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(20.dp).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surface, CircleShape).clickable { navController.popBackStack() }, contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Pengelola Label", color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(32.dp))

        LabelManagerSection()
    }
}


@Composable
fun LabelManagerSection() {
    val context = LocalContext.current
    val prefs = remember { AppPreferences(context) }

    // Key penyimpan sesuai kebutuhan layar
    val sections = listOf(
        Pair("Kategori Pantry", "pantry_labels" to setOf("Bahan Makanan", "Minuman", "Cemilan", "Bumbu")),
        Pair("Tipe Catatan", "note_labels" to setOf("Note Only", "Task", "Deadline", "Event", "Personal", "Work", "Other")),
        Pair("Kategori Pengeluaran", "expense_labels" to setOf("Makanan", "Transportasi", "Hiburan", "Belanja", "Lainnya"))
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        sections.forEach { (title, data) ->
            val (prefKey, defaultSet) = data
            var currentLabels by remember { mutableStateOf(prefs.getLabels(prefKey, defaultSet)) }
            var newLabel by remember { mutableStateOf("") }

            Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newLabel, onValueChange = { newLabel = it },
                    placeholder = { Text("Label Baru", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedTextColor = MaterialTheme.colorScheme.onSurface)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        prefs.saveLabel(prefKey, newLabel, defaultSet)
                        currentLabels = prefs.getLabels(prefKey, defaultSet)
                        newLabel = ""
                        android.widget.Toast.makeText(context, "Label baru berhasil ditambahkan", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.height(50.dp)
                ) { Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.background) }
            }

            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                items(currentLabels) { label ->
                    InputChip(
                        selected = false,
                        onClick = { },
                        label = { Text(label, color = MaterialTheme.colorScheme.onSurface) },
                        trailingIcon = {
                            IconButton(onClick = {
                                prefs.removeLabel(prefKey, label, defaultSet)
                                currentLabels = prefs.getLabels(prefKey, defaultSet)
                                android.widget.Toast.makeText(context, "Label berhasil dihapus", android.widget.Toast.LENGTH_SHORT).show()
                            }, modifier = Modifier.size(16.dp)) { Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFE57373)) }
                        },
                        colors = InputChipDefaults.inputChipColors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 16.dp))
        }
    }
}