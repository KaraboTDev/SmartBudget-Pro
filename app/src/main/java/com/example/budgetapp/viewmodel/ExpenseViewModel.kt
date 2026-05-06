package com.example.budgetapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.budgetapp.Data.Expense
import com.example.budgetapp.GoalManager
import com.example.budgetapp.repository.ExpenseRepository
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ExpenseRepository(application)
    private val goalManager = GoalManager(application)
    
    // Use username instead of userId to match Repository and Dao
    private val currentUsername = MutableLiveData<String>()

    init {
        // Initialize with the current username from GoalManager
        currentUsername.value = goalManager.currentUser
    }

    // These are observed by the fragments
    // switchMap ensures that whenever username changes, the LiveData is updated to the new user's expenses
    val allExpenses: LiveData<List<Expense>> = currentUsername.switchMap { username ->
        repository.getAllExpenses(username)
    }
    
    val totalAmount: LiveData<Double?> = currentUsername.switchMap { username ->
        repository.getTotalAmount(username)
    }

    fun refreshUser() {
        currentUsername.value = goalManager.currentUser
    }

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            // Ensure the expense is saved with the current user's identity
            val expenseWithUser = expense.copy(
                username = goalManager.currentUser,
                userId = goalManager.currentUserId
            )
            repository.upsert(expenseWithUser)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.delete(expense)
        }
    }

    fun getByCategory(category: String) =
        repository.getByCategory(goalManager.currentUser, category)

    fun searchExpenses(query: String) =
        repository.searchExpenses(goalManager.currentUser, query)
}