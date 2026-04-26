package com.example.budgetapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.budgetapp.viewmodel.AuthViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel
    private lateinit var tilUsername: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnGoToRegister: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        tilUsername = findViewById(R.id.tilUsername)
        tilPassword = findViewById(R.id.tilPassword)
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoToRegister = findViewById(R.id.btnGoToRegister)

        btnLogin.setOnClickListener {
            if (validateInputs()) {
                viewModel.login(
                    etUsername.text.toString(),
                    etPassword.text.toString()
                )
            }
        }

        btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Observe login result
        viewModel.loginResult.observe(this) { user ->
            if (user != null) {

                // Save logged in user to GoalManager
                //After a successful login, save the username to GoalManager so goals are linked to the right account
                val goalManager = GoalManager(this)
                goalManager.currentUser = user.username
                goalManager.addAccount(user.username)
                // Login successful - go to main app
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else if (etUsername.text?.isNotEmpty() == true) {
                // Only show error if the user actually tried to log in
                tilPassword.error = "Incorrect username or password"
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { loading ->
            btnLogin.isEnabled = !loading
            btnLogin.text = if (loading) "Logging in..." else "Log In"
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        tilUsername.error = null
        tilPassword.error = null

        if (etUsername.text.toString().trim().isEmpty()) {
            tilUsername.error = "Please enter your username"
            isValid = false
        }
        if (etPassword.text.toString().trim().isEmpty()) {
            tilPassword.error = "Please enter your password"
            isValid = false
        }
        return isValid
    }
}
