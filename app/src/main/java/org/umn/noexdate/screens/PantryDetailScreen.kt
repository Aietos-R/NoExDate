package org.umn.noexdate.screens

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.umn.noexdate.utils.AppPreferences
import org.umn.noexdate.viewmodel.PantryViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

fun extractDateFromString(text: String): LocalDate? {
    val regex8Digits = """\b(\d{2})(\d{2})(\d{4})\b""".toRegex()
    var match = regex8Digits.find(text)
    if (match != null) {
        return try {
            val (p1, p2, p3) = match.destructured
            if (p2.toInt() in 1..12 && p1.toInt() in 1..31) LocalDate.of(p3.toInt(), p2.toInt(), p1.toInt()) else null
        } catch (e: Exception) { null }
    }

    // PERBAIKAN: Mendukung spasi sebagai pemisah -> 27 07 25
    val regexFull = """\b(\d{1,2})[\s\-/.]+(\d{1,2})[\s\-/.]+(\d{2,4})\b""".toRegex()
    match = regexFull.find(text)
    if (match != null) {
        return try {
            val (p1, p2, p3) = match.destructured
            val year = if (p3.length == 2) 2000 + p3.toInt() else p3.toInt()
            LocalDate.of(year, p2.toInt(), p1.toInt())
        } catch (e: Exception) { null }
    }

    val regexNoSeparator = """(?i)(?:EXP|ED|MFG)[^\d]*(\d{2})(\d{2})(\d{2})\b""".toRegex()
    match = regexNoSeparator.find(text)
    if (match != null) {
        return try {
            val (p1, p2, p3) = match.destructured
            val year = 2000 + p3.toInt()
            LocalDate.of(year, p2.toInt(), p1.toInt())
        } catch (e: Exception) { null }
    }

    val regex6Digits = """\b(\d{2})(\d{2})(\d{2})\b""".toRegex()
    match = regex6Digits.find(text)
    if (match != null) {
        return try {
            val (p1, p2, p3) = match.destructured
            val year = 2000 + p3.toInt()
            if (p2.toInt() in 1..12 && p1.toInt() in 1..31) LocalDate.of(year, p2.toInt(), p1.toInt()) else null
        } catch (e: Exception) { null }
    }

    val regexMonthYear = """\b(0[1-9]|1[0-2])[\s\-/.]+(\d{2,4})\b""".toRegex()
    match = regexMonthYear.find(text)
    if (match != null) {
        return try {
            val (month, yearStr) = match.destructured
            val year = if (yearStr.length == 2) 2000 + yearStr.toInt() else yearStr.toInt()
            java.time.YearMonth.of(year, month.toInt()).atEndOfMonth()
        } catch (e: Exception) { null }
    }

    val regexShortNoSep = """(?i)(?:EXP|ED)[^\d]*((?:0[1-9]|1[0-2]))(\d{2})\b""".toRegex()
    match = regexShortNoSep.find(text)
    if (match != null) {
        return try {
            val (month, yearStr) = match.destructured
            val year = 2000 + yearStr.toInt()
            java.time.YearMonth.of(year, month.toInt()).atEndOfMonth()
        } catch (e: Exception) { null }
    }

    val regexTextDate = """(?i)\b(?:(\d{1,2})[\s\-/.,]*)?(jan|feb|mar|apr|mei|may|jun|jul|agu|aug|sep|okt|oct|nov|des|dec)[a-z]*[\s\-/.,]+(\d{2,4})\b""".toRegex()
    match = regexTextDate.find(text)
    if (match != null) {
        return try {
            val (dayStr, monthStr, yearStr) = match.destructured
            val monthMap = mapOf("jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4, "mei" to 5, "may" to 5, "jun" to 6, "jul" to 7, "agu" to 8, "aug" to 8, "sep" to 9, "okt" to 10, "oct" to 10, "nov" to 11, "des" to 12, "dec" to 12)
            val month = monthMap[monthStr.lowercase()] ?: 1
            val year = if (yearStr.length == 2) 2000 + yearStr.toInt() else yearStr.toInt()
            if (dayStr.isNotEmpty()) LocalDate.of(year, month, dayStr.toInt()) else java.time.YearMonth.of(year, month).atEndOfMonth()
        } catch (e: Exception) { null }
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryDetailScreen(navController: NavHostController, viewModel: PantryViewModel, pantryId: Int) {
    val context = LocalContext.current
    val prefs = remember { AppPreferences(context) }
    val existingItem = viewModel.activePantryItems.collectAsState().value.find { it.id == pantryId }

    // Ambil daftar kategori dinamis untuk Pantry
    val categoryList =
        remember { prefs.getLabels("pantry_labels", setOf("Bahan Makanan", "Minuman", "Cemilan", "Bumbu")) }

    var name by remember { mutableStateOf(existingItem?.name ?: "") }
    var category by remember { mutableStateOf(existingItem?.category ?: if (categoryList.isNotEmpty()) categoryList[0] else "Lainnya") }
    var quantity by remember { mutableStateOf(existingItem?.quantity?.toString() ?: "1") }
    var expirationDate by remember { mutableStateOf(existingItem?.expirationDate ?: (System.currentTimeMillis() + 86400000L)) }

    // Tambahan State Pengingat
    // State Pengingat Langsung (Tanpa Switch)
    var reminderConfig by remember { mutableStateOf("1 Hari Sebelum") }
    var showReminderMenu by remember { mutableStateOf(false) }
    // Tambahkan opsi "Tidak Ada"
    val reminderOptions = listOf("Tidak Ada", "1 Hari Sebelum", "1 Minggu Sebelum", "1 Bulan Sebelum", "Kustom")

    var customReminderDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showCustomDatePicker by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var isScanningCamera by remember { mutableStateOf(false) }
    val dateString = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID")).format(Date(expirationDate))

    fun onDateSuccessfullyFound(fullText: String, dateInMillis: Long) {
        expirationDate = dateInMillis
        // PERBAIKAN: name tidak lagi diisi otomatis dari hasil scan
        isScanningCamera = false
        Toast.makeText(context, "Tanggal Ditambahkan!", Toast.LENGTH_SHORT).show()
    }

    val photoPickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            uri?.let {
                val image = InputImage.fromFilePath(context, it)
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS).process(image)
                    .addOnSuccessListener { visionText ->
                        val extracted = extractDateFromString(visionText.text)
                        if (extracted != null) {
                            onDateSuccessfullyFound(
                                visionText.text,
                                extracted.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            )
                        } else Toast.makeText(context, "Tidak ada format tanggal yang cocok", Toast.LENGTH_SHORT).show()
                    }
            }
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) isScanningCamera = true else Toast.makeText(
                context,
                "Izin kamera ditolak",
                Toast.LENGTH_SHORT
            ).show()
        }

    if (isScanningCamera) {
        CameraScannerAutoView(onDateFound = { fullText, dateInMillis ->
            onDateSuccessfullyFound(
                fullText,
                dateInMillis
            )
        }, onClose = { isScanningCamera = false })
    } else {
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(20.dp).verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surface, CircleShape).clickable { navController.popBackStack() }, contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = MaterialTheme.colorScheme.onSurface)
                }

                Text(if (existingItem != null) "Edit Makanan" else "Tambah Makanan", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Tombol Hapus (Hanya muncul saat edit barang lama)
                    if (existingItem != null) {
                        Box(modifier = Modifier.size(40.dp).background(Color(0xFFE57373), CircleShape).clickable {
                            viewModel.deletePantryItems(listOf(existingItem))
                            navController.popBackStack()
                        }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.onSurface) }
                    }

                    // Tombol Simpan
                    Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, CircleShape).clickable {
                        if (name.isNotBlank()) {
                            if (existingItem != null) {
                                val updatedItem = existingItem.copy(name = name, category = category, quantity = quantity, expirationDate = expirationDate)
                                viewModel.updatePantryItem(updatedItem) // Pastikan fungsi update ada di PantryViewModel
                            } else {
                                viewModel.addPantryItem(name, category, quantity, expirationDate)
                            }
                            if (reminderConfig != "Tidak Ada") {
                                val reminderTime = when (reminderConfig) {
                                    "1 Hari Sebelum" -> expirationDate - (24 * 60 * 60 * 1000L)
                                    "1 Minggu Sebelum" -> expirationDate - (7 * 24 * 60 * 60 * 1000L)
                                    "1 Bulan Sebelum" -> expirationDate - (30L * 24 * 60 * 60 * 1000L)
                                    else -> customReminderDate
                                }
                                val calendar = java.util.Calendar.getInstance().apply {
                                    timeInMillis = reminderTime
                                    set(java.util.Calendar.HOUR_OF_DAY, 8)
                                    set(java.util.Calendar.MINUTE, 0)
                                    set(java.util.Calendar.SECOND, 0)
                                }
                                val alarmId = existingItem?.id ?: (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
                                org.umn.noexdate.utils.NotificationHelper.scheduleExactAlarm(context, calendar.timeInMillis, alarmId, "Segera Kedaluwarsa: $name")
                                Toast.makeText(context, "Pengingat diatur!", Toast.LENGTH_SHORT).show()
                            }

                            navController.popBackStack() // Pastikan baris ini tetap ada di bagian akhir
                        }
                    }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Check, contentDescription = "Simpan", tint = MaterialTheme.colorScheme.background) }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp)); Text(
                    "Kamera",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                }
                Button(
                    onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.ImageSearch, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp)); Text(
                    "Galeri",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("NAMA PRODUK", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Cth: Susu Oat", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp)
            )

            // PERBAIKAN: Kategori menggunakan FilterChip dinamis
            Text("KATEGORI", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categoryList) { cat ->
                    FilterChip(
                        selected = category == cat, onClick = { category = cat },
                        label = {
                            Text(
                                cat,
                                color = if (category == cat) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }

            Text("JUMLAH", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp)
            )

            Text("TANGGAL KEDALUWARSA", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = dateString,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clickable { showDatePicker = true },
                enabled = false
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("PENGINGAT KEDALUWARSA", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // Langsung tampilkan Dropdown
            ExposedDropdownMenuBox(
                expanded = showReminderMenu,
                onExpandedChange = { showReminderMenu = !showReminderMenu }
            ) {
                OutlinedTextField(
                    value = reminderConfig,
                    onValueChange = {}, readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showReminderMenu) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(16.dp)
                )
                ExposedDropdownMenu(expanded = showReminderMenu, onDismissRequest = { showReminderMenu = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                    reminderOptions.forEach { option ->
                        DropdownMenuItem(text = { Text(option, color = MaterialTheme.colorScheme.onSurface) }, onClick = {
                            reminderConfig = option; showReminderMenu = false
                        })
                    }
                }
            }
            if (reminderConfig == "Kustom") {
                val customDateStr = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID")).format(Date(customReminderDate))
                Spacer(modifier = Modifier.height(8.dp))

                // Bungkus dengan Box
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = customDateStr,
                        onValueChange = {},
                        readOnly = true, // Mencegah keyboard muncul
                        label = { Text("Tanggal Kustom", color = MaterialTheme.colorScheme.primary) },
                        trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                        // HAPUS enabled = false dan .clickable dari modifier ini
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize() // Mengikuti ukuran persis TextField di bawahnya
                            .clickable { showCustomDatePicker = true }
                    )
                }
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = expirationDate)
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                expirationDate = it
                            }; showDatePicker = false
                        }) { Text("Pilih", color = MaterialTheme.colorScheme.primary) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text(
                                "Batal",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.background), // Kunci warna solid di sini
                    shape = RoundedCornerShape(16.dp)
                ) {
                    DatePicker(
                        state = datePickerState,
                        colors = DatePickerDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.background, // Dan di sini
                            titleContentColor = MaterialTheme.colorScheme.onSurface, headlineContentColor = MaterialTheme.colorScheme.onSurface,
                            weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant, dayContentColor = MaterialTheme.colorScheme.onSurface,
                            selectedDayContainerColor = MaterialTheme.colorScheme.primary, selectedDayContentColor = MaterialTheme.colorScheme.background,
                            todayDateBorderColor = MaterialTheme.colorScheme.primary, todayContentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            //Memunculkan kalender pengingat kadaluarsa kustom
            if (showCustomDatePicker) {
                val customDatePickerState = rememberDatePickerState(initialSelectedDateMillis = customReminderDate)
                DatePickerDialog(
                    onDismissRequest = { showCustomDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            customDatePickerState.selectedDateMillis?.let {
                                customReminderDate = it
                            }
                            showCustomDatePicker = false
                        }) { Text("Pilih", color = MaterialTheme.colorScheme.primary) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCustomDatePicker = false }) {
                            Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    DatePicker(
                        state = customDatePickerState,
                        colors = DatePickerDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onSurface, headlineContentColor = MaterialTheme.colorScheme.onSurface,
                            weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant, dayContentColor = MaterialTheme.colorScheme.onSurface,
                            selectedDayContainerColor = MaterialTheme.colorScheme.primary, selectedDayContentColor = MaterialTheme.colorScheme.background,
                            todayDateBorderColor = MaterialTheme.colorScheme.primary, todayContentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(170.dp))
        }
    }
}

