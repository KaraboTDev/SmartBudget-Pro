package com.example.budgetapp.bills

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class BillViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG        = "BillViewModel"
    private val repository = BillRepository(application)

    val allBills    = repository.allBills
    val unpaidBills = repository.unpaidBills
    val billsDueSoon = repository.getBillsDueSoon()

    fun addBill(bill: Bill) {
        viewModelScope.launch {
            Log.d(TAG, "Adding bill: ${bill.name}, Amount: R${bill.amount}, Due: ${bill.dueDate}")
            repository.insert(bill)
        }
    }

    fun deleteBill(bill: Bill) {
        viewModelScope.launch {
            Log.d(TAG, "Deleting bill: ${bill.name}")
            repository.delete(bill)
        }
    }

    fun markAsPaid(bill: Bill) {
        viewModelScope.launch {
            Log.d(TAG, "Marking bill as paid: ${bill.name}, Recurring: ${bill.isRecurring}")
            repository.markAsPaid(bill)
        }
    }
}