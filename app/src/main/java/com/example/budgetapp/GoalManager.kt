package com.example.budgetapp

import android.content.Context

class GoalManager(context: Context) {

    //This line opens a private "notebook" where the app can save the monthly budget goal.
    private val prefs = context.getSharedPreferences("expense_goals", Context.MODE_PRIVATE)

    // Store currently logged in username so goals are per-account
    var currentUser: String
        get() = prefs.getString("current_user", "default") ?: "default"
        set(value) = prefs.edit().putString("current_user", value).apply()

    //This code creates a "Smart Variable" that automatically saves itself to your phone's memory every time you change it.
    //This is a shortcut that lets you treat a saved setting like a normal variable, automatically handling the reading and writing to the phone's storage for you.
    // Goals are keyed by username
    var minimumGoal: Double
        get() = prefs.getFloat("min_goal_$currentUser", 0f).toDouble()
        set(value) = prefs.edit().putFloat("min_goal_$currentUser", value.toFloat()).apply()

    var maximumGoal: Double
        get() = prefs.getFloat("max_goal_$currentUser", 0f).toDouble()
        set(value) = prefs.edit().putFloat("max_goal_$currentUser", value.toFloat()).apply()

    fun hasGoals(): Boolean = minimumGoal > 0 || maximumGoal > 0

    // Save list of all registered usernames for account switching
    fun getSavedAccounts(): MutableList<String> {
        val accounts = prefs.getStringSet("all_accounts", mutableSetOf()) ?: mutableSetOf()
        return accounts.toMutableList()
}

    fun addAccount(username: String) {
        val accounts = getSavedAccounts().toMutableSet()
        accounts.add(username)
        prefs.edit().putStringSet("all_accounts", accounts).apply()
    }

    fun logout() {
        prefs.edit().remove("current_user").apply()
    }
}