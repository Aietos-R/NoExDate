package org.umn.noexdate.data.repository

import kotlinx.coroutines.flow.Flow
import org.umn.noexdate.data.local.dao.ExpenseDao
import org.umn.noexdate.data.local.entity.ExpenseEntity

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    // Mengambil semua riwayat transaksi
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    // Mengambil total pengeluaran
    val totalExpenses: Flow<Double?> = expenseDao.getTotalExpenses()

    suspend fun insert(expense: ExpenseEntity) {
        expenseDao.insertExpense(expense)
    }

    suspend fun delete(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }
}