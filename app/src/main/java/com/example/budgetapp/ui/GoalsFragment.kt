package com.example.budgetapp.ui

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.budgetapp.GoalManager
import com.example.budgetapp.R
import com.example.budgetapp.viewmodel.ExpenseViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class GoalsFragment : Fragment() {

    private lateinit var goalManager: GoalManager
    private lateinit var viewModel: ExpenseViewModel

    // Selected target date timestamp — defaults to existing saved value or 0
    private var selectedTargetDate: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_goals, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        goalManager = GoalManager(requireContext())
        viewModel   = ViewModelProvider(requireActivity())[ExpenseViewModel::class.java]

        val tvGoalStatus = view.findViewById<TextView>(R.id.tvGoalStatus)
        val etMinGoal    = view.findViewById<TextInputEditText>(R.id.etMinGoal)
        val etMaxGoal    = view.findViewById<TextInputEditText>(R.id.etMaxGoal)
        val tilMinGoal   = view.findViewById<TextInputLayout>(R.id.tilMinGoal)
        val tilMaxGoal   = view.findViewById<TextInputLayout>(R.id.tilMaxGoal)
        val etTargetDate = view.findViewById<TextInputEditText>(R.id.etTargetDate)
        val tilTargetDate = view.findViewById<TextInputLayout>(R.id.tilTargetDate)
        val btnSaveGoals = view.findViewById<MaterialButton>(R.id.btnSaveGoals)

        // Pre-fill saved goals
        if (goalManager.minimumGoal > 0)
            etMinGoal.setText(goalManager.minimumGoal.toString())
        if (goalManager.maximumGoal > 0)
            etMaxGoal.setText(goalManager.maximumGoal.toString())

        // Pre-fill saved target date
        selectedTargetDate = goalManager.goalTargetDate
        if (selectedTargetDate > 0) {
            etTargetDate.setText(formatDate(selectedTargetDate))
        }

        // Date picker — only allow future dates
        etTargetDate.setOnClickListener { showDatePicker(etTargetDate) }
        tilTargetDate.setEndIconOnClickListener { showDatePicker(etTargetDate) }

        // Observe total to show live goal status
        viewModel.totalAmount.observe(viewLifecycleOwner) { total ->
            updateGoalStatus(tvGoalStatus, total ?: 0.0)
        }

        btnSaveGoals.setOnClickListener {
            val minText = etMinGoal.text.toString().trim()
            val maxText = etMaxGoal.text.toString().trim()
            var valid   = true

            tilMinGoal.error = null
            tilMaxGoal.error = null
            tilTargetDate.error = null

            val min = minText.toDoubleOrNull() ?: 0.0
            val max = maxText.toDoubleOrNull() ?: 0.0

            if (minText.isNotEmpty() && min <= 0) {
                tilMinGoal.error = "Enter a valid amount"
                valid = false
            }
            if (maxText.isNotEmpty() && max <= 0) {
                tilMaxGoal.error = "Enter a valid amount"
                valid = false
            }
            if (minText.isNotEmpty() && maxText.isNotEmpty() && min >= max) {
                tilMaxGoal.error = "Maximum must be greater than minimum"
                valid = false
            }

            if (valid) {
                goalManager.minimumGoal = min
                goalManager.maximumGoal = max
                goalManager.goalTargetDate = selectedTargetDate

                viewModel.totalAmount.value?.let {
                    updateGoalStatus(tvGoalStatus, it ?: 0.0)
                }
                Toast.makeText(requireContext(), "Goals saved ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ── Date Picker ────────────────────────────────────────────────────────────
    private fun showDatePicker(etTargetDate: TextInputEditText) {
        val calendar = Calendar.getInstance()
        if (selectedTargetDate > 0) calendar.timeInMillis = selectedTargetDate

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val picked = Calendar.getInstance().apply {
                    set(year, month, day, 23, 59, 59)
                }
                selectedTargetDate = picked.timeInMillis
                etTargetDate.setText(formatDate(selectedTargetDate))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            // Block past dates — target must be in the future
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }

    private fun updateGoalStatus(tvGoalStatus: TextView, total: Double) {
        if (!goalManager.hasGoals()) {
            tvGoalStatus.text = "No goals set yet"
            tvGoalStatus.setTextColor(Color.GRAY)
            return
        }
        val min = goalManager.minimumGoal
        val max = goalManager.maximumGoal
        when {
            max > 0 && total > max -> {
                tvGoalStatus.text = "⚠ Over maximum! (R%.2f over)".format(total - max)
                tvGoalStatus.setTextColor(Color.RED)
            }
            min > 0 && total < min -> {
                tvGoalStatus.text = " Below minimum (R%.2f remaining)".format(min - total)
                tvGoalStatus.setTextColor(Color.parseColor("#FF8C00"))
            }
            else -> {
                tvGoalStatus.text = " Within your goals!"
                tvGoalStatus.setTextColor(Color.parseColor("#16A34A"))
            }
        }
    }

    private fun formatDate(timestamp: Long): String =
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
}