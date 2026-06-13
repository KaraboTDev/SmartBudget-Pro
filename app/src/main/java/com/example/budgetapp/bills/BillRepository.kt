package com.example.budgetapp.bills

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.budgetapp.Data.AppDatabase
import com.example.budgetapp.GoalManager
import java.util.Calendar

class BillRepository(context: Context) {

    private val billDao  = AppDatabase.getDatabase(context).billDao()
    private val username = GoalManager(context).currentUser

    val allBills    = billDao.getAllBills(username)
    val unpaidBills = billDao.getUnpaidBills(username)

    fun getBillsDueSoon(): LiveData<List<Bill>> {
        val now          = System.currentTimeMillis()
        val weekFromNow  = now + (7 * 24 * 60 * 60 * 1000L)
        return billDao.getBillsDueSoon(username, now, weekFromNow)
    }

    suspend fun insert(bill: Bill) {
        billDao.upsertBill(bill.copy(username = username))
    }

    suspend fun delete(bill: Bill) {
        billDao.deleteBill(bill)
    }

    suspend fun markAsPaid(bill: Bill) {
        if (bill.isRecurring) {
            // Advance due date to next cycle instead of deleting
            val nextDueDate = getNextDueDate(bill)
            billDao.updateBill(bill.copy(isPaid = false, dueDate = nextDueDate))
        } else {
            billDao.updateBill(bill.copy(isPaid = true))
        }
    }

    private fun getNextDueDate(bill: Bill): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = bill.dueDate }
        when (bill.recurringType) {
            "weekly"  -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            "yearly"  -> cal.add(Calendar.YEAR, 1)
            else      -> cal.add(Calendar.MONTH, 1) // default monthly
        }
        return cal.timeInMillis
    }
}