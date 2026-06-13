package com.example.budgetapp.ui

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.budgetapp.Data.Expense
import com.example.budgetapp.GoalManager
import com.example.budgetapp.R
import com.example.budgetapp.viewmodel.ExpenseViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SummaryFragment : Fragment() {

    private val TAG = "SummaryFragment"

    private lateinit var viewModel: ExpenseViewModel
    private lateinit var goalManager: GoalManager

    // Period filter: 0 = week, 1 = month, 2 = all time, 3 = custom
    private var selectedPeriod = 1
    private var startDate: Long? = null
    private var endDate: Long? = null

    private val categoryEmojis = mapOf(
        "Food" to "🍔", "Transport" to "🚗", "Entertainment" to "🎬",
        "Shopping" to "🛍️", "Health" to "💊", "Education" to "📚", "Other" to "💰"
    )

    // Chart colors matching app theme
    private val chartColors = listOf(
        Color.parseColor("#1565C0"), Color.parseColor("#2E7D32"),
        Color.parseColor("#F57F17"), Color.parseColor("#6A1B9A"),
        Color.parseColor("#C62828"), Color.parseColor("#00838F"),
        Color.parseColor("#4E342E")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel   = ViewModelProvider(requireActivity())[ExpenseViewModel::class.java]
        goalManager = GoalManager(requireContext())

        val btnWeek      = view.findViewById<MaterialButton>(R.id.btnWeek)
        val btnMonth     = view.findViewById<MaterialButton>(R.id.btnMonth)
        val btnAllTime   = view.findViewById<MaterialButton>(R.id.btnAllTime)
        val btnDateRange = view.findViewById<MaterialButton>(R.id.btnDateRange)

        // Period selector buttons
        btnWeek.setOnClickListener {
            selectedPeriod = 0
            startDate = null
            endDate = null
            btnDateRange.text = "Custom Date Range"
            updateButtonStyles(btnWeek, btnMonth, btnAllTime, 0)
            refreshCharts(view)
        }
        btnMonth.setOnClickListener {
            selectedPeriod = 1
            startDate = null
            endDate = null
            btnDateRange.text = "Custom Date Range"
            updateButtonStyles(btnWeek, btnMonth, btnAllTime, 1)
            refreshCharts(view)
        }
        btnAllTime.setOnClickListener {
            selectedPeriod = 2
            startDate = null
            endDate = null
            btnDateRange.text = "Custom Date Range"
            updateButtonStyles(btnWeek, btnMonth, btnAllTime, 2)
            refreshCharts(view)
        }

        // Custom Date Range Picker
        btnDateRange.setOnClickListener {
            val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Dates")
                .setSelection(
                    Pair(
                        startDate ?: MaterialDatePicker.todayInUtcMilliseconds(),
                        endDate ?: MaterialDatePicker.todayInUtcMilliseconds()
                    )
                )
                .build()

            dateRangePicker.show(childFragmentManager, "SUMMARY_DATE_PICKER")

            dateRangePicker.addOnPositiveButtonClickListener { selection ->
                startDate = selection.first
                endDate = selection.second
                selectedPeriod = 3

                // Format for display
                val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val startStr = sdf.format(Date(startDate!!))
                val endStr = sdf.format(Date(endDate!!))
                btnDateRange.text = "Range: $startStr - $endStr"

                updateButtonStyles(btnWeek, btnMonth, btnAllTime, 3)
                refreshCharts(view)
            }
        }

        // Observe all expenses
        viewModel.allExpenses.observe(viewLifecycleOwner) { expenses ->
            val filtered = filterByPeriod(expenses)
            val total    = filtered.sumOf { it.amount }

            view.findViewById<TextView>(R.id.tvTotalAmount).text =
                "R%.2f".format(total)

            updateBarChart(view, filtered)
            updatePieChart(view, filtered, total)
            updateCategoryBreakdown(view, filtered, total)
        }
    }

    private fun filterByPeriod(expenses: List<Expense>): List<Expense> {
        val now = System.currentTimeMillis()
        return when (selectedPeriod) {
            0 -> { // This week
                val weekAgo = now - (7 * 24 * 60 * 60 * 1000L)
                expenses.filter { it.date >= weekAgo }
            }
            1 -> { // This month
                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                expenses.filter { it.date >= cal.timeInMillis }
            }
            2 -> expenses // All time
            3 -> { // Custom range
                if (startDate != null && endDate != null) {
                    val adjustedEnd = endDate!! + 86399999L // End of day
                    expenses.filter { it.date in startDate!!..adjustedEnd }
                } else expenses
            }
            else -> expenses
        }
    }

    private fun refreshCharts(view: View) {
        viewModel.allExpenses.value?.let { expenses ->
            val filtered = filterByPeriod(expenses)
            val total    = filtered.sumOf { it.amount }
            view.findViewById<TextView>(R.id.tvTotalAmount).text = "R%.2f".format(total)
            updateBarChart(view, filtered)
            updatePieChart(view, filtered, total)
            updateCategoryBreakdown(view, filtered, total)
        }
    }

    private fun updateBarChart(view: View, expenses: List<Expense>) {
        val barChart = view.findViewById<BarChart>(R.id.barChart)
        val customCategories = goalManager.getCustomCategories()

        val grouped = expenses.groupBy { it.category }
        val labels  = mutableListOf<String>()
        val entries = mutableListOf<BarEntry>()
        val colors  = mutableListOf<Int>()

        customCategories.forEachIndexed { index, category ->
            val total = grouped[category]?.sumOf { it.amount } ?: 0.0
            if (total > 0) {
                entries.add(BarEntry(entries.size.toFloat(), total.toFloat()))
                labels.add(category.take(5))
                colors.add(chartColors[index % chartColors.size])
            }
        }

        if (entries.isEmpty()) {
            barChart.clear()
            barChart.setNoDataText("No expenses for this period")
            barChart.invalidate()
            return
        }

        val dataSet = BarDataSet(entries, "Spending").apply {
            this.colors = colors
            valueTextSize  = 10f
            valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getFormattedValue(value: Float) = "R%.0f".format(value)
            }
        }

        barChart.apply {
            data = BarData(dataSet).apply { barWidth = 0.6f }
            description.isEnabled = false
            legend.isEnabled      = false
            setTouchEnabled(false)
            setDrawGridBackground(false)
            setDrawBorders(false)
            animateY(800)

            val axisLeft = axisLeft
            axisLeft.removeAllLimitLines()
            if (goalManager.minimumGoal > 0) {
                axisLeft.addLimitLine(LimitLine(
                    goalManager.minimumGoal.toFloat(), "Min"
                ).apply {
                    lineColor    = Color.parseColor("#FF8C00")
                    lineWidth    = 2f
                    textColor    = Color.parseColor("#FF8C00")
                    textSize     = 10f
                    enableDashedLine(10f, 5f, 0f)
                })
            }
            if (goalManager.maximumGoal > 0) {
                axisLeft.addLimitLine(LimitLine(
                    goalManager.maximumGoal.toFloat(), "Max"
                ).apply {
                    lineColor = Color.RED
                    lineWidth = 2f
                    textColor = Color.RED
                    textSize  = 10f
                    enableDashedLine(10f, 5f, 0f)
                })
            }

            axisRight.isEnabled = false
            xAxis.apply {
                valueFormatter  = IndexAxisValueFormatter(labels)
                granularity     = 1f
                setDrawGridLines(false)
                position        = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            }
            invalidate()
        }
    }

    private fun updatePieChart(view: View, expenses: List<Expense>, total: Double) {
        val pieChart    = view.findViewById<PieChart>(R.id.pieChart)
        val tvGoalSummary = view.findViewById<TextView>(R.id.tvGoalSummary)
        val max = goalManager.maximumGoal

        if (!goalManager.hasGoals()) {
            pieChart.clear()
            pieChart.setNoDataText("Set goals to see budget overview")
            pieChart.invalidate()
            tvGoalSummary.text = "Set goals in the hamburger menu to see this chart"
            return
        }

        val spent     = total.toFloat().coerceAtMost(max.toFloat())
        val remaining = (max - total).toFloat().coerceAtLeast(0f)

        val entries = mutableListOf<PieEntry>()
        val colors  = mutableListOf<Int>()

        entries.add(PieEntry(spent, "Spent"))
        colors.add(when {
            total > max -> Color.RED
            total > max * 0.8 -> Color.parseColor("#FF8C00")
            else -> Color.parseColor("#2E7D32")
        })

        if (remaining > 0) {
            entries.add(PieEntry(remaining, "Remaining"))
            colors.add(Color.parseColor("#E0E0E0"))
        }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors        = colors
            valueTextSize      = 12f
            valueTextColor     = Color.WHITE
            sliceSpace         = 3f
        }

        pieChart.apply {
            data              = PieData(dataSet)
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius        = 55f
            setHoleColor(Color.TRANSPARENT)
            setCenterText("R%.0f\nof R%.0f".format(total, max))
            setCenterTextSize(13f)
            setCenterTextColor(resources.getColor(R.color.text_primary, null))
            setCenterTextTypeface(Typeface.DEFAULT_BOLD)
            setTouchEnabled(false)
            legend.isEnabled  = false
            animateY(1000)
            invalidate()
        }

        tvGoalSummary.text = when {
            total > max -> "⚠️ R%.2f over maximum budget!".format(total - max)
            else -> "✅ On track! R%.2f remaining in budget".format(max - total)
        }
    }

    private fun updateCategoryBreakdown(
        view: View,
        expenses: List<Expense>,
        total: Double
    ) {
        val container = view.findViewById<LinearLayout>(R.id.categoryContainer)
        container.removeAllViews()

        if (expenses.isEmpty()) return

        val grouped = expenses.groupBy { it.category }
        goalManager.getCustomCategories().forEach { category ->
            val items = grouped[category]
            if (!items.isNullOrEmpty()) {
                val categoryTotal = items.sumOf { it.amount }
                val emoji = categoryEmojis[category] ?: "💰"
                container.addView(buildCategoryRow(category, emoji, categoryTotal, items.size))
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
            radius        = 12.dpToPx().toFloat()
            cardElevation = 2.dpToPx().toFloat()
        }
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
            gravity     = android.view.Gravity.CENTER_VERTICAL
        }
        val tvEmoji = TextView(requireContext()).apply {
            text        = emoji
            textSize    = 24f
            layoutParams = LinearLayout.LayoutParams(48.dpToPx(), 48.dpToPx())
            gravity     = android.view.Gravity.CENTER
        }
        val middle = LinearLayout(requireContext()).apply {
            orientation  = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ).also { it.marginStart = 12.dpToPx() }
        }
        val tvName = TextView(requireContext()).apply {
            text     = category
            textSize = 15f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        val tvCount = TextView(requireContext()).apply {
            text     = "$count item${if (count != 1) "s" else ""}"
            textSize = 13f
        }
        middle.addView(tvName)
        middle.addView(tvCount)
        val tvAmount = TextView(requireContext()).apply {
            text     = "R%.2f".format(total)
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(resources.getColor(R.color.blue_primary, null))
        }
        row.addView(tvEmoji)
        row.addView(middle)
        row.addView(tvAmount)
        card.addView(row)
        return card
    }

    private fun updateButtonStyles(
        btnWeek: MaterialButton,
        btnMonth: MaterialButton,
        btnAllTime: MaterialButton,
        selected: Int
    ) {
        val activeColor   = resources.getColorStateList(R.color.blue_primary, null)
        val inactiveColor = resources.getColorStateList(android.R.color.transparent, null)

        listOf(btnWeek, btnMonth, btnAllTime).forEachIndexed { index, btn ->
            if (index == selected) {
                btn.backgroundTintList = activeColor
                btn.setTextColor(Color.WHITE)
            } else {
                btn.backgroundTintList = inactiveColor
                btn.setTextColor(resources.getColor(R.color.blue_primary, null))
            }
        }
    }

    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).toInt()
}