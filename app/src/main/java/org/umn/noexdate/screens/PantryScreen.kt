package org.umn.noexdate.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.umn.noexdate.data.local.entity.PantryEntity
import org.umn.noexdate.viewmodel.PantryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen( navController: NavHostController, viewModel: PantryViewModel) {
    val pantryItems by viewModel.activePantryItems.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    val filterOptions = listOf("All", "Expiring Soon", "Expired", "Fresh")

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf(setOf<PantryEntity>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    fun toggleSelection(item: PantryEntity) {
        selectedItems = if (selectedItems.contains(item)) selectedItems - item else selectedItems + item
        if (selectedItems.isEmpty()) isSelectionMode = false
    }

    val now = System.currentTimeMillis()
    val sevenDays = 7L * 24 * 60 * 60 * 1000
    val filteredPantry = pantryItems.filter { item ->
        val diff = item.expirationDate - now
        val matchFilter = when (selectedFilter) {
            "Expiring Soon" -> diff in 0..sevenDays
            "Expired" -> diff < 0
            "Fresh" -> diff > sevenDays
            else -> true
        }
        val matchSearch = searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true)
        matchFilter && matchSearch
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)
        ) {
            // ==========================================
            // BAGIAN 1: HEADER (Bisa di-scroll)
            // ==========================================
            item {
                Spacer(modifier = Modifier.height(24.dp))

                if (isSelectionMode) {
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { isSelectionMode = false; selectedItems = emptySet() }) { Icon(Icons.Default.Close, contentDescription = "Tutup", tint = MaterialTheme.colorScheme.onSurface) }
                            Text("${selectedItems.size} Terpilih", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Row {
                            IconButton(onClick = { selectedItems = if (selectedItems.size == filteredPantry.size) emptySet() else filteredPantry.toSet() }) { Icon(Icons.Default.SelectAll, contentDescription = "Pilih Semua", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                            if (selectedItems.isNotEmpty()) IconButton(onClick = { showDeleteDialog = true }) { Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFE57373)) }
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("NOEXDATE", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            }
                            Text("Pantry", color = MaterialTheme.colorScheme.onSurface, fontSize = 32.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ==========================================
            // BAGIAN 2: STICKY SEARCH BAR & FILTER CHIPS
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
                        placeholder = { Text("Cari makanan atau item...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
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
            // BAGIAN 3: DAFTAR PANTRY ITEMS
            // ==========================================
            if (filteredPantry.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(if (searchQuery.isNotBlank()) "Tidak ada makanan yang sesuai kueri." else "Tidak ada item yang sesuai filter.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, textAlign = TextAlign.Center)
                    }
                }
            } else {
                items(filteredPantry) { item ->
                    val isSelected = selectedItems.contains(item)
                    val diff = item.expirationDate - System.currentTimeMillis()
                    val daysLeft = (diff / (1000 * 60 * 60 * 24)).toInt()
                    val statusColor = if (daysLeft <= 3) Color(0xFFE57373) else MaterialTheme.colorScheme.background
                    val statusText = if (daysLeft < 0) "Expired" else "$daysLeft days left"
                    val surfaceColor = if (isSelected) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.surface
                    val exactDateStr = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(
                        Date(item.expirationDate)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(surfaceColor)
                            .combinedClickable
                                (onClick = {
                                    if (isSelectionMode) { toggleSelection(item)
                                    } else {
                                        navController.navigate("pantry_detail/${item.id}")
                                    }
                                }, onLongClick = { isSelectionMode = true; toggleSelection(item) })
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelectionMode) {
                            Checkbox(checked = isSelected, onCheckedChange = { toggleSelection(item) }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary, checkmarkColor = MaterialTheme.colorScheme.onPrimary, uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant))
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.onSurface, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Kitchen, contentDescription = null, tint = MaterialTheme.colorScheme.background) }
                            Spacer(modifier = Modifier.width(16.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text("${item.category} • ${item.quantity}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .background(if (daysLeft <= 3) statusColor else MaterialTheme.colorScheme.onSurface, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp) // Padding Box dikecilkan sedikit
                            ) {
                                Text(statusText, color = if (daysLeft <= 3) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.background, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(2.dp)) // Jarak antara Box dan Tanggal

                            Text(
                                text = exactDateStr,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
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
            title = { Text("Hapus ${selectedItems.size} Item?", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = { Text("Makanan ini akan dihapus dari Pantry secara permanen.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = { TextButton(onClick = { viewModel.deletePantryItems(selectedItems.toList()); isSelectionMode = false; selectedItems = emptySet(); showDeleteDialog = false }) { Text("Hapus", color = Color(0xFFE57373), fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Batal", color = MaterialTheme.colorScheme.onSurface) } }
        )
    }
}