# 💰 SmartBudget-Pro

> A professional personal finance Android application built with Kotlin for a Final Year college project at Rosebank College, Pretoria.

![Version](https://img.shields.io/badge/version-2.0.0-blue) ![Platform](https://img.shields.io/badge/platform-Android-green) ![Min SDK](https://img.shields.io/badge/minSdk-24-orange) ![Language](https://img.shields.io/badge/language-Kotlin-purple)

---

## 📱 App Overview

SmartBudget-Pro helps users take control of their personal finances by tracking expenses, setting monthly spending goals, visualising spending patterns through charts, and staying on top of recurring bills — all stored securely on-device using Room Database.

---

## ✨ Features

### Core Features
| Feature | Description |
|---|---|
| 🏠 Dashboard | Home screen with greeting, total spent, goal progress bar, recent expenses and category breakdown |
| ➕ Add Expense | Log expenses with amount, description, category, date picker and optional receipt photo |
| 🕐 Expense History | Full scrollable list with real-time search by name, category or date |
| 📊 Summary | Bar chart (spending per category) and pie chart (budget used vs remaining) with period selector |
| 🔐 Login & Register | Secure per-account login stored in Room Database |
| 👤 Multiple Accounts | Switch between different user accounts with separate goals and expenses |
| 🌙 Dark Mode | Full DayNight theme support, persists across sessions |
| 📷 Receipt Photos | Attach photos from camera or gallery to any expense |
| 🎯 Monthly Goals | Set minimum and maximum spending targets per account |
| 🔍 Search | Filter expense history in real time |
| ☰ Navigation Drawer | Hamburger menu with Goals, Settings, Achievements and Logout |
| 🏆 Achievements | XP system with 5 levels and 13 unlockable badges |
| 📈 Charts | MPAndroidChart bar and pie charts with goal limit lines |

---

## ⭐ Our Own Features 

### 1. 🔔 Bill Reminders
> *Unique feature designed and built by the team*

Users can add **one-time or recurring bills** (e.g. rent, Netflix, data, gym) with a due date and amount. The app schedules a real Android system notification that fires automatically on the due date — even when the app is closed.

**How it works:**
- Tap the Bills tab in the bottom navigation
- Tap the + FAB to add a bill (name, amount, due date, one-time or recurring)
- Recurring bills (weekly, monthly, yearly) automatically advance to the next cycle when marked as paid
- A warning card appears on the Bills screen when bills are due within 7 days
- System notifications fire at the due date via `AlarmManager` and `BroadcastReceiver`
- Long-press any bill to delete it

**Why it's useful:** Most expense trackers only record money already spent. Bill Reminders helps users *plan ahead* for upcoming expenses, preventing overspending surprises.

---

### 2. ⏳ Goal Countdown Timer
> *Unique feature designed and built by the team*

Users set a **custom target date** for their monthly spending goal. A live countdown card on the Dashboard shows exactly how many days remain to achieve the goal, with a dynamic status badge that changes colour based on spending behaviour.

**How it works:**
- Go to Hamburger Menu → Monthly Goals
- Set your min/max spending goals and pick a target date using the date picker
- The Dashboard immediately shows a countdown card: *"12 days left to reach your goal by 30 Jun"*
- The badge updates automatically:
  - ✅ **On track** (green) — spending is within the goal range
  - ⚠️ **Ending soon** (orange) — fewer than 3 days remaining
  - 🔴 **Over budget** (red) — spending has exceeded the maximum goal
- Past dates are blocked — target must always be in the future

**Why it's useful:** Goals without deadlines rarely get met. The countdown timer creates urgency and motivates consistent saving behaviour by making the deadline visible every time the user opens the app.

---

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Kotlin | 2.x | Primary language |
| Room Database | 2.8.4 | Local SQLite storage (Expenses, Users, Bills) |
| ViewModel + LiveData | 2.7.0 | MVVM architecture, lifecycle-aware data |
| Coroutines | 1.7.3 | Background threading for DB operations |
| RecyclerView | 1.3.2 | Expense and Bill list display |
| Material Components | 1.11.0 | UI components and DayNight theming |
| MPAndroidChart | 3.1.0 | Bar chart and Pie chart visualisations |
| Coil | 2.6.0 | Efficient image loading (prevents bitmap crashes) |
| DrawerLayout | 1.2.0 | Navigation Drawer / Hamburger menu |
| AlarmManager | Built-in | Bill reminder notification scheduling |
| FileProvider | Built-in | Secure camera file URI handling |
| SharedPreferences | Built-in | Goals, settings and achievements storage |
| ViewBinding | Built-in | Type-safe view access in Fragments |

---

## 🏗️ Architecture

```
MVVM (Model-View-ViewModel)
│
├── data/
│   ├── Expense.kt              → Room Entity (username, amount, category, date, photoPath)
│   ├── User.kt                 → Room Entity (username, password)
│   ├── ExpenseDao.kt           → Queries filtered by username
│   ├── UserDao.kt              → Login and register queries
│   └── AppDatabase.kt          → Singleton Room DB (version 5)
│
├── bills/
│   ├── Bill.kt                 → Room Entity (name, amount, dueDate, isRecurring)
│   ├── BillDao.kt              → Bill queries (all, unpaid, due soon)
│   ├── BillRepository.kt       → Data layer + recurring date advancement
│   ├── BillViewModel.kt        → LiveData + coroutines
│   ├── BillAdapter.kt          → RecyclerView adapter with DiffUtil
│   ├── BillsFragment.kt        → Bills list screen
│   ├── AddBillBottomSheet.kt   → Bottom sheet dialog to add bills
│   ├── BillScheduler.kt        → AlarmManager scheduling
│   ├── BillNotificationReceiver.kt → BroadcastReceiver for notifications
│   └── BootReceiver.kt         → Restores alarms after reboot
│
├── repository/
│   ├── ExpenseRepository.kt    → Expense data access (username-scoped)
│   └── AuthRepository.kt       → Login and register logic
│
├── viewmodel/
│   ├── ExpenseViewModel.kt     → Expense CRUD + search
│   └── AuthViewModel.kt        → Login/register with loading state
│
├── ui/
│   ├── DashboardFragment.kt    → Home screen (ViewBinding)
│   ├── AddExpenseFragment.kt   → Add expense with camera, gallery, date picker
│   ├── HistoryFragment.kt      → RecyclerView + real-time search
│   ├── SummaryFragment.kt      → Bar chart + Pie chart + period selector
│   ├── GoalsFragment.kt        → Min/max goals + target date picker
│   ├── SettingsFragment.kt     → Dark mode, notifications, about, privacy
│   ├── AchievementsFragment.kt → XP level + badge collection
│   ├── BillsFragment.kt        → Bill reminders list
│   └── ExpenseAdapter.kt       → ListAdapter with DiffUtil
│
├── GoalManager.kt              → SharedPreferences (per-account goals + countdown)
├── AchievementManager.kt       → XP system, levels, 13 badges
├── LoginActivity.kt
├── RegisterActivity.kt
├── SplashActivity.kt
└── MainActivity.kt             → Host: Drawer + BottomNav + FAB
```

---

## 🗺️ App Navigation

```
Launch → Splash (2s) → Login
                          ↓ no account
                       Register → auto-login
                          ↓
                       MainActivity
                       ├── Bottom Nav
                       │   ├── 🏠 Home (Dashboard)
                       │   ├── ➕ Add Expense
                       │   ├── 🕐 History
                       │   ├── 📊 Summary
                       │   └── 🔔 Bills ← Feature 1
                       │
                       └── Hamburger Drawer
                           ├── Monthly Goals ← Feature 2 (countdown)
                           ├── Achievements
                           ├── Settings
                           └── Logout
```

---

## 📋 Requirements Checklist

| Requirement | Implementation | Status |
|---|---|---|
| RecyclerView | Expense History + Bills list with DiffUtil adapter | ✅ |
| Bottom Navigation | 5 tabs: Home, Add, History, Summary, Bills | ✅ |
| Room Database | Expenses, Users and Bills (version 5) | ✅ |
| Login & Registration | Per-account username/password in RoomDB | ✅ |
| Photo attachment | Camera (FileProvider) + Gallery (persistent URI) | ✅ |
| Monthly Goals | Min/max targets with progress bar | ✅ |
| Date Picker | DatePickerDialog on Add Expense | ✅ |
| Search bar | Real-time filter by name, category or date | ✅ |
| Floating Action Button | Quick add from any screen, opens bill sheet on Bills tab | ✅ |
| Navigation Drawer | Hamburger menu with 4 drawer items | ✅ |
| Dashboard | Home screen with full spending overview | ✅ |
| Graph (bar chart) | Spending per category with min/max limit lines | ✅ |
| Graph (pie chart) | Budget used vs remaining, shows goal amounts | ✅ |
| Period selector | Week / Month / All time filter on Summary | ✅ |
| Visual goal format | Progress bar + colour-coded badge on Dashboard | ✅ |
| Gamification | 13 badges + XP points + 5 levels | ✅ |
| Dark Mode | Full DayNight theme, toggle in Settings | ✅ |
| App icon | Custom splash screen logo | ✅ |
| GitHub Actions CI | Auto build on every push to main | ✅ |
| Code comments | Added to all major files | ✅ |
| Logging (Log.d) | In ViewModels, Fragments, Activities | ✅ |
| APK | Available in GitHub Releases v2.0.0 | ✅ |
| **Own Feature 1** | **Bill Reminders with system notifications** | ✅ |
| **Own Feature 2** | **Goal Countdown Timer with target date** | ✅ |

---

## 🚀 How to Run

1. Clone the repository:
   ```
   git clone https://github.com/KaraboTDev/SmartBudget-Pro.git
   ```
2. Open in **Android Studio**
3. Let Gradle sync complete
4. Run on a physical device (Min SDK 24, Android 7.0+)
   > ⚠️ A real device is required for camera and notification features

---

## 👥 Team

| Name | Role |
|---|---|
| [Karabo Tshivhase](https://github.com/KaraboTDev) | Lead Developer |
| [Ompha Mudau](https://github.com/ThandiMDev) | UI Developer |
| [Mulweli Mudau](https://github.com/Peggy201) | UX Developer |

## 🎬 Demo Video

[![Watch the demo](https://img.shields.io/badge/Watch-Demo%20Video-red?logo=youtube)](https://youtube.com/shorts/ZayTo5rWpTQ?si=DTNAJemCaGEGVYx7)

> Click the badge above to watch the full app demonstration video.

---

## 📦 Download APK

[![Download APK](https://img.shields.io/badge/Download-APK%20v2.0.0-green?logo=android)](https://github.com/KaraboTDev/SmartBudget-Pro/releases/download/v2.0.0/app-debug.apk)

> Minimum Android 7.0 (API 24) required. Install by enabling **"Install from unknown sources"** in your device settings.


## 📁 Repository

[![GitHub](https://img.shields.io/badge/GitHub-SmartBudget--Pro-black?logo=github)](https://github.com/KaraboTDev/SmartBudget-Pro)

| Detail | Info |
|---|---|
| 🔗 Repo URL | https://github.com/KaraboTDev/SmartBudget-Pro |
| 🌿 Branch | main |
| 📝 Commits | See commit history for full development timeline |
| ⚙️ CI/CD | GitHub Actions — auto builds on every push to main |

> Clone the repo:
> ```
> git clone https://github.com/KaraboTDev/SmartBudget-Pro.git
> ```

---

## 📄 License

This project was built for educational purposes as part of a final year college submission.
