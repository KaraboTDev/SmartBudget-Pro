package com.example.budgetapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetapp.GoalManager
import com.example.budgetapp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ManageCategoriesFragment : Fragment() {

    private lateinit var goalManager: GoalManager
    private lateinit var adapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_manage_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        goalManager = GoalManager(requireContext())

        val etNewCategory = view.findViewById<TextInputEditText>(R.id.etNewCategory)
        val btnAddCategory = view.findViewById<MaterialButton>(R.id.btnAddCategory)
        val rvCategories = view.findViewById<RecyclerView>(R.id.rvCategories)

        adapter = CategoryAdapter(
            goalManager.getCustomCategories(),
            onDelete = { category ->
                if (goalManager.getCustomCategories().size <= 1) {
                    Toast.makeText(requireContext(), "You must have at least one category", Toast.LENGTH_SHORT).show()
                } else {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Delete Category")
                        .setMessage("Delete \"$category\"? Expenses already using this category will not be changed.")
                        .setPositiveButton("Delete") { _, _ ->
                            goalManager.removeCategory(category)
                            refreshList()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        )

        rvCategories.adapter = adapter
        rvCategories.layoutManager = LinearLayoutManager(requireContext())

        btnAddCategory.setOnClickListener {
            val name = etNewCategory.text.toString().trim()
            if (name.isNotEmpty()) {
                val current = goalManager.getCustomCategories()
                if (current.contains(name)) {
                    Toast.makeText(requireContext(), "Category already exists", Toast.LENGTH_SHORT).show()
                } else {
                    goalManager.addCategory(name)
                    etNewCategory.text?.clear()
                    refreshList()
                    Toast.makeText(requireContext(), "$name added!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun refreshList() {
        adapter.updateCategories(goalManager.getCustomCategories())
    }

    private class CategoryAdapter(
        private var categories: List<String>,
        private val onDelete: (String) -> Unit
    ) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvCategoryName)
            val btnDelete: MaterialButton = view.findViewById(R.id.btnDeleteCategory)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val cat = categories[position]
            holder.tvName.text = cat
            holder.btnDelete.setOnClickListener {
                onDelete(cat)
            }
        }

        override fun getItemCount() = categories.size

        fun updateCategories(newCategories: List<String>) {
            categories = newCategories
            notifyDataSetChanged()
        }
    }
}