# SmartBudget Pro

A personal finance Android app built with Kotlin 

##  Features

- **Dashboard** — Overview of total spending, goals and recent expenses
- **Add Expenses** — Log expenses with amount, category, date and optional photo
- **Expense History** — View and search all past expenses
- **Summary** — Spending breakdown by category
- **Monthly Goals** — Set minimum and maximum spending targets
- **Multiple Accounts** — Switch between different user accounts
- **Dark Mode** — Full dark theme support
- **Photo Receipts** — Attach photos from camera or gallery

##  Tech Stack

| Technology | Purpose |
|---|---|
| Kotlin | Primary language |
| Room Database | Local data storage |
| ViewModel + LiveData | Architecture components |
| RecyclerView | Expense list display |
| Navigation Drawer | Hamburger menu |
| Material Design 3 | UI components |
| Coil | Image loading |
| SharedPreferences | Settings persistence |

##  Architecture
MVVM (Model-View-ViewModel)
├── data/          → Room entities, DAOs, Database
├── repository/    → Data access layer
├── viewmodel/     → Business logic, LiveData
└── ui/            → Fragments, Adapters

##  How to Run

1. Clone the repository
2. Open in Android Studio
3. Let Gradle sync
4. Run on emulator or physical device (Min SDK 24)

##  Requirements Met

-  RecyclerView — Expense History list
-  Bottom Navigation — Home, Add, History, Summary
-  Room Database — Expenses and Users
-  Login & Registration — Stored in RoomDB
-  Photo attachment — Camera and Gallery
-  Monthly Goals — Min and Max targets
-  Date Picker — Manual date selection
-  Search — Filter expenses by name, category or date
-  Floating Action Button — Quick add from any screen
-  Navigation Drawer — Hamburger menu

##  Downloads & Resources

- **GitHub Repository** — [https://github.com/KaraboTDev/SmartBudget-Pro](https://github.com/KaraboTDev/SmartBudget-Pro)
- **Apk**- https://github.com/KaraboTDev/SmartBudget-Pro/releases/download/v1.0/app-debug.apk
- **Demo Video** — [https://youtube.com/shorts/-Mg3bxlpSEI?si=EJ2KGQnnSxZjuTVg](https://youtube.com/shorts/-Mg3bxlpSEI?si=EJ2KGQnnSxZjuTVg)
