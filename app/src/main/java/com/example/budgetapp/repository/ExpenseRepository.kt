package com.example.budgetapp.repository

import android.content.Context
import androidx.room.Upsert
import com.example.budgetapp.Data.AppDatabase
import com.example.budgetapp.Data.Expense

//You need context to give your code permission to access the phone's storage and find your database file.
class ExpenseRepository(context: Context) {
    private val expenseDao = AppDatabase.getDatabase(context).expenseDao()

    val allExpenses = expenseDao.getAllExpenses()
    val totalAmount = expenseDao.getTotalAmount()

    suspend fun upsert(expense: Expense) {
        expenseDao.upsertExpense(expense)
    }

    suspend fun delete(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    fun getByCategory(category: String) =
        expenseDao.getExpensesByCategory(category)

    // Search returns LiveData so results auto-update as user types
    fun searchExpenses(query: String) = expenseDao.searchExpense(query)
}

