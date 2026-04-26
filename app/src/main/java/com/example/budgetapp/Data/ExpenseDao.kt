package com.example.budgetapp.Data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ExpenseDao {

    //Use Upsert when you want to save data without worrying if it’s already in the database or not
    @Upsert
    // suspend to let a function "pause" its work and step aside so the app doesn't freeze while waiting for a slow task to finish
    suspend fun upsertExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    //use LiveData to automatically update the screen whenever your data changes, while making sure it only does so when the app is actually visible to the user
    fun getAllExpenses(): LiveData<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalAmount(): LiveData<Double?>

    // It tells the database to find all expenses in a certain category and show the newest ones first
    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
    fun getExpensesByCategory(category: String): LiveData<List<Expense>>

    // Search by description, category OR date string
    @Query("""
        SELECT * FROM expenses 
        WHERE description LIKE '%' || :query || '%' 
        OR category LIKE '%' || :query || '%'
        OR strftime('%d %m %Y', date/1000, 'unixepoch') LIKE '%' || :query || '%'
        ORDER BY date DESC
    """)

    fun searchExpense(query: String): LiveData<List<Expense>>
}