package com.example.budgetapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.forEach
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.budgetapp.bills.AddBillBottomSheet
import com.example.budgetapp.bills.BillsFragment
import com.example.budgetapp.ui.AchievementsFragment
import com.example.budgetapp.ui.AddExpenseFragment
import com.example.budgetapp.ui.DashboardFragment
import com.example.budgetapp.ui.GoalsFragment
import com.example.budgetapp.ui.HistoryFragment
import com.example.budgetapp.ui.ManageCategoriesFragment
import com.example.budgetapp.ui.SettingsFragment
import com.example.budgetapp.ui.SummaryFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fab: FloatingActionButton
    private lateinit var navigationView: NavigationView
    private lateinit var goalManager: GoalManager

    override fun onCreate(savedInstanceState: Bundle?) {

        // Restore dark mode preference on every launch
        val settingsPrefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        val isDarkMode    = settingsPrefs.getBoolean("dark_mode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        goalManager = GoalManager(this)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        //  Drawer
        drawerLayout   = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        bottomNav      = findViewById(R.id.bottom_navigation)
        fab            = findViewById(R.id.fab)

        // Hamburger toggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toggle.drawerArrowDrawable.color = getColor(R.color.white)

        // Set username in drawer header
        val headerView = navigationView.getHeaderView(0)
        headerView.findViewById<TextView>(R.id.tvDrawerUsername).text =
            goalManager.currentUser

        //  Drawer menu item clicks
        navigationView.setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawer(GravityCompat.START)

            when (item.itemId) {

                R.id.drawer_add_account -> {
                    // Small delay so drawer closes smoothly before navigating
                    drawerLayout.postDelayed({
                        startActivity(Intent(this, RegisterActivity::class.java))
                    }, 250)
                    true
                }

                R.id.drawer_goals -> {
                    drawerLayout.postDelayed({
                        loadFragment(GoalsFragment(), "SmartBudget Pro")
                        // Deselect bottom nav since Goals isn't a tab
                        bottomNav.menu.setGroupCheckable(0, true, false)
                        bottomNav.menu.forEach { it.isChecked = false }
                        bottomNav.menu.setGroupCheckable(0, true, true)
                        fab.setImageResource(R.drawable.ic_add)
                        fab.show()
                    }, 250)
                    true
                }

                R.id.drawer_manage_categories -> {
                    drawerLayout.postDelayed({
                        loadFragment(ManageCategoriesFragment(), "Manage Categories")
                        bottomNav.menu.setGroupCheckable(0, true, false)
                        bottomNav.menu.forEach { it.isChecked = false }
                        bottomNav.menu.setGroupCheckable(0, true, true)
                        fab.setImageResource(R.drawable.ic_add)
                        fab.show()
                    }, 250)
                    true
                }

                R.id.drawer_settings -> {
                    drawerLayout.postDelayed({
                        loadFragment(SettingsFragment(), "SmartBudget Pro")
                        bottomNav.menu.setGroupCheckable(0, true, false)
                        bottomNav.menu.forEach { it.isChecked = false }
                        bottomNav.menu.setGroupCheckable(0, true, true)
                        fab.setImageResource(R.drawable.ic_add)
                        fab.show()
                    }, 250)
                    true
                }

                R.id.nav_bills -> {
                    loadFragment(BillsFragment(), "Bill Reminders")
                    bottomNav.selectedItemId = R.id.nav_bills
                    // Change FAB to add bill when on bills screen
                    fab.setImageResource(R.drawable.ic_notifications)
                    fab.show()
                    true
                }

                R.id.drawer_logout -> {
                    drawerLayout.postDelayed({
                        AlertDialog.Builder(this)
                            .setTitle("SmartBudget Pro")
                            .setMessage("Are you sure you want to logout?")
                            .setPositiveButton("Logout") { _, _ ->
                                goalManager.logout()
                                val intent = Intent(this, LoginActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }, 250)
                    true
                }

                R.id.drawer_achievements -> {
                    drawerLayout.postDelayed({
                        loadFragment(AchievementsFragment(), "Achievements")
                        fab.setImageResource(R.drawable.ic_add)
                        fab.show()
                    }, 250)
                    true
                }

                else -> false
            }
        }

        //  bottom nav listener
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(DashboardFragment(), "SmartBudget Pro")
                    fab.setImageResource(R.drawable.ic_add)
                    fab.show()
                    true
                }
                R.id.nav_add -> {
                    loadFragment(AddExpenseFragment(), "SmartBudget Pro")
                    fab.hide()
                    true
                }
                R.id.nav_history -> {
                    loadFragment(HistoryFragment(), "SmartBudget Pro")
                    fab.setImageResource(R.drawable.ic_add)
                    fab.show()
                    true
                }
                R.id.nav_summary -> {
                    loadFragment(SummaryFragment(), "SmartBudget Pro")
                    fab.setImageResource(R.drawable.ic_add)
                    fab.show()
                    true
                }
                R.id.nav_bills -> {
                    loadFragment(BillsFragment(), "Bill Reminders")
                    fab.setImageResource(R.drawable.ic_notifications)
                    fab.show()
                    true
                }
                else -> false
            }
        }

        fab.setOnClickListener {
            val currentFragment = supportFragmentManager
                .findFragmentById(R.id.fragment_container)
            if (currentFragment is BillsFragment) {
                // Open add bill bottom sheet
                AddBillBottomSheet().show(supportFragmentManager, "AddBill")
            } else {
                loadFragment(AddExpenseFragment(), "Add Expense")
                bottomNav.selectedItemId = R.id.nav_add
            }
        }

// Update default screen — Dashboard loads first now
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment(), "SmartBudget Pro")
            bottomNav.selectedItemId = R.id.nav_home
            fab.setImageResource(R.drawable.ic_add)
            fab.show()
        }

        //  Handle back button for the navigation drawer
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }

    fun loadFragment(fragment: Fragment, title: String) {
        supportActionBar?.title = title
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    //
    fun navigateTo(navItemId: Int, title: String) {
        bottomNav.selectedItemId = navItemId
        val fragment = when (navItemId) {
            R.id.nav_home    -> DashboardFragment()
            R.id.nav_add     -> AddExpenseFragment()
            R.id.nav_history -> HistoryFragment()
            R.id.nav_summary -> SummaryFragment()
            R.id.nav_bills   -> BillsFragment()
            else             -> DashboardFragment()
        }
        loadFragment(fragment, title)
        if (navItemId == R.id.nav_add) {
            fab.hide()
        } else if (navItemId == R.id.nav_bills) {
            fab.setImageResource(R.drawable.ic_notifications)
            fab.show()
        } else {
            fab.setImageResource(R.drawable.ic_add)
            fab.show()
        }
    }
}
