package com.example.budgetapp.bills

import android.os.Bundle
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
import com.google.android.material.card.MaterialCardView
import java.util.concurrent.TimeUnit

class BillsFragment : Fragment() {

    private lateinit var viewModel: BillViewModel
    private lateinit var adapter: BillAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bills, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[BillViewModel::class.java]

        adapter = BillAdapter(
            onMarkPaid = { bill ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Mark as Paid")
                    .setMessage(
                        if (bill.isRecurring)
                            "Mark ${bill.name} as paid? The next due date will be set automatically."
                        else
                            "Mark ${bill.name} as paid?"
                    )
                    .setPositiveButton("Mark Paid") { _, _ ->
                        viewModel.markAsPaid(bill)
                        BillScheduler.cancelBill(requireContext(), bill)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onDelete = { bill ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Bill")
                    .setMessage("Delete ${bill.name} reminder?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteBill(bill)
                        BillScheduler.cancelBill(requireContext(), bill)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        val recyclerView   = view.findViewById<RecyclerView>(R.id.recyclerBills)
        val emptyState     = view.findViewById<LinearLayout>(R.id.emptyBillsState)
        val cardDueSoon    = view.findViewById<MaterialCardView>(R.id.cardDueSoon)
        val tvDueSoonText  = view.findViewById<TextView>(R.id.tvDueSoonText)
        val tvBillsSummary = view.findViewById<TextView>(R.id.tvBillsSummary)

        recyclerView.adapter      = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Observe all bills
        viewModel.allBills.observe(viewLifecycleOwner) { bills ->
            adapter.submitList(bills)

            if (bills.isEmpty()) {
                emptyState.visibility   = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyState.visibility   = View.GONE
                recyclerView.visibility = View.VISIBLE
            }

            val unpaid = bills.count { !it.isPaid }
            val total  = bills.filter { !it.isPaid }.sumOf { it.amount }
            tvBillsSummary.text = if (unpaid > 0)
                "$unpaid unpaid bill${if (unpaid != 1) "s" else ""} — R%.2f due".format(total)
            else "All bills paid! ✅"
        }

        // Due soon warning
        viewModel.billsDueSoon.observe(viewLifecycleOwner) { dueSoon ->
            if (dueSoon.isNotEmpty()) {
                cardDueSoon.visibility = View.VISIBLE
                val names = dueSoon.joinToString(", ") { bill ->
                    val days = TimeUnit.MILLISECONDS.toDays(
                        bill.dueDate - System.currentTimeMillis()
                    )
                    "${bill.name} (${if (days == 0L) "today" else "in $days days"})"
                }
                tvDueSoonText.text = "Due soon: $names"
            } else {
                cardDueSoon.visibility = View.GONE
            }
        }
    }
}