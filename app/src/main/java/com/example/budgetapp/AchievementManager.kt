package com.example.budgetapp

import android.content.Context

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val xpReward: Int
)

class AchievementManager(context: Context) {

    private val prefs = context.getSharedPreferences(
        "achievements", Context.MODE_PRIVATE
    )

    //  XP & Levels
    var totalXP: Int
        get() = prefs.getInt("total_xp", 0)
        set(value) = prefs.edit().putInt("total_xp", value).apply()

    val level: Int get() = when {
        totalXP >= 5000 -> 5
        totalXP >= 2000 -> 4
        totalXP >= 1000 -> 3
        totalXP >= 400  -> 2
        else            -> 1
    }

    val levelTitle: String get() = when (level) {
        1 -> "Budget Beginner"
        2 -> "Savings Apprentice"
        3 -> "Money Manager"
        4 -> "Finance Pro"
        5 -> "Budget Master"
        else -> "Budget Beginner"
    }

    val xpForNextLevel: Int get() = when (level) {
        1 -> 400
        2 -> 1000
        3 -> 2000
        4 -> 5000
        else -> 5000
    }

    val levelProgress: Int get() {
        val prevThreshold = when (level) {
            1 -> 0; 2 -> 400; 3 -> 1000; 4 -> 2000; else -> 5000
        }
        val nextThreshold = xpForNextLevel
        return if (nextThreshold == prevThreshold) 100
        else ((totalXP - prevThreshold).toFloat() /
                (nextThreshold - prevThreshold) * 100).toInt().coerceIn(0, 100)
    }

    fun addXP(amount: Int) {
        totalXP += amount
    }

    //  Badges
    val allBadges = listOf(
        Badge("first_expense",   "First Step",       "Log your first expense",              "🎯", 50),
        Badge("five_expenses",   "Getting Started",  "Log 5 expenses",                      "📝", 100),
        Badge("ten_expenses",    "On a Roll",        "Log 10 expenses",                     "🔥", 150),
        Badge("fifty_expenses",  "Expense Expert",   "Log 50 expenses",                     "💼", 500),
        Badge("goal_set",        "Goal Setter",      "Set your first monthly goal",         "🎯", 75),
        Badge("within_budget",   "Budget Keeper",    "Stay within your max goal",           "✅", 200),
        Badge("under_min",       "Big Spender",      "Stay above your minimum goal",        "📈", 100),
        Badge("photo_added",     "Receipt Ready",    "Attach a photo to an expense",        "📷", 75),
        Badge("search_used",     "Detective",        "Use the search feature",              "🔍", 50),
        Badge("multi_category",  "Diverse Spender",  "Log expenses in 5+ categories",       "🗂️", 150),
        Badge("level_2",         "Rising Saver",     "Reach Level 2",                       "⭐", 100),
        Badge("level_3",         "Money Manager",    "Reach Level 3",                       "🌟", 200),
        Badge("level_5",         "Budget Master",    "Reach the maximum level",             "👑", 1000),
    )

    fun isBadgeUnlocked(badgeId: String): Boolean =
        prefs.getBoolean("badge_$badgeId", false)

    fun unlockBadge(badgeId: String): Boolean {
        if (isBadgeUnlocked(badgeId)) return false // already unlocked
        val badge = allBadges.find { it.id == badgeId } ?: return false
        prefs.edit().putBoolean("badge_$badgeId", true).apply()
        addXP(badge.xpReward)
        return true // returns true = newly unlocked, show notification
    }

    fun getUnlockedBadges() = allBadges.filter { isBadgeUnlocked(it.id) }
    fun getLockedBadges()   = allBadges.filter { !isBadgeUnlocked(it.id) }

    //  Check and award all relevant badges
    // Call this after every expense add/delete or goal update
    fun checkAndAward(
        expenseCount: Int,
        totalSpent: Double,
        goalManager: GoalManager,
        categoriesUsed: Set<String>,
        hasPhoto: Boolean,
        searchUsed: Boolean
    ): List<Badge> {
        val newlyUnlocked = mutableListOf<Badge>()

        fun tryUnlock(id: String) {
            if (unlockBadge(id))
                allBadges.find { it.id == id }?.let { newlyUnlocked.add(it) }
        }

        // Expense count badges
        if (expenseCount >= 1)  tryUnlock("first_expense")
        if (expenseCount >= 5)  tryUnlock("five_expenses")
        if (expenseCount >= 10) tryUnlock("ten_expenses")
        if (expenseCount >= 50) tryUnlock("fifty_expenses")

        // Goal badges
        if (goalManager.hasGoals()) {
            tryUnlock("goal_set")
            if (goalManager.maximumGoal > 0 && totalSpent <= goalManager.maximumGoal)
                tryUnlock("within_budget")
            if (goalManager.minimumGoal > 0 && totalSpent >= goalManager.minimumGoal)
                tryUnlock("under_min")
        }

        // Feature badges
        if (hasPhoto)                    tryUnlock("photo_added")
        if (searchUsed)                  tryUnlock("search_used")
        if (categoriesUsed.size >= 5)    tryUnlock("multi_category")

        // Level badges
        if (level >= 2) tryUnlock("level_2")
        if (level >= 3) tryUnlock("level_3")
        if (level >= 5) tryUnlock("level_5")

        return newlyUnlocked
    }
}