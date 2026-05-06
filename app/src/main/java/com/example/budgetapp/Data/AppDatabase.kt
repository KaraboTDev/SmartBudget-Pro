package com.example.budgetapp.Data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Expense::class, User::class], version = 4, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    //allows you to access the database from anywhere in your app without having to create a new version of it every time.

    abstract fun expenseDao(): ExpenseDao
    abstract fun userDao(): UserDao


    companion object{

        //ensures that the most up-to-date version of the database is instantly visible to all parts of your app
        @Volatile
        private var INSTANCE: AppDatabase? = null

        //This function checks if the database already exists; if it doesn't, it locks the door to make sure only one copy is created safely.
        fun getDatabase(context: Context): AppDatabase{

            //If we already have a database  in the INSTANCE spot, just give me that one immediately
            return INSTANCE?: synchronized(this){

                //This ensures the database lives as long as the whole app, even if you switch screens.
                val instance = Room.databaseBuilder(context.applicationContext,
                    AppDatabase::class.java,
                    "expense_database")
                    .fallbackToDestructiveMigration()
                    .build()

                //This saves the newly built database into our INSTANCE variable so we can reuse it next time without building it again.
                INSTANCE = instance
                 return instance
            }

        }
    }
}