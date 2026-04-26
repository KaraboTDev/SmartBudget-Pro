package com.example.budgetapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetapp.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // No layout needed — the theme window background IS the splash screen
        // Wait 2 seconds then go to MainActivity
        android.os.Handler(mainLooper).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // removes SplashActivity from back stack
        }, 2000) // 2000ms = 2 seconds
    }
}