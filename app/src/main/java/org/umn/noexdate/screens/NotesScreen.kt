package org.umn.noexdate.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavHostController
import org.umn.noexdate.data.local.entity.NoteEntity
import org.umn.noexdate.utils.AppPreferences
import org.umn.noexdate.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotesScreen(navController: NavHostController, viewModel: NoteViewModel) {
    val context = LocalContext.current
    val prefs = remember { AppPreferences(context) }

    var selectedTab by remember { mutableStateOf(0) } // 0: Calendar, 1: List
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val dynamicNoteLabels = remember {
        prefs.getLabels(
            "note_labels",
            setOf("Note Only", "Task", "Deadline", "Event", "Personal", "Work", "Other")
        )
    }
    val filterOptions = remember(dynamicNoteLabels) { listOf("All") + dynamicNoteLabels }

    val currentCalendar = remember { Calendar.getInstance() }
    var displayedMonth by remember { mutableStateOf(currentCalendar.get(Calendar.MONTH)) }
    var displayedYear by remember { mutableStateOf(currentCalendar.get(Calendar.YEAR)) }
    var selectedDate by remember { mutableStateOf(currentCalendar.get(Calendar.DAY_OF_MONTH)) }

    val notesList by viewModel.allNotes.collectAsState()

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf(setOf<NoteEntity>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    fun toggleSelection(note: NoteEntity) {
        selectedItems = if (selectedItems.contains(note)) selectedItems - note else selectedItems + note
        if (selectedItems.isEmpty()) isSelectionMode = false
    }

    val eventDates = remember(notesList, displayedMonth, displayedYear) {
        val dates = mutableSetOf<Int>()
        val cal = Calendar.getInstance()

        notesList.filter { it.showOnCalendar }.forEach { note ->
            cal.timeInMillis = note.deadlineAt ?: note.createdAt
            if (cal.get(Calendar.MONTH) == displayedMonth && cal.get(Calendar.YEAR) == displayedYear) {
                dates.add(cal.get(Calendar.DAY_OF_MONTH))
            }
        }
        //ini kalau mau di tampili titik di calender untuk makanan
//        pantryList.forEach { item ->
//            cal.timeInMillis = item.expirationDate
//            if (cal.get(Calendar.MONTH) == displayedMonth && cal.get(Calendar.YEAR) == displayedYear) {
//                dates.add(cal.get(Calendar.DAY_OF_MONTH))
//            }
//        }
        dates.toList()
    }

    // Menggunakan LazyColumn secara global untuk mendukung sticky header & scroll mulus
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)
        ) {
            // ==========================================
            // BAGIAN 1: HEADER & KALENDER (Bisa di-scroll)
            // ==========================================
            item {
                Spacer(modifier = Modifier.height(24.dp))

                if (isSelectionMode && selectedTab == 1) {
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { isSelectionMode = false; selectedItems = emptySet() }) { Icon(Icons.Default.Close, contentDescription = "Tutup", tint = MaterialTheme.colorScheme.onSurface) }
                            Text("${selectedItems.size} Terpilih", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Row {
                            if (selectedItems.isNotEmpty()) {
                                IconButton(onClick = { showDeleteDialog = true }) { Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFE57373)) }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                } else {
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).padding(4.dp)) {
                        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (selectedTab == 0) MaterialTheme.colorScheme.background else Color.Transparent).clickable { selectedTab = 0; isSelectionMode = false }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text("Calendar", color = if (selectedTab == 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (selectedTab == 1) MaterialTheme.colorScheme.background else Color.Transparent).clickable { selectedTab = 1 }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text("List", color = if (selectedTab == 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // ==========================================
            // BAGIAN 2: STICKY SEARCH BAR (Menempel di atas)
            // ==========================================
            stickyHeader {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(bottom = 12.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari judul atau isi catatan...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
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
                }
            }

            // ==========================================
            // BAGIAN 3: KONTEN UTAMA (Tab Calendar vs List)
            // ==========================================
            if (selectedTab == 0) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    val calHeader = Calendar.getInstance().apply { set(displayedYear, displayedMonth, 1) }
                    val monthName = SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(calHeader.time)

                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .pointerInput(Unit) {
                            var swipeThreshold = 0f
                            detectHorizontalDragGestures(
                                onDragEnd = { swipeThreshold = 0f }
                            ) { change, dragAmount ->
                                change.consume()
                                swipeThreshold += dragAmount
                                if (swipeThreshold > 80) { // Geser Kanan (Bulan Sebelumnya)
                                    if (displayedMonth == 0) { displayedMonth = 11; displayedYear -= 1 } else displayedMonth -= 1
                                    swipeThreshold = 0f
                                } else if (swipeThreshold < -80) { // Geser Kiri (Bulan Selanjutnya)
                                    if (displayedMonth == 11) { displayedMonth = 0; displayedYear += 1 } else displayedMonth += 1
                                    swipeThreshold = 0f
                                }
                            }
                        }
                        .padding(20.dp))
                    {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Prev", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.clickable {
                                if (displayedMonth == 0) { displayedMonth = 11; displayedYear -= 1 } else displayedMonth -= 1
                            })
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(monthName.uppercase(), color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("TODAY", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, modifier = Modifier.clickable {
                                    displayedMonth = currentCalendar.get(Calendar.MONTH)
                                    displayedYear = currentCalendar.get(Calendar.YEAR)
                                    selectedDate = currentCalendar.get(Calendar.DAY_OF_MONTH)
                                }.padding(top = 4.dp))
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.clickable {
                                if (displayedMonth == 11) { displayedMonth = 0; displayedYear += 1 } else displayedMonth += 1
                            })
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        val daysInMonth = calHeader.getActualMaximum(Calendar.DAY_OF_MONTH)
                        val startDayOfWeek = calHeader.get(Calendar.DAY_OF_WEEK) - 1
                        val gridItems = MutableList<Int?>(if (startDayOfWeek == 0) 6 else startDayOfWeek - 1) { null }
                        for (i in 1..daysInMonth) gridItems.add(i)

                        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.height(280.dp), userScrollEnabled = false) {
                            items(gridItems.size) { index ->
                                val date = gridItems[index]
                                if (date == null) {
                                    Box(modifier = Modifier.aspectRatio(1f))
                                } else {
                                    val isSelected = date == selectedDate
                                    val hasEvent = eventDates.contains(date)
                                    Box(
                                        modifier = Modifier.aspectRatio(1f).padding(4.dp).clip(RoundedCornerShape(12.dp)).background(if (isSelected) MaterialTheme.colorScheme.outline else Color.Transparent).clickable { selectedDate = date },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(date.toString(), color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                            if (hasEvent) Box(modifier = Modifier.size(4.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                val filteredTodayNotes = notesList.filter { it.showOnCalendar }.filter { note ->
                    val c = Calendar.getInstance().apply { timeInMillis = note.deadlineAt ?: note.createdAt }
                    val matchDate = c.get(Calendar.DAY_OF_MONTH) == selectedDate && c.get(Calendar.MONTH) == displayedMonth && c.get(Calendar.YEAR) == displayedYear
                    val matchSearch = searchQuery.isBlank() || note.title.contains(searchQuery, true) || note.content.contains(searchQuery, true)
                    matchDate && matchSearch
                }

                if (filteredTodayNotes.isEmpty()) {
                    item {
                        Text(if (searchQuery.isNotBlank()) "Tidak ada acara untuk kueri \"$searchQuery\"." else "Tidak ada acara pada hari ini.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.padding(top = 16.dp))
                    }
                } else {
                    items(filteredTodayNotes) { note ->
                        NoteItem(date = selectedDate.toString(), title = note.title, subtitle = note.noteType, isAlert = false, isSelected = false, isSelectionMode = false,
                            onClick = { navController.navigate("catatan_detail/${note.id}") }, onLongClick = {}
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

            } else {
                // Tampilan List & Filter Chips
                val filteredNotes = notesList.filter { note ->
                    val matchFilter = selectedFilter == "All" || note.noteType == selectedFilter
                    val matchSearch = searchQuery.isBlank() || note.title.contains(
                        searchQuery,
                        true
                    ) || note.content.contains(searchQuery, true)
                    matchFilter && matchSearch
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filterOptions) { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = {
                                    Text(
                                        filter,
                                        color = if (selectedFilter == filter) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
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
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (filteredNotes.isEmpty()) {
                    item {
                        Text(
                            if (searchQuery.isNotBlank()) "Pencarian \"$searchQuery\" tidak ditemukan." else "Tidak ada catatan.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                } else {
                    items(filteredNotes) { note ->
                        val isSelected = selectedItems.contains(note)

                        val displayDateMillis =
                            if (note.noteType != "Note Only" && note.deadlineAt != null) {
                                note.deadlineAt!!
                            } else {
                                note.createdAt
                            }

                        val dateStr = SimpleDateFormat(
                            "dd",
                            Locale("id", "ID")
                        ).format(Date(displayDateMillis))

                        NoteItem(
                            date = dateStr,
                            title = note.title,
                            subtitle = note.noteType,
                            isAlert = false,
                            isSelected = isSelected,
                            isSelectionMode = isSelectionMode,
                            onClick = {
                                if (isSelectionMode) toggleSelection(note) else navController.navigate(
                                    "catatan_detail/${note.id}"
                                )
                            },
                            onLongClick = { isSelectionMode = true; toggleSelection(note) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(170.dp))
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false }, containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Hapus ${selectedItems.size} Catatan?", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = { Text("Catatan yang dipilih akan dihapus permanen.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteNotes(selectedItems.toList()); isSelectionMode = false; selectedItems = emptySet(); showDeleteDialog = false }) { Text("Hapus", color = Color(0xFFE57373), fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Batal", color = MaterialTheme.colorScheme.onSurface) } }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteItem(date: String, title: String, subtitle: String, isAlert: Boolean, isSelected: Boolean, isSelectionMode: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val bgColor = if (isAlert) Color(0xFFE57373).copy(alpha = 0.1f) else if (isSelected) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(bgColor)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Checkbox(checked = isSelected, onCheckedChange = null, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary, checkmarkColor = MaterialTheme.colorScheme.onPrimary, uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant))
            Spacer(modifier = Modifier.width(16.dp))
        } else {
            Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.onSurface, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) { Text(date, color = MaterialTheme.colorScheme.background, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.width(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title.ifEmpty { "Tanpa Judul" }, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}