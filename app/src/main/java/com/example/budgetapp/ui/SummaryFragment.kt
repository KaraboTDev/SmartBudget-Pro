package com.example.budgetapp.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.budgetapp.GoalManager
import com.example.budgetapp.R
import com.example.budgetapp.viewmodel.ExpenseViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SummaryFragment : Fragment() {

    private lateinit var viewModel: ExpenseViewModel
    private lateinit var goalManager: GoalManager

    private val categories = listOf(
        "Food", "Transport", "Entertainment",
        "Shopping", "Health", "Education", "Other"
    )

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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[ExpenseViewModel::class.java]

        val tvTotal   = view.findViewById<TextView>(R.id.tvTotalAmount)
        val tvCount   = view.findViewById<TextView>(R.id.tvExpenseCount)
        val container = view.findViewById<LinearLayout>(R.id.categoryContainer)

        viewModel.totalAmount.observe(viewLifecycleOwner) { total ->
            tvTotal.text = "R%.2f".format(total ?: 0.0)
        }

        viewModel.allExpenses.observe(viewLifecycleOwner) { expenses ->
            tvCount.text = "${expenses.size} expense${if (expenses.size != 1) "s" else ""}"
            container.removeAllViews()
            val grouped = expenses.groupBy { it.category }
            categories.forEach { category ->
                val items = grouped[category]
                if (!items.isNullOrEmpty()) {
                    val categoryTotal = items.sumOf { it.amount }
                    val emoji = categoryEmojis[category] ?: "💰"
                    container.addView(buildCategoryRow(category, emoji, categoryTotal, items.size))
                }
            }
        }


        }
    private fun buildCategoryRow(
        category: String, emoji: String, total: Double, count: Int
    ): MaterialCardView {
        val card = MaterialCardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 12.dpToPx() }
            radius = 12.dpToPx().toFloat()
            cardElevation = 2.dpToPx().toFloat()
        }

        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val tvEmoji = TextView(requireContext()).apply {
            text = emoji
            textSize = 24f
            layoutParams = LinearLayout.LayoutParams(48.dpToPx(), 48.dpToPx())
            gravity = android.view.Gravity.CENTER
        }

        val middle = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ).also { it.marginStart = 12.dpToPx() }
        }
        val tvName = TextView(requireContext()).apply {
            text = category
            textSize = 15f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        val tvItemCount = TextView(requireContext()).apply {
            text = "$count item${if (count != 1) "s" else ""}"
            textSize = 13f
        }
        middle.addView(tvName)
        middle.addView(tvItemCount)

        val tvAmount = TextView(requireContext()).apply {
            text = "R%.2f".format(total)
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        row.addView(tvEmoji)
        row.addView(middle)
        row.addView(tvAmount)
        card.addView(row)
        return card
    }

    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).toInt()
}