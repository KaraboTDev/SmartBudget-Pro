package com.example.budgetapp.bills

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.budgetapp.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddBillBottomSheet : BottomSheetDialogFragment() {

    private lateinit var viewModel: BillViewModel
    private var selectedDueDate = System.currentTimeMillis()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_add_bill, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[BillViewModel::class.java]

        val etBillName     = view.findViewById<TextInputEditText>(R.id.etBillName)
        val etBillAmount   = view.findViewById<TextInputEditText>(R.id.etBillAmount)
        val etBillDate     = view.findViewById<TextInputEditText>(R.id.etBillDate)
        val etBillNotes    = view.findViewById<TextInputEditText>(R.id.etBillNotes)
        val tilBillName    = view.findViewById<TextInputLayout>(R.id.tilBillName)
        val tilBillAmount  = view.findViewById<TextInputLayout>(R.id.tilBillAmount)
        val switchRecurring = view.findViewById<SwitchMaterial>(R.id.switchRecurring)
        val tilRecurring   = view.findViewById<TextInputLayout>(R.id.tilRecurringType)
        val actvRecurring  = view.findViewById<AutoCompleteTextView>(R.id.actvRecurringType)
        val btnSaveBill    = view.findViewById<MaterialButton>(R.id.btnSaveBill)

        // Set default date to today
        etBillDate.setText(formatDate(selectedDueDate))

        // Date picker
        etBillDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                val picked = Calendar.getInstance().apply { set(y, m, d, 8, 0, 0) }
                selectedDueDate = picked.timeInMillis
                etBillDate.setText(formatDate(selectedDueDate))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Recurring type dropdown
        val recurringTypes = listOf("Weekly", "Monthly", "Yearly")
        actvRecurring.setAdapter(
            ArrayAdapter(requireContext(),
                android.R.layout.simple_dropdown_item_1line, recurringTypes)
        )
        actvRecurring.setText("Monthly", false)

        // Show/hide recurring type based on toggle
        switchRecurring.setOnCheckedChangeListener { _, isChecked ->
            tilRecurring.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Save bill
        btnSaveBill.setOnClickListener {
            val name   = etBillName.text.toString().trim()
            val amount = etBillAmount.text.toString().trim()

            if (name.isEmpty()) {
                tilBillName.error = "Please enter a bill name"
                return@setOnClickListener
            }
            if (amount.isEmpty() || amount.toDoubleOrNull() == null) {
                tilBillAmount.error = "Please enter a valid amount"
                return@setOnClickListener
            }

            val bill = Bill(
                name          = name,
                amount        = amount.toDouble(),
                dueDate       = selectedDueDate,
                isRecurring   = switchRecurring.isChecked,
                recurringType = actvRecurring.text.toString().lowercase(),
                notes         = etBillNotes.text.toString().trim()
            )

            viewModel.addBill(bill)

            // Schedule the notification alarm
            BillScheduler.scheduleBill(requireContext(), bill)

            Toast.makeText(requireContext(), "${bill.name} reminder set! 🔔", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun formatDate(timestamp: Long) =
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
}