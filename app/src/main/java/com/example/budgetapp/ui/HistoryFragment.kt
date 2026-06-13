package com.example.budgetapp.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetapp.GoalManager
import com.example.budgetapp.R
import com.example.budgetapp.viewmodel.ExpenseViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class HistoryFragment : Fragment() {

    private lateinit var viewModel: ExpenseViewModel
    private lateinit var goalManager: GoalManager
    private lateinit var adapter: ExpenseAdapter

    private var startDate: Long? = null
    private var endDate: Long? = null
    private var searchQuery: String = ""
    private var selectedCategory: String? = null

    private var currentSearchObserver: androidx.lifecycle.Observer<List<com.example.budgetapp.Data.Expense>>? = null
    private var currentLiveData: androidx.lifecycle.LiveData<List<com.example.budgetapp.Data.Expense>>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[ExpenseViewModel::class.java]
        goalManager = GoalManager(requireContext())

        adapter = ExpenseAdapter { expense ->
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Expense")
                .setMessage("Delete \"${expense.description}\"?")
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deleteExpense(expense)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        val recyclerView    = view.findViewById<RecyclerView>(R.id.recyclerView)
        val emptyState      = view.findViewById<LinearLayout>(R.id.emptyState)
        val tvCount         = view.findViewById<TextView>(R.id.tvExpenseCount)
        val tvEmptyMessage  = view.findViewById<TextView>(R.id.tvEmptyMessage)
        val tvEmptySubMsg   = view.findViewById<TextView>(R.id.tvEmptySubMessage)
        val etSearch        = view.findViewById<TextInputEditText>(R.id.etSearch)
        val btnDateRange    = view.findViewById<MaterialButton>(R.id.btnDateRange)
        val chipGroup       = view.findViewById<ChipGroup>(R.id.chipGroupCategories)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Set up Category Chips
        setupCategoryChips(chipGroup, recyclerView, emptyState, tvCount, tvEmptyMessage, tvEmptySubMsg)

        // Initial load
        refreshData(recyclerView, emptyState, tvCount, tvEmptyMessage, tvEmptySubMsg)

        // Date Range Picker
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

            dateRangePicker.show(childFragmentManager, "DATE_RANGE_PICKER")

            dateRangePicker.addOnPositiveButtonClickListener { selection ->
                startDate = selection.first
                endDate = selection.second

                val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val startStr = sdf.format(Date(startDate!!))
                val endStr = sdf.format(Date(endDate!!))
                
                btnDateRange.text = "Range: $startStr - $endStr"
                refreshData(recyclerView, emptyState, tvCount, tvEmptyMessage, tvEmptySubMsg)
            }
        }

        btnDateRange.setOnLongClickListener {
            startDate = null
            endDate = null
            btnDateRange.text = "Select Date Range"
            refreshData(recyclerView, emptyState, tvCount, tvEmptyMessage, tvEmptySubMsg)
            true
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                searchQuery = s.toString().trim()
                refreshData(recyclerView, emptyState, tvCount, tvEmptyMessage, tvEmptySubMsg)
            }
        })
    }

    private fun setupCategoryChips(
        chipGroup: ChipGroup,
        recyclerView: RecyclerView,
        emptyState: LinearLayout,
        tvCount: TextView,
        tvEmptyMessage: TextView,
        tvEmptySubMsg: TextView
    ) {
        val categories = goalManager.getCustomCategories()
        
        // Add chips for each category
        categories.forEach { category ->
            val chip = layoutInflater.inflate(R.layout.view_filter_chip, chipGroup, false) as Chip
            chip.text = category
            chip.id = View.generateViewId()
            chipGroup.addView(chip)
        }

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull()
            val chip = checkedId?.let { group.findViewById<Chip>(it) }
            
            selectedCategory = if (chip == null || chip.id == R.id.chipAll) {
                null
            } else {
                chip.text.toString()
            }
            
            refreshData(recyclerView, emptyState, tvCount, tvEmptyMessage, tvEmptySubMsg)
        }
    }

    private fun refreshData(
        recyclerView: RecyclerView,
        emptyState: LinearLayout,
        tvCount: TextView,
        tvEmptyMessage: TextView,
        tvEmptySubMsg: TextView
    ) {
        currentSearchObserver?.let {
            currentLiveData?.removeObserver(it)
        }

        val liveData = when {
            // Category + Date Range
            selectedCategory != null && startDate != null && endDate != null -> {
                val adjustedEnd = endDate!! + 86399999L
                viewModel.getByCategoryAndDateRange(selectedCategory!!, startDate!!, adjustedEnd)
            }
            // Category Only
            selectedCategory != null -> {
                viewModel.getByCategory(selectedCategory!!)
            }
            // Date Range Only
            startDate != null && endDate != null -> {
                val adjustedEnd = endDate!! + 86399999L
                viewModel.getByDateRange(startDate!!, adjustedEnd)
            }
            // Search Only
            searchQuery.isNotEmpty() -> {
                viewModel.searchExpenses(searchQuery)
            }
            // Default: All
            else -> {
                viewModel.allExpenses
            }
        }

        val observer = androidx.lifecycle.Observer<List<com.example.budgetapp.Data.Expense>> { expenses ->
            adapter.submitList(expenses)

            if (expenses.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyState.visibility   = View.VISIBLE

                val filterMsg = when {
                    selectedCategory != null && startDate != null -> "No $selectedCategory expenses in this range"
                    selectedCategory != null -> "No expenses in category \"$selectedCategory\""
                    startDate != null -> "No expenses in this range"
                    searchQuery.isNotEmpty() -> "No results for \"$searchQuery\""
                    else -> "No expenses yet"
                }
                tvEmptyMessage.text = filterMsg
                tvEmptySubMsg.text = "Try clearing filters or search"
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyState.visibility   = View.GONE
                tvCount.text = "${expenses.size} item${if (expenses.size != 1) "s" else ""}"
            }
        }

        currentLiveData = liveData
        currentSearchObserver = observer
        liveData.observe(viewLifecycleOwner, observer)
    }
}