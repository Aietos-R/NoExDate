package org.umn.noexdate

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.umn.noexdate.data.local.database.NoexDatabase
import org.umn.noexdate.data.repository.ExpenseRepository
import org.umn.noexdate.data.repository.NoteRepository
import org.umn.noexdate.data.repository.PantryRepository
import org.umn.noexdate.screens.*
import org.umn.noexdate.ui.theme.*
import org.umn.noexdate.utils.AppPreferences
import org.umn.noexdate.viewmodel.*
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import org.umn.noexdate.utils.AppLogger
import org.umn.noexdate.worker.DailyReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = AppPreferences(this)
        scheduleDailyReminder(this)
        AppLogger.init(this)
        AppLogger.logActivity("APP_LIFECYCLE", "Aplikasi dibuka (onCreate)")
        setContent {
            var currentTheme by remember { mutableStateOf(prefs.getThemeMode()) }

            DisposableEffect(Unit) {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    // Menangkap key "theme_mode" sesuai dengan isi AppPreferences
                    if (key == "theme_mode") {
                        currentTheme = prefs.getThemeMode()
                    }
                }
                // Menggunakan prefs.prefs setelah private-nya dihapus
                prefs.prefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose {
                    prefs.prefs.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }

// 2. Bungkus seluruh aplikasi dengan NoExDateTheme bawaan project Anda
            NoExDateTheme(themeMode = currentTheme) {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }

// 1. Mencatat setiap perpindahan halaman di background
                DisposableEffect(navController) {
                    val listener = androidx.navigation.NavController.OnDestinationChangedListener { _, destination, _ ->
                        AppLogger.logActivity("NAVIGATION", "Berpindah ke halaman: ${destination.route}")
                    }
                    navController.addOnDestinationChangedListener(listener)
                    onDispose { navController.removeOnDestinationChangedListener(listener) }
                }

// 2. Mendengarkan semua hasil operasi (Berhasil / Gagal) secara global
                LaunchedEffect(Unit) {
                    AppLogger.operationEvents.collect { result ->
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(
                            message = result.message,
                            duration = if (result.isError) SnackbarDuration.Long else SnackbarDuration.Short
                        )
                    }
                }

                var isMenuExpanded by remember { mutableStateOf(false) }

                val context = LocalContext.current
                val database = remember { NoexDatabase.getDatabase(context) }

                val noteRepository = remember { NoteRepository(database.noteDao()) }
                val noteViewModel: NoteViewModel = viewModel(factory = NoteViewModelFactory(noteRepository))

                val pantryRepository = remember { PantryRepository(database.pantryDao()) }
                val pantryViewModel: PantryViewModel = viewModel(factory = PantryViewModelFactory(pantryRepository))

                val expenseRepository = remember { ExpenseRepository(database.expenseDao()) }
                val expenseViewModel: ExpenseViewModel = viewModel(factory = ExpenseViewModelFactory(expenseRepository))

                // Pantau rute saat ini untuk indikator warna Navbar
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = { CustomBottomNavigation(navController, currentRoute) },
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState) { data ->
                            val isError = data.visuals.message.contains("gagal:", ignoreCase = true)
                            Snackbar(
                                snackbarData = data,
                                containerColor = if (isError) Color(0xFFE57373) else MaterialTheme.colorScheme.primary,
                                contentColor = if (isError) Color.White else MaterialTheme.colorScheme.onPrimary,
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        AppNavigation(navController, noteViewModel, pantryViewModel, expenseViewModel)
                        if (isMenuExpanded) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.2f))
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { isMenuExpanded = false }
                            )
                        }
                        // 1. KOTAK MENU SPEED DIAL (Yang muncul saat tombol ditekan)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 96.dp, end = 16.dp) // Ditaruh lebih tinggi agar berada tepat di atas tombol Plus
                        ) {
                            AnimatedVisibility(
                                visible = isMenuExpanded,
                                enter = fadeIn(tween(200)) + slideInVertically(tween(200), initialOffsetY = { it / 2 }),
                                exit = fadeOut(tween(200)) + slideOutVertically(tween(200), targetOffsetY = { it / 2 })
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.End, // Teks rata kanan agar sejajar
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                                        .padding(16.dp)
                                ) {
                                    SpeedDialItem(Icons.Default.Edit, "Catatan", onClick = { isMenuExpanded = false; navController.navigate("catatan_detail") })
                                    SpeedDialItem(Icons.Default.Receipt, "Transaksi", onClick = { isMenuExpanded = false; navController.navigate("expense_detail") })
                                    SpeedDialItem(Icons.Default.CameraAlt, "Scan Makanan", onClick = { isMenuExpanded = false; navController.navigate("pantry_tambah") })
                                }
                            }
                        }

                        // 2. KOTAK TOMBOL PLUS (Glassmorphism di pojok kanan bawah)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 16.dp, end = 16.dp)
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .clickable { isMenuExpanded = !isMenuExpanded },
                            contentAlignment = Alignment.Center
                        ) {
                            // Ikon Plus atau Close ditaruh di sini
                            Icon(
                                imageVector = if (isMenuExpanded) Icons.Default.Close else Icons.Default.Add,
                                contentDescription = "Tambah",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    noteViewModel: NoteViewModel,
    pantryViewModel: PantryViewModel,
    expenseViewModel: ExpenseViewModel // Menerima ViewModel Keuangan
) {
    NavHost(navController = navController, startDestination = "beranda") {
        // Dashboard membutuhkan expenseViewModel untuk membaca total pengeluaran
        composable("beranda") { DashboardScreen(navController, expenseViewModel, pantryViewModel, noteViewModel) }
        composable("settings") { SettingsScreen(navController) }
        composable("edit_profile") { EditProfileScreen(navController) }
        composable("notifikasi") { NotificationSettingsScreen(navController) }
        composable("labels") { LabelSettingsScreen(navController) }
        composable("theme_settings") { ThemeSettingsScreen(navController) }

        //Halaman calender
        composable("calendar") { NotesScreen(navController, noteViewModel) }

        // Halaman Note
        composable("catatan") { ComingSoonScreen(navController /* noteViewModel, pantryViewModel*/) }
        composable("catatan_detail") { NoteDetailScreen(navController, noteViewModel, noteId = -1) }
        composable("catatan_detail/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull() ?: -1
            NoteDetailScreen(navController, noteViewModel, noteId)
        }

        //Halaman pantry
        composable("pantry") { PantryScreen(navController, pantryViewModel) }
        composable("pantry_tambah") { PantryDetailScreen(navController, pantryViewModel, pantryId = -1) }
        composable("pantry_detail/{itemId}") { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")?.toIntOrNull() ?: -1
            PantryDetailScreen(navController, pantryViewModel, pantryId = itemId)
        }

        // Rute Keuangan
        composable("expense") { ExpenseScreen(navController, expenseViewModel) }
        composable(
            route = "expense_detail/{expenseId}",
            arguments = listOf(androidx.navigation.navArgument("expenseId") { type = androidx.navigation.NavType.IntType })
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getInt("expenseId") ?: -1
            ExpenseDetailScreen(navController = navController, viewModel = expenseViewModel, expenseId = expenseId)
        }

        // Rute untuk Tambah Baru
        composable("expense_detail") {
            ExpenseDetailScreen(navController = navController, viewModel = expenseViewModel, expenseId = -1)
        }

        composable("coming_soon") { ComingSoonScreen(navController) }
    }
}

// NAVBAR BARU: Nempel di bawah & berisi 5 ikon
@Composable
fun CustomBottomNavigation(navController: NavHostController, currentRoute: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface) // Solid sesuai tema
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItem(Icons.Default.Home, "Home", currentRoute == "beranda") { navController.navigate("beranda") }
        NavItem(Icons.Default.CalendarMonth, "Calendar", currentRoute == "calendar") { navController.navigate("calendar") }
        NavItem(Icons.Default.EditNote, "Notes", currentRoute == "catatan") { navController.navigate("catatan") }
        NavItem(Icons.Default.AccountBalanceWallet, "Expenses", currentRoute == "expense") { navController.navigate("expense") }
        NavItem(Icons.Default.Kitchen, "Pantry", currentRoute == "pantry") { navController.navigate("pantry") }
    }
}

// DESAIN NAVITEM BARU: Kapsul di belakang ikon jika aktif
@Composable
fun NavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(bgColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun SpeedDialItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    // Saya balik urutannya (Teks duluan baru Ikon) agar ikon sejajar dengan tombol Plus di kanan
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .clip(RoundedCornerShape(20))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp))
        Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
        }
    }
}

fun scheduleDailyReminder(context: android.content.Context) {
    val currentDate = Calendar.getInstance()
    val dueDate = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 12) // Berjalan otomatis jam 12:00 siang
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }

    if (dueDate.before(currentDate)) {
        dueDate.add(Calendar.HOUR_OF_DAY, 24)
    }

    val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis

    val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
        .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "NoexDateDailyReminder",
        ExistingPeriodicWorkPolicy.UPDATE,
        dailyWorkRequest
    )
}