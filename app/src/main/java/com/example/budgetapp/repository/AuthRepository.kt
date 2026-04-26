package com.example.budgetapp.repository

import android.content.Context
import com.example.budgetapp.Data.AppDatabase
import com.example.budgetapp.Data.User

class AuthRepository(context: Context) {

    //This gives your Repository the specific commands needed to find or save user accounts in your database.
    private val userDao = AppDatabase.getDatabase(context).userDao()

    //This is a safe background function that tries to sign up a new user and lets you know if it succeeded or if there was an error (like the username already existing).
    suspend fun register(username: String, password: String): Result<Unit>{

        //This code checks if a username is available before creating a new account to make sure every user has a unique name.
        val existing = userDao.getUserByUsername(username)
        return if(existing != null){
            Result.failure(Exception("Username already taken"))
        }else{
            userDao.registerUser(User(username = username, password = password))
            Result.success(Unit)
        }
    }

    suspend fun login(username: String, password: String): User?{
        return userDao.login(username, password)
    }
}