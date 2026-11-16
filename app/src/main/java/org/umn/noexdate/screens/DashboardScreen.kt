package org.umn.noexdate.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.umn.noexdate.R
import org.umn.noexdate.viewmodel.ExpenseViewModel
import org.umn.noexdate.viewmodel.PantryViewModel
import org.umn.noexdate.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DashboardScreen(navController: NavHostController, expenseViewModel: ExpenseViewModel, pantryViewModel: PantryViewModel, noteViewModel: NoteViewModel) {
    val expenses by expenseViewModel.allExpenses.collectAsState()
    val notesList by noteViewModel.allNotes.collectAsState()
    val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }

    // Warna chart (Sama dengan Expense)
    val chartColors = listOf(Color(0xFFE57373), Color(0xFF64B5F6), MaterialTheme.colorScheme.primary, Color(0xFF81C784), Color(0xFFBA68C8), Color(0xFFFFB74D))
    val pantryItems by pantryViewModel.activePantryItems.collectAsState()

    //Filter pengurangan bulan ini
    val currentMonthExpenses = remember(expenses) {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonthIndex = Calendar.getInstance().get(Calendar.MONTH)
        expenses.filter { exp ->
            val cal = Calendar.getInstance().apply { timeInMillis = exp.expenseDate }
            cal.get(Calendar.YEAR) == currentYear && cal.get(Calendar.MONTH) == currentMonthIndex
        }
    }
    val eventDotColor = Color(0xFF3B82F6)
    val deadlineDotColor = Color(0xFFF59E0B)
    val pantryDotColor = Color(0xFFEF4444)

    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    fun isSameDay(millis1: Long, millis2: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = millis1 }
        val c2 = Calendar.getInstance().apply { timeInMillis = millis2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    val nowCal = remember { Calendar.getInstance() }
    val todayExpenses = remember(expenses) { expenses.filter { isSameDay(it.expenseDate, nowCal.timeInMillis) } }
    val todayTotal = todayExpenses.sumOf { it.amount }
    val monthTotal = currentMonthExpenses.sumOf { it.amount }
    val currentMonthShort = SimpleDateFormat("MMM", Locale("en", "US")).format(nowCal.time)

    val weekDays = remember {
        val cal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
        List(7) { offset ->
            (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }.timeInMillis
        }
    }

    Column(modifier = Modifier.fillMaxSize().background((MaterialTheme.colorScheme.background)).padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.outline, CircleShape), contentAlignment = Alignment.Center) { Icon(painter = painterResource(id = R.drawable.logo), contentDescription = "L", tint = Color.Unspecified, modifier = Modifier.size(20.dp)) }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("NOEXDATE", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    Text("Halo Rafael", color = MaterialTheme.colorScheme.onSurface, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeaderIcon(Icons.Default.Person, onClick = { navController.navigate("settings") })
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

// 1. EXPENSES SECTION
        Text("Expenses", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Today", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(formatRupiah.format(todayTotal), color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("${todayExpenses.size} transactions", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
                Box(modifier = Modifier.fillMaxHeight().padding(horizontal = 12.dp).width(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)))
                Column(modifier = Modifier.weight(1f)) {
                    Text("$currentMonthShort so far", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(formatRupiah.format(monthTotal), color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("${currentMonthExpenses.size} transactions", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val groupedData = currentMonthExpenses.groupBy { it.category }.mapValues { e -> e.value.sumOf { it.amount } }.entries.filter { it.value > 0 }.sortedByDescending { it.value }
            if (groupedData.isNotEmpty() && monthTotal > 0) {
                Row(modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    groupedData.forEachIndexed { index, entry ->
                        val weight = (entry.value / monthTotal).toFloat().coerceAtLeast(0.03f)
                        Box(modifier = Modifier.weight(weight).fillMaxHeight().clip(CircleShape).background(chartColors[index % chartColors.size]))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    groupedData.forEachIndexed { index, entry ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).background(chartColors[index % chartColors.size], CircleShape))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(entry.key, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(formatRupiah.format(entry.value), color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                Text("Belum ada pengeluaran bulan ini.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.background).clickable { navController.navigate("expense_detail") }.padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log an expense", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 2. WEEKLY CALENDAR STRIP
        val headerMonthYear = SimpleDateFormat("MMMM yyyy", Locale("en", "US")).format(Date(selectedDateMillis))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(headerMonthYear, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Open calendar", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { navController.navigate("calendar") })
        }
        Spacer(modifier = Modifier.height(14.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            weekDays.forEach { dayMillis ->
                val isSelected = isSameDay(dayMillis, selectedDateMillis)
                val isToday = isSameDay(dayMillis, nowCal.timeInMillis)
                val dayName = SimpleDateFormat("EEE", Locale("en", "US")).format(Date(dayMillis))
                val dayNumber = SimpleDateFormat("dd", Locale("en", "US")).format(Date(dayMillis))
                val hasEvent = notesList.any { it.showOnCalendar && it.noteType == "Event" && isSameDay(it.deadlineAt ?: it.createdAt, dayMillis) }
                val hasDeadline = notesList.any { it.showOnCalendar && it.noteType != "Event" && isSameDay(it.deadlineAt ?: it.createdAt, dayMillis) }
                val hasPantryExpiry = pantryItems.any { isSameDay(it.expirationDate, dayMillis) }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .border(
                            width = if (isToday) 2.dp else 1.dp, // Border lebih tebal untuk hari ini
                            color = if (isToday || isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedDateMillis = dayMillis }
                        .padding(vertical = 10.dp)
                ) {
                    Text(dayName, color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(dayNumber, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.height(5.dp)) {
                        if (hasEvent) Box(modifier = Modifier.size(5.dp).background(eventDotColor, CircleShape))
                        if (hasDeadline) Box(modifier = Modifier.size(5.dp).background(deadlineDotColor, CircleShape))
                        if (hasPantryExpiry) Box(modifier = Modifier.size(5.dp).background(pantryDotColor, CircleShape))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            LegendDot(eventDotColor, "Event")
            LegendDot(deadlineDotColor, "Note deadline")
            LegendDot(pantryDotColor, "Pantry expiry")
        }
        Spacer(modifier = Modifier.height(22.dp))

        // 3. QUICK ACTION SHORTCUTS
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            QuickShortcutButton(Icons.Default.EditNote, "Note", MaterialTheme.colorScheme.surface, Color(0xFFF59E0B)) { navController.navigate("catatan_detail") }
            QuickShortcutButton(Icons.Default.Event, "Event", MaterialTheme.colorScheme.surface, Color(0xFF3B82F6)) { navController.navigate("catatan_detail") }
            QuickShortcutButton(Icons.Default.AccountBalanceWallet, "Expense", MaterialTheme.colorScheme.surface, Color(0xFF7E57C2)) { navController.navigate("expense_detail") }
            QuickShortcutButton(Icons.Default.Kitchen, "Pantry", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.primary) { navController.navigate("pantry_tambah") }
            QuickShortcutButton(Icons.Default.CameraAlt, "Scan EXP", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary) { navController.navigate("pantry_tambah") }
        }
        Spacer(modifier = Modifier.height(28.dp))

        // 4. TODAY'S AGENDA
        val agendaTitle = if (isSameDay(selectedDateMillis, nowCal.timeInMillis)) "Today's agenda" else "Agenda • ${SimpleDateFormat("dd MMM", Locale("en", "US")).format(Date(selectedDateMillis))}"
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Today's agenda", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("See all", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { navController.navigate("calendar") })
        }
        Spacer(modifier = Modifier.height(12.dp))

        val todayMillis = System.currentTimeMillis()
        val dayPantryItems = pantryItems.filter { isSameDay(it.expirationDate, todayMillis) }
        val dayNotes = notesList.filter { it.showOnCalendar && isSameDay(it.deadlineAt ?: it.createdAt, todayMillis) }
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(24.dp)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (dayPantryItems.isEmpty() && dayNotes.isEmpty()) {
                Text("Tidak ada jadwal atau makanan kedaluwarsa pada tanggal ini.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                dayPantryItems.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(pantryDotColor.copy(alpha = 0.12f)).clickable { navController.navigate("pantry_detail/${item.id}") }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(38.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Kitchen, contentDescription = null, tint = pantryDotColor, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${item.name} expires", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("All day • ${item.category} • Pantry", color = pantryDotColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                }

                dayNotes.forEach { note ->
                    val isEvent = note.noteType == "Event"
                    val barColor = if (isEvent) eventDotColor else deadlineDotColor
                    val timeStr = SimpleDateFormat("HH:mm", Locale("id", "ID")).format(Date(note.deadlineAt ?: note.createdAt))

                    Row(modifier = Modifier.fillMaxWidth().clickable { navController.navigate("catatan_detail/${note.id}") }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(44.dp)) {
                            Text(timeStr, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(if (isEvent) "Event" else "Due", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(modifier = Modifier.width(3.dp).height(36.dp).clip(CircleShape).background(barColor))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(note.title.ifEmpty { "Tanpa Judul" }, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${note.noteType} • ${note.reminderConfiguration}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(7.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
    }
}

@Composable
private fun QuickShortcutButton(icon: ImageVector, label: String, containerColor: Color, contentColor: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier.size(54.dp).clip(RoundedCornerShape(16.dp)).background(containerColor).border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun HeaderIcon(icon: ImageVector, onClick: () -> Unit = {}) {
    Box(modifier = Modifier.size(36.dp).border(1.dp, MaterialTheme.colorScheme.outline, CircleShape).background(MaterialTheme.colorScheme.surface, CircleShape).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}