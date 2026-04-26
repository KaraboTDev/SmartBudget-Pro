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
import com.example.budgetapp.GoalManager
import com.example.budgetapp.MainActivity
import com.example.budgetapp.R
import com.example.budgetapp.Data.Expense
import com.example.budgetapp.databinding.FragmentDashboardBinding
import com.example.budgetapp.viewmodel.ExpenseViewModel
import com.google.android.material.button.MaterialButton
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

    private val categoryEmojis = mapOf(
        "Food"          to "🍔",
        "Transport"     to "🚗",
        "Entertainment" to "🎬",
        "Shopping"      to "🛍️",
        "Health"        to "💊",
        "Education"     to "📚",
        "Other"         to "💰"
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
            hour < 12 -> "Good morning 👋"
            hour < 17 -> "Good afternoon 👋"
            else      -> "Good evening 👋"
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

    // ── Main UI update ────────────────────────────────────────────────────────
    private fun updateUI(expenses: List<Expense>) {
        val totalAmount = expenses.sumOf { it.amount }

        // Total amount + count
        binding.tvTotalAmount.text = "R%.2f".format(totalAmount)
        binding.tvTotalCount.text  =
            "${expenses.size} expense${if (expenses.size != 1) "s" else ""}"

        // Spending emoji reacts to goal status
        binding.tvSpendingEmoji.text = when {
            goalManager.maximumGoal > 0 && totalAmount > goalManager.maximumGoal  -> "🔴"
            goalManager.maximumGoal > 0 && totalAmount > goalManager.maximumGoal * 0.8 -> "🟠"
            totalAmount > 0 -> "🟢"
            else            -> "💰"
        }

        // Goals card
        updateGoalStatus(totalAmount)

        // Empty state vs recent expenses
        if (expenses.isEmpty()) {
            binding.cardEmpty.visibility              = View.VISIBLE
            binding.recentExpensesContainer.visibility = View.GONE
        } else {
            binding.cardEmpty.visibility              = View.GONE
            binding.recentExpensesContainer.visibility = View.VISIBLE
            buildRecentExpenses(expenses.take(3))
        }

        // Category breakdown
        updateCategoryBreakdown(expenses, totalAmount)
    }

    // ── Goals progress ────────────────────────────────────────────────────────
    private fun updateGoalStatus(total: Double) {
        val min = goalManager.minimumGoal
        val max = goalManager.maximumGoal

        if (!goalManager.hasGoals()) {
            binding.tvGoalMin.text         = "No goals set"
            binding.tvGoalMax.text         = ""
            binding.tvGoalStatus.text      = "Set goals in menu"
            binding.tvGoalStatus.setTextColor(Color.GRAY)
            binding.progressGoal.progress  = 0
            binding.tvGoalStatusBadge.text = "No goals"
            binding.tvGoalStatusBadge.backgroundTintList =
                resources.getColorStateList(R.color.text_secondary, null)
            return
        }

        binding.tvGoalMin.text = "Min: R%.0f".format(min)
        binding.tvGoalMax.text = "Max: R%.0f".format(max)

        val progress = if (max > 0) ((total / max) * 100).toInt().coerceIn(0, 100) else 0
        binding.progressGoal.progress = progress

        when {
            max > 0 && total > max -> {
                binding.tvGoalStatus.text = "R%.2f over max".format(total - max)
                binding.tvGoalStatus.setTextColor(Color.RED)
                binding.tvGoalStatusBadge.text = "Over budget"
                binding.tvGoalStatusBadge.backgroundTintList =
                    resources.getColorStateList(android.R.color.holo_red_light, null)
                binding.progressGoal.progressTintList =
                    resources.getColorStateList(android.R.color.holo_red_light, null)
            }
            min > 0 && total < min -> {
                binding.tvGoalStatus.text = "R%.2f below min".format(min - total)
                binding.tvGoalStatus.setTextColor(Color.parseColor("#FF8C00"))
                binding.tvGoalStatusBadge.text = "Under target"
                binding.tvGoalStatusBadge.backgroundTintList =
                    resources.getColorStateList(android.R.color.holo_orange_light, null)
                binding.progressGoal.progressTintList =
                    resources.getColorStateList(android.R.color.holo_orange_light, null)
            }
            else -> {
                binding.tvGoalStatus.text = "On track!"
                binding.tvGoalStatus.setTextColor(Color.parseColor("#2E7D32"))
                binding.tvGoalStatusBadge.text = "On track ✅"
                binding.tvGoalStatusBadge.backgroundTintList =
                    resources.getColorStateList(R.color.green_primary, null)
                binding.progressGoal.progressTintList =
                    resources.getColorStateList(R.color.green_primary, null)
            }
        }
    }

    // ── Recent expenses (latest 3) ────────────────────────────────────────────
    private fun buildRecentExpenses(expenses: List<Expense>) {
        binding.recentExpensesContainer.removeAllViews()

        expenses.forEach { expense ->
            val card = MaterialCardView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = 8.dpToPx() }
                radius        = 12.dpToPx().toFloat()
                cardElevation = 2.dpToPx().toFloat()
            }

            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(16.dpToPx(), 14.dpToPx(), 16.dpToPx(), 14.dpToPx())
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            // Emoji badge
            val tvEmoji = TextView(requireContext()).apply {
                text     = categoryEmojis[expense.category] ?: "💰"
                textSize = 22f
                layoutParams = LinearLayout.LayoutParams(44.dpToPx(), 44.dpToPx())
                gravity  = android.view.Gravity.CENTER
            }

            // Description + date column
            val middle = LinearLayout(requireContext()).apply {
                orientation  = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                ).also { it.marginStart = 12.dpToPx() }
            }
            val tvDesc = TextView(requireContext()).apply {
                text     = expense.description
                textSize = 15f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(resources.getColor(R.color.text_primary, null))
            }
            val tvDate = TextView(requireContext()).apply {
                text     = formatDate(expense.date)
                textSize = 12f
                setTextColor(resources.getColor(R.color.text_secondary, null))
            }
            middle.addView(tvDesc)
            middle.addView(tvDate)

            // Amount
            val tvAmount = TextView(requireContext()).apply {
                text     = "R%.2f".format(expense.amount)
                textSize = 15f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(resources.getColor(R.color.blue_primary, null))
            }

            row.addView(tvEmoji)
            row.addView(middle)
            row.addView(tvAmount)
            card.addView(row)
            binding.recentExpensesContainer.addView(card)
        }
    }

    // ── Category breakdown ────────────────────────────────────────────────────
    private fun updateCategoryBreakdown(expenses: List<Expense>, total: Double) {
        binding.categoryBreakdownContainer.removeAllViews()
        if (expenses.isEmpty()) return

        expenses.groupBy { it.category }
            .mapValues { it.value.sumOf { e -> e.amount } }
            .toList()
            .sortedByDescending { it.second }
            .forEach { (category, categoryTotal) ->
                val percentage = if (total > 0) (categoryTotal / total * 100).toInt() else 0
                val emoji      = categoryEmojis[category] ?: "💰"

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

                val tvLabel = TextView(requireContext()).apply {
                    text         = "$emoji  $category"
                    textSize     = 14f
                    layoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                    )
                    setTextColor(resources.getColor(R.color.text_primary, null))
                }

                val tvPercent = TextView(requireContext()).apply {
                    text     = "$percentage%%  •  R%.2f".format(categoryTotal)
                    textSize = 13f
                    setTextColor(resources.getColor(R.color.text_secondary, null))
                }

                labelRow.addView(tvLabel)
                labelRow.addView(tvPercent)

                val bar = ProgressBar(
                    requireContext(), null,
                    android.R.attr.progressBarStyleHorizontal
                ).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 8.dpToPx()
                    )
                    max      = 100
                    progress = percentage
                    progressTintList =
                        resources.getColorStateList(R.color.blue_light, null)
                }

                row.addView(labelRow)
                row.addView(bar)
                binding.categoryBreakdownContainer.addView(row)
            }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private fun formatDate(timestamp: Long): String =
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))

    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}