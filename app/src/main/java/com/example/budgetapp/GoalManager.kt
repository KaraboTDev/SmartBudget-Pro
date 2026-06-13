package com.example.budgetapp

import android.content.Context
import java.util.concurrent.TimeUnit

class GoalManager(context: Context) {

    //This line opens a private "notebook" where the app can save the monthly budget goal.
    private val prefs = context.getSharedPreferences("expense_goals", Context.MODE_PRIVATE)

    // Store currently logged in username so goals are per-account
    var currentUser: String
        get() = prefs.getString("current_user", "default") ?: "default"
        set(value) = prefs.edit().putString("current_user", value).apply()

    // Store currently logged in user ID for database filtering
    var currentUserId: Int
        get() = prefs.getInt("current_user_id", -1)
        set(value) = prefs.edit().putInt("current_user_id", value).apply()

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
        prefs.edit().remove("current_user").remove("current_user_id").apply()
    }



    var goalTargetDate: Long
        get() = prefs.getLong("goal_target_date_$currentUser", 0L)
        set(value) = prefs.edit().putLong("goal_target_date_$currentUser", value).apply()

    fun hasTargetDate(): Boolean = goalTargetDate > System.currentTimeMillis()

    // Returns days remaining, or -1 if no valid target date set
    fun daysRemaining(): Int {
        if (!hasTargetDate()) return -1
        val diff = goalTargetDate - System.currentTimeMillis()
        return TimeUnit.MILLISECONDS.toDays(diff).toInt() + 1 // +1 so "today" counts as day 1
    }

    // --- Custom Categories ---
    private val defaultCategories = setOf("Food", "Transport", "Entertainment", "Shopping", "Health", "Education", "Other")

    fun getCustomCategories(): MutableList<String> {
        val key = "custom_categories_$currentUser"
        val categories = prefs.getStringSet(key, defaultCategories) ?: defaultCategories
        return categories.toMutableList().sorted().toMutableList()
    }

    fun addCategory(category: String) {
        val key = "custom_categories_$currentUser"
        val categories = getCustomCategories().toMutableSet()
        categories.add(category)
        prefs.edit().putStringSet(key, categories).apply()
    }

    fun removeCategory(category: String) {
        val key = "custom_categories_$currentUser"
        val categories = getCustomCategories().toMutableSet()
        if (categories.size > 1) { // Keep at least one category
            categories.remove(category)
            prefs.edit().putStringSet(key, categories).apply()
        }
    }
}