package com.example.budgetapp.Data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(@PrimaryKey(autoGenerate = true)
                   val id: Int = 0,
                   val username: String = "",   // ← links expense to account
                   val amount: Double,
                   val category: String,
                   val description: String,
                   //to get the current real-world date and time based on the clock you see on your phone.
                   val date: Long = System.currentTimeMillis(),
                   val photoPath: String? = null,
                   val userId: Int = 0
)
