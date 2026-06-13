package com.example.budgetapp.bills

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BillDao {

    @Upsert
    suspend fun upsertBill(bill: Bill)

    @Delete
    suspend fun deleteBill(bill: Bill)

    @Update
    suspend fun updateBill(bill: Bill)

    // All bills for current user ordered by due date
    @Query("SELECT * FROM bills WHERE username = :username ORDER BY dueDate ASC")
    fun getAllBills(username: String): LiveData<List<Bill>>

    // Unpaid bills only
    @Query("SELECT * FROM bills WHERE username = :username AND isPaid = 0 ORDER BY dueDate ASC")
    fun getUnpaidBills(username: String): LiveData<List<Bill>>

    // Bills due within the next 7 days
    @Query("""
        SELECT * FROM bills 
        WHERE username = :username 
        AND isPaid = 0 
        AND dueDate BETWEEN :now AND :weekFromNow 
        ORDER BY dueDate ASC
    """)
    fun getBillsDueSoon(username: String, now: Long, weekFromNow: Long): LiveData<List<Bill>>
}