package com.example.budgetapp.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.budgetapp.GoalManager
import com.example.budgetapp.R
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsFragment : Fragment() {

    private lateinit var goalManager: GoalManager

    // SharedPreferences key for storing toggle states
    private val PREFS_NAME    = "settings_prefs"
    private val KEY_DARK_MODE = "dark_mode"
    private val KEY_NOTIFS    = "notifications"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        goalManager = GoalManager(requireContext())

        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        //  Username
        val accountPrefs = requireContext().getSharedPreferences(
            "expense_goals", Context.MODE_PRIVATE
        )
        view.findViewById<TextView>(R.id.tvSettingsUsername).text =
            accountPrefs.getString("current_user", goalManager.currentUser)

        //  Dark Mode toggle
        val switchDarkMode = view.findViewById<SwitchMaterial>(R.id.switchDarkMode)

        // Set switch to match current saved state
        switchDarkMode.isChecked = prefs.getBoolean(KEY_DARK_MODE, false)

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            // Save preference
            prefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply()

            // Apply dark/light mode immediately across the whole app
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // ── Notifications toggle (UI only — no functionality yet) ─────────────
        val switchNotifications = view.findViewById<SwitchMaterial>(R.id.switchNotifications)
        switchNotifications.isChecked = prefs.getBoolean(KEY_NOTIFS, false)

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            // Save state so it persists across sessions
            prefs.edit().putBoolean(KEY_NOTIFS, isChecked).apply()
            // No actual notification logic yet — coming soon
        }

        //  About dialog
        view.findViewById<LinearLayout>(R.id.rowAbout).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("About SmartBudget Pro")
                .setMessage(
                    "SmartBudget Pro v1.0.0\n\n" +
                            "Built as a final year project to help you track, manage and understand " +
                            "your personal spending.\n\n" +
                            "Features:\n" +
                            "• Track expenses with photos\n" +
                            "• Set monthly spending goals\n" +
                            "• View spending by category\n" +
                            "• Multiple account support\n" +
                            "• Search your expense history\n\n" +
                            "Built with Kotlin & Android Studio 🚀"
                )
                .setPositiveButton("Close", null)
                .show()
        }

        //  Privacy Policy dialog
        view.findViewById<LinearLayout>(R.id.rowPrivacy).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Privacy Policy")
                .setMessage(
                    "Last updated: April 2026\n\n" +
                            "YOUR DATA STAYS ON YOUR DEVICE\n" +
                            "All your expense data is stored locally on your device only. " +
                            "We do not collect, transmit or share any of your personal or " +
                            "financial information.\n\n" +
                            "PHOTOS\n" +
                            "Photos attached to expenses are stored in your device's private " +
                            "app storage and are never uploaded anywhere.\n\n" +
                            "ACCOUNT INFORMATION\n" +
                            "Usernames and passwords are stored locally in an encrypted database " +
                            "on your device only.\n\n" +
                            "THIRD PARTIES\n" +
                            "This app does not use any third-party analytics, advertising or " +
                            "tracking services.\n\n" +
                            "CONTACT\n" +
                            "For any questions about this policy, contact the developer."
                )
                .setPositiveButton("I Understand", null)
                .show()
        }
    }
}