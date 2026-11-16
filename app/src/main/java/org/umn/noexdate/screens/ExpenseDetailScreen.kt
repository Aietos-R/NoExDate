package org.umn.noexdate.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.umn.noexdate.utils.AppPreferences
import org.umn.noexdate.viewmodel.ExpenseViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

// PEMBARUAN: VisualTransformation untuk format angka otomatis dengan pemisah ribuan
class CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text.filter { it.isDigit() }
        var formattedText = ""

        if (originalText.isNotEmpty()) {
            try {
                val symbols = DecimalFormatSymbols(Locale("id", "ID"))
                val formatter = DecimalFormat("#,###", symbols)
                formattedText = formatter.format(originalText.toLong())
            } catch (e: Exception) {
                formattedText = originalText
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (originalText.isEmpty()) return 0
                val lengthDiff = formattedText.length - originalText.length
                return offset + lengthDiff
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (formattedText.isEmpty()) return 0
                val commas = formattedText.substring(0, offset.coerceAtMost(formattedText.length)).count { it == '.' || it == ',' }
                return (offset - commas).coerceIn(0, originalText.length)
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(navController: NavHostController, viewModel: ExpenseViewModel, expenseId: Int = -1) {
    val context = LocalContext.current
    val prefs = remember { AppPreferences(context) }

    // PEMBARUAN: Jika expenseId diberikan, ambil data transaksi yang akan diedit
    val existingExpense = if (expenseId != -1) {
        viewModel.allExpenses.collectAsState().value.find { it.id == expenseId }
    } else null

    val categoryList = remember { prefs.getLabels("expense_labels", setOf("Makanan", "Transportasi", "Hiburan", "Belanja", "Lainnya")) }

    // State di-initialize dengan data lama jika mode edit aktif
    var amount by remember { mutableStateOf(existingExpense?.amount?.toLong()?.toString() ?: "") }
    var category by remember { mutableStateOf(existingExpense?.category ?: if (categoryList.isNotEmpty()) categoryList.first() else "Lainnya") }
    var description by remember { mutableStateOf(existingExpense?.description ?: "") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(20.dp)) {
        Spacer(modifier = Modifier.height(24.dp))

        // --- HEADER ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surface, CircleShape).clickable { navController.popBackStack() }, contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(if (existingExpense != null) "Edit Pengeluaran" else "Catat Pengeluaran", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (existingExpense != null) {
                    Box(modifier = Modifier.size(40.dp).background(Color(0xFFE57373), CircleShape).clickable { showDeleteDialog = true }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, CircleShape).clickable {
                    val rawAmount = amount.filter { it.isDigit() }
                    if (rawAmount.isNotBlank() && description.isNotBlank()) {
                        val parsedAmount = rawAmount.toDoubleOrNull() ?: 0.0

                        // PEMBARUAN: Simpan baru atau update data lama
                        if (existingExpense != null) {
                            val updatedExpense = existingExpense.copy(amount = parsedAmount, category = category, description = description)
                            // Anggap saja ada fungsi updateExpense di ViewModel
                            // viewModel.updateExpense(updatedExpense)
                            viewModel.deleteExpenses(listOf(existingExpense))
                            viewModel.addExpense(parsedAmount, category, description)
                        } else {
                            viewModel.addExpense(parsedAmount, category, description)
                        }

                        navController.popBackStack()
                    } else {
                        showError = true
                    }
                }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Check, contentDescription = "Simpan", tint = MaterialTheme.colorScheme.background)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        if (showError) {
            Text("Harap isi nominal dan deskripsi", color = Color(0xFFE57373), fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))
        }

        // --- FORM INPUT ---
        Text("NOMINAL (RP)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = amount,
            onValueChange = { newValue ->
                // Hanya mengizinkan input angka
                if (newValue.all { it.isDigit() }) {
                    amount = newValue
                    showError = false
                }
            },
            placeholder = { Text("Cth: 25000", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            visualTransformation = CurrencyVisualTransformation(), // Menerapkan format 150.000 otomatis
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.outline, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.onSurface, focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp)
        )

        Text("DESKRIPSI", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = description,
            onValueChange = { description = it; showError = false },
            placeholder = { Text("Cth: Nasi Goreng Kantin", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.outline, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.onSurface, focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp)
        )

        // --- KATEGORI DINAMIS ---
        Text("KATEGORI PENGELUARAN", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        LazyRow(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categoryList) { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { category = cat },
                    label = { Text(cat, color = if (category == cat) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        }
        Spacer(modifier = Modifier.height(170.dp))
    }

    if (showDeleteDialog && existingExpense != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false }, containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Hapus Transaksi?", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = { Text("Transaksi ini akan dihapus secara permanen.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteExpenses(listOf(existingExpense)); navController.popBackStack() }) {
                    Text("Hapus", color = Color(0xFFE57373), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Batal", color = MaterialTheme.colorScheme.onSurface) } }
        )
    }
}