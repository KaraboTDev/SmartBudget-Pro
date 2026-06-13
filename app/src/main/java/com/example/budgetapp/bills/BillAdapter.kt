package com.example.budgetapp.bills

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetapp.R
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class BillAdapter(
    private val onMarkPaid: (Bill) -> Unit,
    private val onDelete:   (Bill) -> Unit
) : ListAdapter<Bill, BillAdapter.BillViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Bill>() {
        override fun areItemsTheSame(oldItem: Bill, newItem: Bill) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Bill, newItem: Bill) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bill, parent, false)
        return BillViewHolder(view)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BillViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvBillIcon   = itemView.findViewById<TextView>(R.id.tvBillIcon)
        val tvBillName   = itemView.findViewById<TextView>(R.id.tvBillName)
        val tvBillDue    = itemView.findViewById<TextView>(R.id.tvBillDueDate)
        val tvBillType   = itemView.findViewById<TextView>(R.id.tvBillType)
        val tvBillAmount = itemView.findViewById<TextView>(R.id.tvBillAmount)
        val btnMarkPaid  = itemView.findViewById<MaterialButton>(R.id.btnMarkPaid)

        fun bind(bill: Bill) {
            tvBillName.text   = bill.name
            tvBillAmount.text = "R%.2f".format(bill.amount)
            tvBillType.text   = if (bill.isRecurring)
                bill.recurringType.replaceFirstChar { it.uppercase() }
            else "One-time"

            // Update button based on paid state
            if (bill.isPaid) {
                btnMarkPaid.text = "Paid"
                btnMarkPaid.isEnabled = false
                btnMarkPaid.alpha = 0.6f
                tvBillName.paintFlags = tvBillName.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                btnMarkPaid.text = "Pay"
                btnMarkPaid.isEnabled = true
                btnMarkPaid.alpha = 1.0f
                tvBillName.paintFlags = tvBillName.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            // Colour due date red if overdue, orange if due within 3 days
            val daysUntilDue = TimeUnit.MILLISECONDS.toDays(
                bill.dueDate - System.currentTimeMillis()
            )
            tvBillDue.text = when {
                daysUntilDue < 0  -> "⚠️ Overdue! (${formatDate(bill.dueDate)})"
                daysUntilDue == 0L -> "Due TODAY!"
                daysUntilDue <= 3  -> "Due in $daysUntilDue days"
                else               -> "Due: ${formatDate(bill.dueDate)}"
            }
            tvBillDue.setTextColor(
                when {
                    daysUntilDue < 0  -> android.graphics.Color.RED
                    daysUntilDue <= 3 -> android.graphics.Color.parseColor("#FF8C00")
                    else              -> itemView.context.getColor(R.color.text_secondary)
                }
            )

            // Bill category icon
            tvBillIcon.text = getBillIcon(bill.name)

            // Mark paid button
            btnMarkPaid.setOnClickListener { onMarkPaid(bill) }

            // Long press to delete
            itemView.setOnLongClickListener {
                onDelete(bill)
                true
            }
        }

        private fun getBillIcon(name: String): String {
            return when {
                name.contains("rent",      ignoreCase = true) -> "🏠"
                name.contains("netflix",   ignoreCase = true) -> "🎬"
                name.contains("data",      ignoreCase = true) -> "📱"
                name.contains("electric",  ignoreCase = true) -> "⚡"
                name.contains("water",     ignoreCase = true) -> "💧"
                name.contains("gym",       ignoreCase = true) -> "💪"
                name.contains("insurance", ignoreCase = true) -> "🛡️"
                name.contains("internet",  ignoreCase = true) -> "🌐"
                name.contains("spotify",   ignoreCase = true) -> "🎵"
                else                                          -> "📄"
            }
        }

        private fun formatDate(timestamp: Long): String =
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}
