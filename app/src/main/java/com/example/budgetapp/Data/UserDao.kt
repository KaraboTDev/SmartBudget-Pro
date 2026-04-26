package com.example.budgetapp.Data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface UserDao {

    @Upsert
    suspend fun registerUser(user: User)

    // Returns the user if username + password match, null if not
    @Query("SELECT * FROM users WHERE username = :username AND password = :password")

    //This is a safe background function that looks for a matching user in the database and returns the user's info if the login is successful.
    suspend fun login(username: String, password: String): User?

    // Check if a username is already taken
    //It tells the database to find the user that matches the name provided and stop searching immediately after finding the first one.
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?
}

