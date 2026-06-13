package com.example.budgetapp.bills

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val username: String = "",
    val name: String,           // e.g. "Netflix"
    val amount: Double,         // e.g. 199.00
    val dueDate: Long,          // timestamp of next due date
    val isRecurring: Boolean,   // true = monthly, false = one-time
    val recurringType: String = "monthly", // "weekly", "monthly", "yearly"
    val isPaid: Boolean = false,
    val category: String = "Bills",
    val notes: String = ""
)