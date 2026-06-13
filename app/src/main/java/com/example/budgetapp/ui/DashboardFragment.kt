package com.example.budgetapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.budgetapp.AchievementManager
import com.example.budgetapp.GoalManager
import com.example.budgetapp.MainActivity
import com.example.budgetapp.R
import com.example.budgetapp.Data.Expense
import com.example.budgetapp.databinding.FragmentDashboardBinding
import com.example.budgetapp.viewmodel.ExpenseViewModel
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var expenseViewModel: ExpenseViewModel
    private lateinit var goalManager: GoalManager

    // Maps category name to a Material icon drawable
    private val categoryIcons = mapOf(
        "Food"          to R.drawable.ic_food,
        "Transport"     to R.drawable.ic_transport,
        "Entertainment" to R.drawable.ic_entertainment,
        "Shopping"      to R.drawable.ic_shopping,
        "Health"        to R.drawable.ic_health,
        "Education"     to R.drawable.ic_education,
        "Other"         to R.drawable.ic_other
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        expenseViewModel = ViewModelProvider(requireActivity())[ExpenseViewModel::class.java]
        goalManager      = GoalManager(requireContext())

        // ── Greeting based on time of day ─────────────────────────────────────
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else      -> "Good evening"
        }

        // ── Username — read directly from SharedPreferences ───────────────────
        val prefs    = requireContext().getSharedPreferences(
            "expense_goals", android.content.Context.MODE_PRIVATE
        )
        val username = prefs.getString("current_user", null)
        binding.tvUsername.text = when {
            !username.isNullOrEmpty() -> username
            else                      -> goalManager.currentUser
        }

        // ── XP / Level badge ───────────────────────────────────────────────────
        val achievements = AchievementManager(requireContext())
        binding.tvLevelBadge.text = "Level ${achievements.level} — ${achievements.levelTitle}"
        binding.progressDashboardXP.progress = achievements.levelProgress

        // ── See all → goes to History tab ─────────────────────────────────────
        binding.tvSeeAll.setOnClickListener {
            (activity as? MainActivity)?.navigateTo(R.id.nav_history, "Expense History")
        }

        // ── Add first expense button ───────────────────────────────────────────
        binding.btnAddFirst.setOnClickListener {
            (activity as? MainActivity)?.navigateTo(R.id.nav_add, "Add Expense")
        }

        // ── Observe expenses ──────────────────────────────────────────────────
        expenseViewModel.allExpenses.observe(viewLifecycleOwner) { expenses ->
            updateUI(expenses)
        }
    }

    private fun updateUI(expenses: List<Expense>) {
        val totalAmount = expenses.sumOf { it.amount }

        // Total amount + count
        binding.tvTotalAmount.text = "R%.2f".format(totalAmount)
        binding.tvTotalCount.text  =
            "${expenses.size} expense${if (expenses.size != 1) "s" else ""}"

        // Spending status icon — color reacts to goal status
        binding.ivSpendingIcon.imageTintList = when {
            goalManager.maximumGoal > 0 && totalAmount > goalManager.maximumGoal ->
                resources.getColorStateList(R.color.danger, null)
            goalManager.maximumGoal > 0 && totalAmount > goalManager.maximumGoal * 0.8 ->
                resources.getColorStateList(R.color.warning, null)
            totalAmount > 0 ->
                resources.getColorStateList(R.color.success, null)
            else ->
                resources.getColorStateList(R.color.text_secondary, null)
        }

        // Goals card
        updateGoalStatus(totalAmount)

        updateCountdown(totalAmount)

        // Empty state vs recent expenses
        if (expenses.isEmpty()) {
            binding.cardEmpty.visibility               = View.VISIBLE
            binding.recentExpensesContainer.visibility = View.GONE
        } else {
            binding.cardEmpty.visibility               = View.GONE
            binding.recentExpensesContainer.visibility = View.VISIBLE
            buildRecentExpenses(expenses.take(3))
        }

        // Category breakdown
        updateCategoryBreakdown(expenses, totalAmount)
    }

    private fun updateGoalStatus(total: Double) {
        val min = goalManager.minimumGoal
        val max = goalManager.maximumGoal

        if (!goalManager.hasGoals()) {
            binding.tvGoalMin.text    = "No goals set"
            binding.tvGoalMax.text    = ""
            binding.tvGoalStatus.text = "Set goals in menu"
            binding.tvGoalStatus.setTextColor(resources.getColor(R.color.text_secondary, null))

            binding.tvGoalStatusBadge.text = "No goals"
            binding.tvGoalStatusBadge.setTextColor(resources.getColor(R.color.text_secondary, null))
            binding.tvGoalStatusBadge.backgroundTintList = resources.getColorStateList(R.color.border_subtle, null)

            binding.progressGoal.progress = 0
            binding.progressGoal.progressTintList =
                resources.getColorStateList(R.color.text_primary, null)
            return
        }

        binding.tvGoalMin.text = "Min: R%.0f".format(min)
        binding.tvGoalMax.text = "Max: R%.0f".format(max)

        val progress = if (max > 0) ((total / max) * 100).toInt().coerceIn(0, 100) else 0
        binding.progressGoal.progress = progress

        when {
            max > 0 && total > max -> {
                binding.tvGoalStatus.text = "R%.2f over max".format(total - max)
                binding.tvGoalStatus.setTextColor(resources.getColor(R.color.danger, null))

                binding.tvGoalStatusBadge.text = "Over budget"
                binding.tvGoalStatusBadge.backgroundTintList =
                    resources.getColorStateList(R.color.danger_bg, null)
                binding.tvGoalStatusBadge.setTextColor(resources.getColor(R.color.danger, null))

                binding.progressGoal.progressTintList =
                    resources.getColorStateList(R.color.danger, null)
            }
            min > 0 && total < min -> {
                binding.tvGoalStatus.text = "R%.2f below min".format(min - total)
                binding.tvGoalStatus.setTextColor(resources.getColor(R.color.warning, null))

                binding.tvGoalStatusBadge.text = "Under target"
                binding.tvGoalStatusBadge.backgroundTintList =
                    resources.getColorStateList(R.color.warning_bg, null)
                binding.tvGoalStatusBadge.setTextColor(resources.getColor(R.color.warning, null))

                binding.progressGoal.progressTintList =
                    resources.getColorStateList(R.color.warning, null)
            }
            else -> {
                binding.tvGoalStatus.text = "On track"
                binding.tvGoalStatus.setTextColor(resources.getColor(R.color.success, null))

                binding.tvGoalStatusBadge.text = "On track"
                binding.tvGoalStatusBadge.backgroundTintList =
                    resources.getColorStateList(R.color.success_bg, null)
                binding.tvGoalStatusBadge.setTextColor(resources.getColor(R.color.success, null))

                binding.progressGoal.progressTintList =
                    resources.getColorStateList(R.color.success, null)
            }
        }
    }

    private fun buildRecentExpenses(expenses: List<Expense>) {
        binding.recentExpensesContainer.removeAllViews()

        expenses.forEach { expense ->
            val card = MaterialCardView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = 8.dpToPx() }
                radius        = 12.dpToPx().toFloat()
                cardElevation = 0f
                setCardBackgroundColor(resources.getColor(R.color.card_white, null))
                strokeColor = resources.getColor(R.color.border_subtle, null)
                strokeWidth = 1.dpToPx()
            }

            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(14.dpToPx(), 12.dpToPx(), 14.dpToPx(), 12.dpToPx())
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            val ivIcon = ImageView(requireContext()).apply {
                setImageResource(categoryIcons[expense.category] ?: R.drawable.ic_other)
                setBackgroundResource(R.drawable.icon_bg_neutral)
                imageTintList = resources.getColorStateList(R.color.text_secondary, null)
                val pad = 9.dpToPx()
                setPadding(pad, pad, pad, pad)
                layoutParams = LinearLayout.LayoutParams(40.dpToPx(), 40.dpToPx())
                scaleType = ImageView.ScaleType.FIT_CENTER
            }

            val middle = LinearLayout(requireContext()).apply {
                orientation  = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                ).also { it.marginStart = 12.dpToPx() }
            }
            val tvDesc = TextView(requireContext()).apply {
                text     = expense.description
                textSize = 14f
                typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
                setTextColor(resources.getColor(R.color.text_primary, null))
            }
            val tvDate = TextView(requireContext()).apply {
                text     = "${expense.category} • ${formatDate(expense.date)}"
                textSize = 11f
                setTextColor(resources.getColor(R.color.text_secondary, null))
            }
            middle.addView(tvDesc)
            middle.addView(tvDate)

            val tvAmount = TextView(requireContext()).apply {
                text     = "R%.2f".format(expense.amount)
                textSize = 14f
                typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
                setTextColor(resources.getColor(R.color.text_primary, null))
            }

            row.addView(ivIcon)
            row.addView(middle)
            row.addView(tvAmount)
            card.addView(row)
            binding.recentExpensesContainer.addView(card)
        }
    }

    // ── Goal countdown ────────────────────────────────────────────────────────
    private fun updateCountdown(total: Double) {
        val daysLeft = goalManager.daysRemaining()

        if (daysLeft < 0) {
            // No valid target date set — hide the card
            binding.cardCountdown.visibility = View.GONE
            return
        }

        binding.cardCountdown.visibility = View.VISIBLE

        val targetDateStr = SimpleDateFormat("dd MMM", Locale.getDefault())
            .format(Date(goalManager.goalTargetDate))

        binding.tvCountdownDays.text = when (daysLeft) {
            0    -> "Last day!"
            1    -> "1 day left"
            else -> "$daysLeft days left"
        }
        binding.tvCountdownLabel.text = "To reach your goal by $targetDateStr"

        // Badge reflects whether spending is currently on track for the goal
        val max = goalManager.maximumGoal
        when {
            max > 0 && total > max -> {
                binding.tvCountdownBadge.text = "Over budget"
                binding.tvCountdownBadge.setTextColor(resources.getColor(R.color.danger, null))
                setCountdownBadgeBg(R.color.danger_bg)
            }
            daysLeft <= 3 -> {
                binding.tvCountdownBadge.text = "Ending soon"
                binding.tvCountdownBadge.setTextColor(resources.getColor(R.color.warning, null))
                setCountdownBadgeBg(R.color.warning_bg)
            }
            else -> {
                binding.tvCountdownBadge.text = "On track"
                binding.tvCountdownBadge.setTextColor(resources.getColor(R.color.success, null))
                setCountdownBadgeBg(R.color.success_bg)
            }
        }
    }

    private fun setCountdownBadgeBg(colorRes: Int) {
        val drawable = resources.getDrawable(R.drawable.badge_background, null).mutate()
        drawable.setTint(resources.getColor(colorRes, null))
        binding.tvCountdownBadge.background = drawable
    }

    private fun updateCategoryBreakdown(expenses: List<Expense>, total: Double) {
        binding.categoryBreakdownContainer.removeAllViews()
        if (expenses.isEmpty()) return

        expenses.groupBy { it.category }
            .mapValues { it.value.sumOf { e -> e.amount } }
            .toList()
            .sortedByDescending { it.second }
            .forEach { (category, categoryTotal) ->
                val percentage = if (total > 0) (categoryTotal / total * 100).toInt() else 0

                val row = LinearLayout(requireContext()).apply {
                    orientation  = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also { it.bottomMargin = 12.dpToPx() }
                }

                val labelRow = LinearLayout(requireContext()).apply {
                    orientation  = LinearLayout.HORIZONTAL
                    gravity      = android.view.Gravity.CENTER_VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also { it.bottomMargin = 4.dpToPx() }
                }

                val ivIcon = ImageView(requireContext()).apply {
                    setImageResource(categoryIcons[category] ?: R.drawable.ic_other)
                    imageTintList = resources.getColorStateList(R.color.text_secondary, null)
                    layoutParams = LinearLayout.LayoutParams(16.dpToPx(), 16.dpToPx())
                        .also { it.marginEnd = 8.dpToPx() }
                }

                val tvLabel = TextView(requireContext()).apply {
                    text         = category
                    textSize     = 13f
                    layoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                    )
                    setTextColor(resources.getColor(R.color.text_primary, null))
                }

                val tvPercent = TextView(requireContext()).apply {
                    val amountStr = "R%.2f".format(categoryTotal)
                    text     = "$percentage%  •  $amountStr"
                    textSize = 12f
                    setTextColor(resources.getColor(R.color.text_secondary, null))
                }

                labelRow.addView(ivIcon)
                labelRow.addView(tvLabel)
                labelRow.addView(tvPercent)

                val bar = ProgressBar(
                    requireContext(), null,
                    android.R.attr.progressBarStyleHorizontal
                ).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 6.dpToPx()
                    )
                    max      = 100
                    progress = percentage
                    progressTintList =
                        resources.getColorStateList(R.color.text_primary, null)
                    progressBackgroundTintList =
                        resources.getColorStateList(R.color.border_subtle, null)
                }

                row.addView(labelRow)
                row.addView(bar)
                binding.categoryBreakdownContainer.addView(row)
            }
    }

    private fun formatDate(timestamp: Long): String =
        SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))

    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
