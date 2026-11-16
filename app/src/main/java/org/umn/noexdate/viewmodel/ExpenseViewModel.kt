package org.umn.noexdate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.umn.noexdate.data.local.entity.ExpenseEntity
import org.umn.noexdate.data.repository.ExpenseRepository
import org.umn.noexdate.utils.AppLogger

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalExpenses: StateFlow<Double> = repository.totalExpenses
        .map { it ?: 0.0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    fun addExpense(amount: Double, category: String, description: String) {
        viewModelScope.launch {
            try {
                if (amount <= 0) {
                    throw IllegalArgumentException("Nominal pengeluaran harus lebih dari 0")
                }
                val newExpense = ExpenseEntity(
                    amount = amount,
                    category = category,
                    description = description
                )
                repository.insert(newExpense)
                AppLogger.logSuccess("EXPENSE", "Transaksi berhasil disimpan", "Kategori: $category, Nominal: $amount")
            } catch (e: Exception) {
                AppLogger.logError("EXPENSE", "Simpan transaksi", e)
            }
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            try {
                repository.delete(expense)
                AppLogger.logSuccess("EXPENSE", "Transaksi berhasil dihapus", "ID: ${expense.id}")
            } catch (e: Exception) {
                AppLogger.logError("EXPENSE", "Hapus transaksi", e)
            }
        }
    }

    fun deleteExpenses(expenses: List<ExpenseEntity>) {
        viewModelScope.launch {
            try {
                expenses.forEach { repository.delete(it) }
                AppLogger.logSuccess("EXPENSE", "${expenses.size} transaksi berhasil dihapus")
            } catch (e: Exception) {
                AppLogger.logError("EXPENSE", "Hapus beberapa transaksi", e)
            }
        }
    }
}

class ExpenseViewModelFactory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}