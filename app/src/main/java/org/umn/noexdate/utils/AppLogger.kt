package org.umn.noexdate.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Model hasil operasi untuk ditampilkan ke pengguna
sealed class OperationResult(val message: String, val isError: Boolean) {
    class Success(message: String) : OperationResult(message, isError = false)
    class Failure(action: String, reason: String) : OperationResult("$action gagal: $reason", isError = true)
}

object AppLogger {
    private const val TAG = "NoExDateLog"
    private const val LOG_FILE_NAME = "noexdate_activity.log"
    private const val MAX_LOG_SIZE_BYTES = 2 * 1024 * 1024 // Maksimal 2 MB agar memori aman

    // Scope khusus background thread (IO) agar tidak mengganggu performa UI
    private val logScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var logFile: File? = null

    // Stream global untuk memicu notifikasi (Snackbar) di layar
    private val _operationEvents = MutableSharedFlow<OperationResult>(extraBufferCapacity = 10)
    val operationEvents = _operationEvents.asSharedFlow()

    // Panggil sekali di MainActivity / Worker
    fun init(context: Context) {
        if (logFile == null) {
            val logDir = File(context.applicationContext.filesDir, "logs")
            if (!logDir.exists()) logDir.mkdirs()
            logFile = File(logDir, LOG_FILE_NAME)
        }
    }

    // Catat aktivitas umum / background (Navigasi, Lifecycle, Worker, Alarm)
    fun logActivity(category: String, message: String) {
        Log.i(TAG, "[$category] $message")
        writeToFile("INFO", category, message)
    }

    // Handler saat operasi CRUD BERHASIL
    fun logSuccess(category: String, userMessage: String, detail: String = "") {
        val fullLog = if (detail.isNotEmpty()) "$userMessage ($detail)" else userMessage
        Log.i(TAG, "[$category] SUCCESS: $fullLog")
        writeToFile("SUCCESS", category, fullLog)
        _operationEvents.tryEmit(OperationResult.Success(userMessage))
    }

    // Handler saat operasi CRUD GAGAL (menangkap penyebab pasti)
    fun logError(category: String, actionName: String, exception: Throwable) {
        val reason = parseErrorMessage(exception)
        Log.e(TAG, "[$category] ERROR saat $actionName: $reason", exception)
        writeToFile("ERROR", category, "$actionName -> Penyebab: $reason | Detail: ${exception.stackTraceToString().take(300)}")
        _operationEvents.tryEmit(OperationResult.Failure(actionName, reason))
    }

    // Menerjemahkan error teknis menjadi alasan yang mudah dipahami
    private fun parseErrorMessage(e: Throwable): String {
        val rawMsg = e.localizedMessage ?: e.message ?: ""
        return when {
            e is android.database.sqlite.SQLiteConstraintException -> "Data bertabrakan atau aturan database tidak terpenuhi."
            e is android.database.sqlite.SQLiteFullException -> "Penyimpanan HP penuh."
            e is IllegalArgumentException -> rawMsg.ifBlank { "Input data tidak valid." }
            e is NullPointerException -> "Data yang diproses kosong/tidak ditemukan."
            rawMsg.isNotBlank() -> rawMsg
            else -> "Terjadi kesalahan sistem (${e.javaClass.simpleName})"
        }
    }

    private fun writeToFile(level: String, category: String, message: String) {
        val file = logFile ?: return
        logScope.launch {
            try {
                // Reset sebagian jika ukuran file sudah melebihi 2 MB
                if (file.exists() && file.length() > MAX_LOG_SIZE_BYTES) {
                    val lastLines = file.readLines().takeLast(200)
                    file.writeText(lastLines.joinToString("\n") + "\n")
                }
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
                file.appendText("[$timestamp] [$level] [$category] $message\n")
            } catch (e: Exception) {
                Log.e(TAG, "Gagal menulis file log: ${e.message}")
            }
        }
    }

    // Opsional: Membaca seluruh isi log jika nanti ingin ditampilkan di halaman Settings
    fun readAllLogs(): String {
        return try {
            logFile?.takeIf { it.exists() }?.readText() ?: "Belum ada log aktivitas."
        } catch (e: Exception) {
            "Gagal membaca log: ${e.message}"
        }
    }
}