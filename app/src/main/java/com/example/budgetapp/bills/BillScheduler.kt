package com.example.budgetapp.bills

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

object BillScheduler {

    // Schedules a notification alarm for a bill's due date
    fun scheduleBill(context: Context, bill: Bill) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, BillNotificationReceiver::class.java).apply {
            putExtra("bill_name", bill.name)
            putExtra("bill_amount", bill.amount)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            bill.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule alarm at the bill's due date
        // Android 12 (API 31) and higher require checking for exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    bill.dueDate,
                    pendingIntent
                )
            } else {
                // Fallback to a non-exact alarm to avoid a crash if permission is missing
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    bill.dueDate,
                    pendingIntent
                )
                Log.w("BillScheduler", "Exact alarm permission missing. Scheduled non-exact alarm instead.")
            }
        } else {
            // For versions below Android 12, setExactAndAllowWhileIdle works without extra permission checks
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                bill.dueDate,
                pendingIntent
            )
        }

        Log.d("BillScheduler", "Scheduled alarm for bill: ${bill.name} at ${bill.dueDate}")
    }

    // Cancels an existing alarm
    fun cancelBill(context: Context, bill: Bill) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, BillNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            bill.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        Log.d("BillScheduler", "Cancelled alarm for bill: ${bill.name}")
    }
}
