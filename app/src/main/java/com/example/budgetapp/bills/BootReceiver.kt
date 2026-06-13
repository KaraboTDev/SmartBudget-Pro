package com.example.budgetapp.bills

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Phone rebooted — restoring bill alarms")
            // Alarms are restored via BillScheduler when bills are loaded
            // In a full implementation you'd reload all bills from DB here
        }
    }
}