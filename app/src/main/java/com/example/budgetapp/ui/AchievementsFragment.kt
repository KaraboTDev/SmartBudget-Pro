package com.example.budgetapp.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.budgetapp.AchievementManager
import com.example.budgetapp.Badge
import com.example.budgetapp.GoalManager
import com.example.budgetapp.R
import com.example.budgetapp.viewmodel.ExpenseViewModel
import com.google.android.material.card.MaterialCardView

class AchievementsFragment : Fragment() {

    private lateinit var achievementManager: AchievementManager
    private lateinit var goalManager: GoalManager
    private lateinit var viewModel: ExpenseViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_achievements, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        achievementManager = AchievementManager(requireContext())
        goalManager        = GoalManager(requireContext())
        viewModel          = ViewModelProvider(requireActivity())[ExpenseViewModel::class.java]

        // Update XP card
        view.findViewById<TextView>(R.id.tvLevelTitle).text = achievementManager.levelTitle
        view.findViewById<TextView>(R.id.tvLevel).text      = achievementManager.level.toString()
        view.findViewById<TextView>(R.id.tvXP).text         = "${achievementManager.totalXP} XP total"
        view.findViewById<ProgressBar>(R.id.progressXP).progress = achievementManager.levelProgress
        view.findViewById<TextView>(R.id.tvXPProgress).text =
            "${achievementManager.totalXP} / ${achievementManager.xpForNextLevel} XP to next level"

        // Check and award badges based on current data
        viewModel.allExpenses.observe(viewLifecycleOwner) { expenses ->
            val categories   = expenses.map { it.category }.toSet()
            val hasPhoto     = expenses.any { !it.photoPath.isNullOrEmpty() }
            val total        = expenses.sumOf { it.amount }

            achievementManager.checkAndAward(
                expenseCount    = expenses.size,
                totalSpent      = total,
                goalManager     = goalManager,
                categoriesUsed  = categories,
                hasPhoto        = hasPhoto,
                searchUsed      = false
            )

            // Render badges
            buildBadgeList(
                view.findViewById(R.id.unlockedBadgesContainer),
                achievementManager.getUnlockedBadges(),
                unlocked = true
            )
            buildBadgeList(
                view.findViewById(R.id.lockedBadgesContainer),
                achievementManager.getLockedBadges(),
                unlocked = false
            )
        }
    }

    private fun buildBadgeList(
        container: LinearLayout,
        badges: List<Badge>,
        unlocked: Boolean
    ) {
        container.removeAllViews()

        if (badges.isEmpty()) {
            val empty = TextView(requireContext()).apply {
                text     = if (unlocked) "No badges unlocked yet — keep going!" else "All badges unlocked! 🏆"
                textSize = 14f
                setTextColor(resources.getColor(R.color.text_secondary, null))
                setPadding(0, 8.dpToPx(), 0, 16.dpToPx())
            }
            container.addView(empty)
            return
        }

        badges.forEach { badge ->
            val card = MaterialCardView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = 8.dpToPx() }
                radius        = 12.dpToPx().toFloat()
                cardElevation = 2.dpToPx().toFloat()
                alpha         = if (unlocked) 1f else 0.45f
            }

            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(16.dpToPx(), 14.dpToPx(), 16.dpToPx(), 14.dpToPx())
                gravity     = android.view.Gravity.CENTER_VERTICAL
            }

            // Badge icon
            val tvIcon = TextView(requireContext()).apply {
                text        = if (unlocked) badge.icon else "🔒"
                textSize    = 28f
                layoutParams = LinearLayout.LayoutParams(52.dpToPx(), 52.dpToPx())
                gravity     = android.view.Gravity.CENTER
            }

            // Badge info
            val middle = LinearLayout(requireContext()).apply {
                orientation  = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                ).also { it.marginStart = 12.dpToPx() }
            }
            val tvTitle = TextView(requireContext()).apply {
                text     = badge.title
                textSize = 15f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(
                    if (unlocked) resources.getColor(R.color.text_primary, null)
                    else resources.getColor(R.color.text_secondary, null)
                )
            }
            val tvDesc = TextView(requireContext()).apply {
                text     = badge.description
                textSize = 12f
                setTextColor(resources.getColor(R.color.text_secondary, null))
            }
            middle.addView(tvTitle)
            middle.addView(tvDesc)

            // XP reward
            val tvXP = TextView(requireContext()).apply {
                text     = "+${badge.xpReward} XP"
                textSize = 13f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(
                    if (unlocked) Color.parseColor("#2E7D32")
                    else resources.getColor(R.color.text_secondary, null)
                )
            }

            row.addView(tvIcon)
            row.addView(middle)
            row.addView(tvXP)
            card.addView(row)
            container.addView(card)
        }
    }

    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).toInt()
}