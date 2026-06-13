package com.example.budgetapp.repository

import android.content.Context
import com.example.budgetapp.Data.AppDatabase
import com.example.budgetapp.Data.Expense
import com.example.budgetapp.GoalManager

class ExpenseRepository(context: Context) {

    private val expenseDao = AppDatabase.getDatabase(context).expenseDao()
    private val goalManager = GoalManager(context)

    // Current username from GoalManager
    private val username get() = goalManager.currentUser

    // LiveData for the current user
    val allExpenses = expenseDao.getAllExpenses(username)
    val totalAmount = expenseDao.getTotalAmount(username)

    // Functions for switchMap in ViewModel
    fun getAllExpenses(username: String) = expenseDao.getAllExpenses(username)
    fun getTotalAmount(username: String) = expenseDao.getTotalAmount(username)

    suspend fun upsert(expense: Expense) {
        // Ensure username is stamped onto the expense
        val expenseWithUser = if (expense.username.isEmpty()) {
            expense.copy(username = username)
        } else {
            expense
        }
        expenseDao.upsertExpense(expenseWithUser)
    }

    suspend fun delete(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    fun getByCategory(username: String, category: String) =
        expenseDao.getExpensesByCategory(username, category)

    fun getByDateRange(username: String, startDate: Long, endDate: Long) =
        expenseDao.getExpensesByDateRange(username, startDate, endDate)

    fun getByCategoryAndDateRange(username: String, category: String, startDate: Long, endDate: Long) =
        expenseDao.getExpensesByCategoryAndDateRange(username, category, startDate, endDate)

    fun searchExpenses(username: String, query: String) =
        expenseDao.searchExpenses(username, query)
}