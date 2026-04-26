package com.example.budgetapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetapp.Data.Expense
import com.example.budgetapp.repository.ExpenseRepository
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ExpenseRepository(application)

    // these are observed by the fragments
    val allExpenses = repository.allExpenses
    val totalAmount = repository.totalAmount

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            repository.upsert(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.delete(expense)
        }
    }

    fun getByCategory(category: String) =
        repository.getByCategory(category)

    //returns LiveData for the search results
    fun searchExpenses(query: String) =
        repository.searchExpenses(query)
}
