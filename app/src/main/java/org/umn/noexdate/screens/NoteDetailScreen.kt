package org.umn.noexdate.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.umn.noexdate.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(navController: NavHostController, viewModel: NoteViewModel, noteId: Int) {
    val existingNote = viewModel.allNotes.collectAsState().value.find { it.id == noteId }
    val context = LocalContext.current
    val cal = java.util.Calendar.getInstance()
    var selectedHour by remember { mutableStateOf(cal.get(java.util.Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(cal.get(java.util.Calendar.MINUTE)) }
    var isTimeSelected by remember { mutableStateOf(false) }

    val timePickerDialog = android.app.TimePickerDialog(
        context,
        { _, hour: Int, minute: Int ->
            selectedHour = hour
            selectedMinute = minute
            isTimeSelected = true
        }, cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE), true // true untuk format 24 jam
    )

    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.content ?: "") }
    var selectedType by remember { mutableStateOf(existingNote?.noteType ?: "Note Only") }
    var showOnCalendar by remember { mutableStateOf(existingNote?.showOnCalendar ?: false) }

    // Status Tanggal Acara (Deadline)
    var deadlineAt by remember { mutableStateOf(existingNote?.deadlineAt ?: System.currentTimeMillis()) }
    var showEventDatePicker by remember { mutableStateOf(false) }

    // Status Pengingat & Pengulangan
    var reminderEnabled by remember { mutableStateOf(existingNote?.reminderEnabled ?: false) }
    var reminderConfig by remember { mutableStateOf(existingNote?.reminderConfiguration ?: "Pada waktu yang ditentukan") }
    var recurrence by remember { mutableStateOf(existingNote?.recurrence ?: "Tidak berulang") }

    var imageUri by remember { mutableStateOf(existingNote?.imageUri) }

    val noteTypes = listOf("Note Only", "Task", "Deadline", "Event", "Reminder", "Personal", "Work", "Other")
    // Tambahan Opsi Kustom
    val reminderOptions = listOf("Pada waktu yang ditentukan", "1 hari sebelumnya", "3 hari sebelumnya", "1 minggu sebelumnya", "Kustom (Pilih Tanggal)")
    val recurrenceOptions = listOf("Tidak berulang", "Setiap hari", "Setiap minggu", "Setiap bulan", "Setiap tahun")

    var showReminderMenu by remember { mutableStateOf(false) }
    var showRecurrenceMenu by remember { mutableStateOf(false) }
    var showCustomReminderDatePicker by remember { mutableStateOf(false) } // Pemilih waktu kustom

    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            content = if (content.isEmpty()) spokenText else "$content $spokenText"
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            imageUri = uri.toString()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(20.dp).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surface, CircleShape).clickable { navController.popBackStack() }, contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = MaterialTheme.colorScheme.onSurface) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (existingNote != null) "Edit Catatan" else "Tambah Catatan", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                if (existingNote != null) {
                    // Mengambil waktu terakhir diubah (updatedAt) atau waktu dibuat (createdAt)
                    val timeToDisplay = existingNote.updatedAt ?: existingNote.createdAt
                    val timestamp = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date(timeToDisplay))
                    Text(
                        text = "Terakhir diubah: $timestamp",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (existingNote != null) Box(modifier = Modifier.size(40.dp).background(Color(0xFFE57373), CircleShape).clickable { viewModel.deleteNote(existingNote); navController.popBackStack() }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.onSurface) }
                Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, CircleShape).clickable {
                    val currentTime = System.currentTimeMillis()

                    // 1. Simpan Data Catatan (Pemisahan Logika Edit vs Baru)
                    if (existingNote != null) {
                        val updatedNote = existingNote.copy(
                            title = title, content = content, noteType = selectedType,
                            showOnCalendar = showOnCalendar, deadlineAt = deadlineAt,
                            reminderEnabled = reminderEnabled, reminderConfiguration = reminderConfig,
                            recurrence = recurrence, imageUri = imageUri, updatedAt = currentTime
                        )
                        viewModel.updateNote(updatedNote)
                    } else {
                        // Kirim SELURUH parameter untuk catatan baru!
                        val newNote = org.umn.noexdate.data.local.entity.NoteEntity(
                            title = title, content = content, noteType = selectedType,
                            showOnCalendar = showOnCalendar, deadlineAt = deadlineAt,
                            reminderEnabled = reminderEnabled, reminderConfiguration = reminderConfig,
                            recurrence = recurrence, imageUri = imageUri,
                            createdAt = currentTime, updatedAt = currentTime
                        )
                        viewModel.insertNoteFull(newNote) // Memanggil fungsi baru di ViewModel
                    }

                    // 2. Jadwalkan Alarm Presisi
                    if (reminderEnabled) {
                        val reminderCalendar = java.util.Calendar.getInstance().apply {
                            timeInMillis = deadlineAt
                            if (isTimeSelected) {
                                set(java.util.Calendar.HOUR_OF_DAY, selectedHour)
                                set(java.util.Calendar.MINUTE, selectedMinute)
                                set(java.util.Calendar.SECOND, 0)
                            }
                        }

                        // Generate ID super unik untuk catatan baru agar tidak tertimpa
                        val alarmId = existingNote?.id ?: (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
                        org.umn.noexdate.utils.NotificationHelper.scheduleExactAlarm(
                            context = context,
                            timeInMillis = reminderCalendar.timeInMillis,
                            noteId = alarmId,
                            title = title.ifEmpty { "Jadwal Tanpa Judul" } // Pastikan judul terkirim
                        )
                        android.widget.Toast.makeText(context, "Pengingat diatur pada ${String.format("%02d:%02d", selectedHour, selectedMinute)}", android.widget.Toast.LENGTH_SHORT).show()
                    }

                    navController.popBackStack()
                }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Check, contentDescription = "Simpan", tint = MaterialTheme.colorScheme.background) }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(noteTypes) { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { selectedType = type; if (type == "Note Only") { showOnCalendar = false; reminderEnabled = false } },
                    label = { Text(type, color = if (selectedType == type) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column { Text("Tampilkan di Kalender", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp); Text("Jadikan catatan sebagai acara/deadline", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) }
            Switch(checked = showOnCalendar, onCheckedChange = { showOnCalendar = it }, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.background, checkedTrackColor = MaterialTheme.colorScheme.primary))
        }

        // Tampilkan pemilih tanggal jika Tampilkan di Kalender aktif
        if (showOnCalendar) {
            val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date(deadlineAt))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = dateStr, onValueChange = { }, readOnly = true,
                label = { Text("Tanggal Acara/Deadline", color = MaterialTheme.colorScheme.primary) },
                trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().clickable { showEventDatePicker = true }, enabled = false
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column { Text("Aktifkan Pengingat", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp); Text("Membutuhkan izin notifikasi", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) }
            Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.background, checkedTrackColor = MaterialTheme.colorScheme.primary))
        }

        if (reminderEnabled) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                ExposedDropdownMenuBox(
                    expanded = showReminderMenu, onExpandedChange = { showReminderMenu = !showReminderMenu },
                    modifier = Modifier.weight(1f) // Mengisi sisa ruang
                ) {
                    OutlinedTextField(
                        value = reminderConfig, onValueChange = {}, readOnly = true, label = { Text("Waktu Pengingat", color = MaterialTheme.colorScheme.primary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showReminderMenu) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(16.dp)
                    )
                    ExposedDropdownMenu(expanded = showReminderMenu, onDismissRequest = { showReminderMenu = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                        reminderOptions.forEach { option ->
                            DropdownMenuItem(text = { Text(option, color = MaterialTheme.colorScheme.onSurface) }, onClick = {
                                reminderConfig = option; showReminderMenu = false
                                if (option == "Kustom (Pilih Tanggal)") showCustomReminderDatePicker = true
                            })
                        }
                    }
                }

                // Tombol jam sekarang berada di luar dan di samping dropdown
                Button(
                    onClick = { timePickerDialog.show() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(56.dp) // Menyamakan tinggi dengan TextField
                ) {
                    val timeText = if (isTimeSelected) String.format("%02d:%02d", selectedHour, selectedMinute) else "Jam"
                    Icon(Icons.Default.AccessTime, contentDescription = "Waktu", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(timeText, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            ExposedDropdownMenuBox(expanded = showRecurrenceMenu, onExpandedChange = { showRecurrenceMenu = !showRecurrenceMenu }) {
                OutlinedTextField(
                    value = recurrence, onValueChange = {}, readOnly = true, label = { Text("Pengulangan", color = MaterialTheme.colorScheme.primary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showRecurrenceMenu) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(16.dp)
                )
                ExposedDropdownMenu(expanded = showRecurrenceMenu, onDismissRequest = { showRecurrenceMenu = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                    recurrenceOptions.forEach { option ->
                        DropdownMenuItem(text = { Text(option, color = MaterialTheme.colorScheme.onSurface) }, onClick = { recurrence = option; showRecurrenceMenu = false })
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply { putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID") }
                voiceLauncher.launch(intent)
            }) { Icon(Icons.Default.Mic, contentDescription = "Dikte Suara", tint = MaterialTheme.colorScheme.primary) }
            IconButton(onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Icon(Icons.Default.Image, contentDescription = "Pilih Gambar", tint = MaterialTheme.colorScheme.primary) }
        }

        if (imageUri != null) {
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                AsyncImage(model = imageUri, contentDescription = "Lampiran Foto", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)))
                IconButton(onClick = { imageUri = null }, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Black.copy(alpha=0.5f), CircleShape)) { Icon(Icons.Default.Close, contentDescription = "Hapus Gambar", tint = Color.White) }
            }
        }

        TextField(value = title, onValueChange = { title = it }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface), textStyle = LocalTextStyle.current.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold), modifier = Modifier.fillMaxWidth(), placeholder = { Text("Judul...", color = MaterialTheme.colorScheme.onSurfaceVariant) })
        TextField(value = content, onValueChange = { content = it }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface), modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp), placeholder = { Text("Mulai mengetik...", color = MaterialTheme.colorScheme.onSurfaceVariant) })
        Spacer(modifier = Modifier.height(170.dp))
    }

    // Pemilih Tanggal Acara (Event)
    if (showEventDatePicker) {
        val eventPickerState = rememberDatePickerState(initialSelectedDateMillis = deadlineAt)
        DatePickerDialog(
            onDismissRequest = { showEventDatePicker = false },
            confirmButton = { TextButton(onClick = { eventPickerState.selectedDateMillis?.let { deadlineAt = it }; showEventDatePicker = false }) { Text("Pilih", color = MaterialTheme.colorScheme.primary) } },
            dismissButton = { TextButton(onClick = { showEventDatePicker = false }) { Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant) } },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.background), shape = RoundedCornerShape(16.dp)
        ) { DatePicker(state = eventPickerState, colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.background, titleContentColor = MaterialTheme.colorScheme.onSurface, headlineContentColor = MaterialTheme.colorScheme.onSurface, weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant, dayContentColor = MaterialTheme.colorScheme.onSurface, selectedDayContainerColor = MaterialTheme.colorScheme.primary, selectedDayContentColor = MaterialTheme.colorScheme.background)) }
    }

    // Pemilih Tanggal Kustom Pengingat
    if (showCustomReminderDatePicker) {
        val customPickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showCustomReminderDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    customPickerState.selectedDateMillis?.let {
                        val customDate = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(it))
                        reminderConfig = "Kustom: $customDate"
                    }
                    showCustomReminderDatePicker = false
                }) { Text("Atur Pengingat", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = { TextButton(onClick = { showCustomReminderDatePicker = false }) { Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant) } },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.background), shape = RoundedCornerShape(16.dp)
        ) { DatePicker(state = customPickerState, colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.background, titleContentColor = MaterialTheme.colorScheme.onSurface, headlineContentColor = MaterialTheme.colorScheme.onSurface, weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant, dayContentColor = MaterialTheme.colorScheme.onSurface, selectedDayContainerColor = MaterialTheme.colorScheme.primary, selectedDayContentColor = MaterialTheme.colorScheme.background)) }
    }
}