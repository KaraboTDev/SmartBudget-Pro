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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetapp.R
import com.example.budgetapp.viewmodel.ExpenseViewModel
import com.google.android.material.textfield.TextInputEditText

class HistoryFragment : Fragment() {

    private lateinit var viewModel: ExpenseViewModel
    private lateinit var adapter: ExpenseAdapter

    // Tracks the current search LiveData observer so we can swap it out
    // when the query changes without stacking up multiple observers
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

        // Set up adapter with delete confirmation dialog
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

        // Set up RecyclerView
        val recyclerView    = view.findViewById<RecyclerView>(R.id.recyclerView)
        val emptyState      = view.findViewById<LinearLayout>(R.id.emptyState)
        val tvCount         = view.findViewById<TextView>(R.id.tvExpenseCount)
        val tvEmptyMessage  = view.findViewById<TextView>(R.id.tvEmptyMessage)
        val tvEmptySubMsg   = view.findViewById<TextView>(R.id.tvEmptySubMessage)
        val etSearch        = view.findViewById<TextInputEditText>(R.id.etSearch)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Load all expenses by default (empty search query)
        observeExpenses(view, "", recyclerView, emptyState, tvCount, tvEmptyMessage, tvEmptySubMsg)

        // Search — fires every time the user types a character
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                observeExpenses(
                    view, query, recyclerView,
                    emptyState, tvCount, tvEmptyMessage, tvEmptySubMsg
                )
            }
        })
    }

    // ─── observeExpenses ───────────────────────────────────────────────────────
    // Swaps the LiveData being observed whenever the search query changes.
    // Without this swap, old observers would stack up and cause duplicate updates.
    private fun observeExpenses(
        view: View,
        query: String,
        recyclerView: RecyclerView,
        emptyState: LinearLayout,
        tvCount: TextView,
        tvEmptyMessage: TextView,
        tvEmptySubMsg: TextView
    ) {
        // Remove previous observer to prevent stacking
        currentSearchObserver?.let {
            currentLiveData?.removeObserver(it)
        }

        // Pick the right LiveData — all expenses or filtered search results
        val liveData = if (query.isEmpty()) {
            viewModel.allExpenses
        } else {
            viewModel.searchExpenses(query)
        }

        // Build and attach the new observer
        val observer = androidx.lifecycle.Observer<List<com.example.budgetapp.Data.Expense>> { expenses ->
            adapter.submitList(expenses)

            if (expenses.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyState.visibility   = View.VISIBLE

                // Show different messages for empty list vs no search results
                if (query.isEmpty()) {
                    tvEmptyMessage.text = "No expenses yet"
                    tvEmptySubMsg.text  = "Add your first expense on the Add tab"
                } else {
                    tvEmptyMessage.text = "No results for \"$query\""
                    tvEmptySubMsg.text  = "Try searching by name, category or date"
                }
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyState.visibility   = View.GONE
                tvCount.text = "${expenses.size} item${if (expenses.size != 1) "s" else ""}"
            }
        }

        // Store references so we can remove this observer next time query changes
        currentLiveData    = liveData
        currentSearchObserver = observer

        liveData.observe(viewLifecycleOwner, observer)
    }
}