// ... (CameraScannerAutoView tetap sama seperti sebelumnya)
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun CameraScannerAutoView(onDateFound: (String, Long) -> Unit, onClose: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var isProcessing by remember { mutableStateOf(false) }
    var pendingDetectedDate by remember { mutableStateOf<Pair<String, Long>?>(null) }
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                    val imageAnalysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()

                    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                        if (isProcessing) { imageProxy.close(); return@setAnalyzer }

                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            recognizer.process(image)
                                .addOnSuccessListener { visionText ->
                                    val fullText = visionText.text
                                    if (fullText.isNotBlank()) {
                                        val extractedDate = extractDateFromString(fullText)
                                        if (extractedDate != null) {
                                            isProcessing = true
                                            val dateMillis = extractedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                            pendingDetectedDate = Pair(fullText, dateMillis)
                                        }
                                    }
                                    imageProxy.close()
                                }.addOnFailureListener { imageProxy.close() }
                        } else { imageProxy.close() }
                    }
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
                    } catch(e: Exception) {}
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f)).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Arahkan kamera ke Label Tanggal", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text("Otomatis mendeteksi jika format cocok...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))

            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onClose, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)), shape = RoundedCornerShape(16.dp)) {
                Text("Batal Scan", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
        }

        pendingDetectedDate?.let { result ->
            val formattedDate = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date(result.second))
            AlertDialog(
                onDismissRequest = { },
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text("Konfirmasi Tanggal", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
                text = { Text("Tanggal terbaca:\n$formattedDate\n\nApakah ini sudah benar?", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp) },
                confirmButton = {
                    TextButton(onClick = { onDateFound(result.first, result.second) }) {
                        Text("Benar", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        pendingDetectedDate = null
                        isProcessing = false
                    }) {
                        Text("Pindai Ulang", color = Color(0xFFE57373))
                    }
                }
            )
        }
    }
}