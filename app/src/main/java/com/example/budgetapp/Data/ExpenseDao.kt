package com.example.budgetapp.Data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ExpenseDao {

    @Upsert
    suspend fun upsertExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    // Only fetch expenses belonging to this user
    @Query("SELECT * FROM expenses WHERE username = :username ORDER BY date DESC")
    fun getAllExpenses(username: String): LiveData<List<Expense>>

    // Total for this user only
    @Query("SELECT SUM(amount) FROM expenses WHERE username = :username")
    fun getTotalAmount(username: String): LiveData<Double?>

    // Category filter for this user
    @Query("SELECT * FROM expenses WHERE username = :username AND category = :category ORDER BY date DESC")
    fun getExpensesByCategory(username: String, category: String): LiveData<List<Expense>>

    // Date range filter for this user
    @Query("SELECT * FROM expenses WHERE username = :username AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(username: String, startDate: Long, endDate: Long): LiveData<List<Expense>>

    // Category AND Date Range filter
    @Query("SELECT * FROM expenses WHERE username = :username AND category = :category AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByCategoryAndDateRange(username: String, category: String, startDate: Long, endDate: Long): LiveData<List<Expense>>

    // Search for this user only
    @Query("""
        SELECT * FROM expenses 
        WHERE username = :username
        AND (
            description LIKE '%' || :query || '%' 
            OR category LIKE '%' || :query || '%'
            OR strftime('%d %m %Y', date/1000, 'unixepoch') LIKE '%' || :query || '%'
        )
        ORDER BY date DESC
    """)
    fun searchExpenses(username: String, query: String): LiveData<List<Expense>>
}