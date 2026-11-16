package org.umn.noexdate.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.umn.noexdate.data.local.entity.ExpenseEntity
import org.umn.noexdate.utils.AppPreferences
import org.umn.noexdate.viewmodel.ExpenseViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(navController: NavHostController, viewModel: ExpenseViewModel) {
    val context = LocalContext.current
    val prefs = remember { AppPreferences(context) }

    val expenses by viewModel.allExpenses.collectAsState()
    val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }

    val dynamicLabels = remember { prefs.getLabels("expense_labels", setOf("Makanan", "Transportasi", "Hiburan", "Belanja", "Lainnya")) }
    val filterOptions = listOf("All") + dynamicLabels

    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf(setOf<ExpenseEntity>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    fun toggleSelection(expense: ExpenseEntity) {
        selectedItems = if (selectedItems.contains(expense)) selectedItems - expense else selectedItems + expense
        if (selectedItems.isEmpty()) isSelectionMode = false
    }

    // State untuk Dropdown Bulan
    val cal = Calendar.getInstance()
    var selectedMonth by remember { mutableStateOf(cal.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableStateOf(cal.get(Calendar.YEAR)) }
    var showMonthMenu by remember { mutableStateOf(false) }
    val monthNames = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")

    // Filter transaksi HANYA untuk bulan & tahun yang dipilih
    val baseMonthExpenses = expenses.filter {
        val expCal = Calendar.getInstance().apply { timeInMillis = it.expenseDate }
        expCal.get(Calendar.MONTH) == selectedMonth && expCal.get(Calendar.YEAR) == selectedYear
    }

    // Total pengeluaran dinamis sesuai bulan yang dipilih
    val currentMonthTotal = baseMonthExpenses.sumOf { it.amount }

    // Filter akhir (Bulan + Kategori + Pencarian) untuk List riwayat
    val filteredExpenses = baseMonthExpenses.filter {
        val matchCategory = selectedFilter == "All" || it.category == selectedFilter
        val matchSearch = searchQuery.isBlank() || it.description.contains(searchQuery, ignoreCase = true)
        matchCategory && matchSearch
    }

    val chartColors = listOf(Color(0xFFE57373), Color(0xFF64B5F6), MaterialTheme.colorScheme.primary, Color(0xFF81C784), Color(0xFFBA68C8), Color(0xFFFFB74D))

    // Root pembungkus
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // PERUBAHAN: Menggunakan LazyColumn secara global untuk seluruh halaman
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)
        ) {
            // ==========================================
            // BAGIAN 1: HEADER & GRAFIK (Bisa di-scroll)
            // ==========================================
            item {
                Spacer(modifier = Modifier.height(24.dp))

                // Header Mode Seleksi / Normal
                if (isSelectionMode) {
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { isSelectionMode = false; selectedItems = emptySet() }) { Icon(Icons.Default.Close, contentDescription = "Tutup", tint = MaterialTheme.colorScheme.onSurface) }
                            Text("${selectedItems.size} Terpilih", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Row {
                            IconButton(onClick = { selectedItems = if (selectedItems.size == filteredExpenses.size) emptySet() else filteredExpenses.toSet() }) { Icon(Icons.Default.SelectAll, contentDescription = "Pilih Semua", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                            if (selectedItems.isNotEmpty()) {
                                IconButton(onClick = { showDeleteDialog = true }) { Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFE57373)) }
                            }
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFF64B5F6), CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("NOEXDATE", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            }
                            Text("Keuangan", color = MaterialTheme.colorScheme.onSurface, fontSize = 32.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Box Total Pengeluaran & Donut Chart
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surface).padding(24.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("TOTAL PENGELUARAN", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(formatRupiah.format(currentMonthTotal), color = MaterialTheme.colorScheme.onSurface, fontSize = 32.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)

                        Spacer(modifier = Modifier.height(12.dp))

                        // DROPDOWN PEMILIH BULAN
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .clickable { showMonthMenu = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${monthNames[selectedMonth]} $selectedYear", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Bulan", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(
                                expanded = showMonthMenu,
                                onDismissRequest = { showMonthMenu = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                monthNames.forEachIndexed { index, month ->
                                    DropdownMenuItem(
                                        text = { Text("$month $selectedYear", color = MaterialTheme.colorScheme.onSurface) },
                                        onClick = { selectedMonth = index; showMonthMenu = false }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // GRAFIK DONAT
                        if (baseMonthExpenses.isNotEmpty() && currentMonthTotal > 0) {
                            val groupedData = baseMonthExpenses.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.amount } }
                            val sortedData = groupedData.entries.sortedByDescending { it.value }

                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                                    Canvas(modifier = Modifier.size(100.dp)) {
                                        var startAngle = -90f
                                        val strokeWidth = 24f
                                        sortedData.forEachIndexed { index, data ->
                                            val sweepAngle = ((data.value / currentMonthTotal).toFloat() * 360f)
                                            val color = chartColors[index % chartColors.size]
                                            drawArc(color = color, startAngle = startAngle, sweepAngle = sweepAngle, useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round), size = Size(size.width, size.height), topLeft = Offset(0f, 0f))
                                            startAngle += sweepAngle
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(24.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    sortedData.take(4).forEachIndexed { index, data ->
                                        val percentage = ((data.value / currentMonthTotal) * 100).toInt()
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                                            Box(modifier = Modifier.size(10.dp).background(chartColors[index % chartColors.size], CircleShape))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(data.key.take(12), color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, maxLines = 1, modifier = Modifier.weight(1f))
                                            Text("$percentage%", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        } else {
                            Text("Belum ada data di bulan ini", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ==========================================
            // BAGIAN 2: STICKY HEADER (TIDAK IKUT TERGULUNG)
            // ==========================================
            stickyHeader {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background) // Wajib ada background agar item yg lewat di bawahnya tertutup
                        .padding(bottom = 12.dp)
                ) {
                    // UI Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filter Chips
                    LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filterOptions) { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter, color = if (selectedFilter == filter) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, containerColor = MaterialTheme.colorScheme.surface)
                            )
                        }
                    }
                }
            }

            // ==========================================
            // BAGIAN 3: LIST TRANSAKSI (Bisa di-scroll)
            // ==========================================
            if (filteredExpenses.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(if (searchQuery.isNotBlank()) "Tidak ada hasil pencarian." else "Belum ada pengeluaran.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, textAlign = TextAlign.Center)
                    }
                }
            } else {
                items(filteredExpenses) { expense ->
                    val isSelected = selectedItems.contains(expense)
                    val timeString = SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID")).format(Date(expense.expenseDate))
                    val surfaceColor = if (isSelected) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.surface

                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(surfaceColor)
                            .combinedClickable(
                                onClick = {
                                    if (isSelectionMode) toggleSelection(expense)
                                    else navController.navigate("expense_detail/${expense.id}")
                                },
                                onLongClick = { isSelectionMode = true; toggleSelection(expense) }
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelectionMode) {
                            Checkbox(checked = isSelected, onCheckedChange = { toggleSelection(expense) }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary, checkmarkColor = MaterialTheme.colorScheme.onPrimary, uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant))
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            val catColor = chartColors[dynamicLabels.indexOf(expense.category).coerceAtLeast(0) % chartColors.size]
                            Box(modifier = Modifier.size(48.dp).background(catColor.copy(alpha=0.1f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Receipt, contentDescription = null, tint = catColor)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(expense.description, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text("${expense.category} • $timeString", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                        Text("- ${formatRupiah.format(expense.amount)}", color = Color(0xFFE57373), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp)) // Jarak antar item
                }
            }

            // Memberikan ruang di bagian bawah agar tidak tertutup Bottom Navigation
            item {
                Spacer(modifier = Modifier.height(170.dp))
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false }, containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Hapus Transaksi?", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = { Text("Transaksi yang dipilih akan dihapus permanen.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = { TextButton(onClick = { viewModel.deleteExpenses(selectedItems.toList()); isSelectionMode = false; selectedItems = emptySet(); showDeleteDialog = false }) { Text("Hapus", color = Color(0xFFE57373), fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Batal", color = MaterialTheme.colorScheme.onSurface) } }
        )
    }
}