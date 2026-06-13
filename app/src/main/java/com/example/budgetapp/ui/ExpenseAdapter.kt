package com.example.budgetapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.budgetapp.Data.Expense
import com.example.budgetapp.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseAdapter(
    private val onDeleteClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem == newItem
        }
    }

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val ivCategoryBadge: ImageView = itemView.findViewById(R.id.ivCategoryBadge)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        private val ivExpensePhoto: ImageView = itemView.findViewById(R.id.ivExpensePhoto)

        fun bind(expense: Expense) {
            tvDescription.text = expense.description
            tvCategory.text = expense.category
            tvAmount.text = "R%.2f".format(expense.amount)
            tvDate.text = formatDate(expense.date)
            ivCategoryBadge.setImageResource(getCategoryIcon(expense.category))

            btnDelete.setOnClickListener {
                onDeleteClick(expense)
            }

            // --- PHOTO LOADING WITH COIL ---
            // We use the Coil library (.load) instead of .setImageURI() to prevent the "too large bitmap" crash.
            // Coil is much better than basic Android methods because:
            // 1. It automatically resizes (downsamples) the image to fit the screen, which saves memory.
            // 2. It handles temporary URI permissions from the gallery automatically.
            // 3. It runs the image loading on a background thread, so the app stays smooth.
            if (!expense.photoPath.isNullOrEmpty()) {
                ivExpensePhoto.visibility = View.VISIBLE
                
                // .load() is an extension function provided by Coil
                ivExpensePhoto.load(expense.photoPath) {
                    crossfade(true) // Makes the photo fade in smoothly
                    placeholder(android.R.drawable.ic_menu_gallery) // Icon shown while the photo is loading
                    error(android.R.drawable.stat_notify_error) // Icon shown if the photo cannot be found
                }
            } else {
                // If there is no photo, hide the ImageView so it doesn't leave a blank gap
                ivExpensePhoto.visibility = View.GONE
            }
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

        private fun getCategoryIcon(category: String): Int {
            return when (category.lowercase()) {
                "food"          -> R.drawable.ic_food
                "transport"     -> R.drawable.ic_transport
                "entertainment" -> R.drawable.ic_entertainment
                "shopping"      -> R.drawable.ic_shopping
                "health"        -> R.drawable.ic_health
                "education"     -> R.drawable.ic_education
                else            -> R.drawable.ic_other
            }
        }
    }
}
