package com.example.budgetapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.budgetapp.viewmodel.AuthViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel
    private lateinit var tilUsername: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var btnBackToLogin: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        tilUsername = findViewById(R.id.tilRegUsername)
        tilPassword = findViewById(R.id.tilRegPassword)
        tilConfirmPassword = findViewById(R.id.tilRegConfirmPassword)
        etUsername = findViewById(R.id.etRegUsername)
        etPassword = findViewById(R.id.etRegPassword)
        etConfirmPassword = findViewById(R.id.etRegConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        btnBackToLogin = findViewById(R.id.btnBackToLogin)

        btnRegister.setOnClickListener {
            if (validateInputs()) {
                viewModel.register(
                    etUsername.text.toString(),
                    etPassword.text.toString()
                )
            }
        }

        btnBackToLogin.setOnClickListener {
            finish() // goes back to LoginActivity
        }

        // Observe register result
        viewModel.registerResult.observe(this) { result ->
            if (result != null) {
                result.onSuccess {
                    Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                    // Auto-login after successful registration
                    viewModel.login(
                        etUsername.text.toString(),
                        etPassword.text.toString()
                    )
                }
                result.onFailure { error ->
                    tilUsername.error = error.message // "Username already taken"
                }
                // Clear the result so it doesn't trigger again on screen rotation
                viewModel.clearRegisterResult()
            }
        }

        // When auto-login succeeds after registration, go to main app
        viewModel.loginResult.observe(this) { user ->
            if (user != null) {

                val goalManager = GoalManager(this)
                goalManager.currentUser = user.username
                goalManager.currentUserId = user.id
                goalManager.addAccount(user.username)
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity() // clears both Register and Login from back stack
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { loading ->
            btnRegister.isEnabled = !loading
            btnRegister.text = if (loading) "Creating account..." else "Create Account"
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        tilUsername.error = null
        tilPassword.error = null
        tilConfirmPassword.error = null

        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirm  = etConfirmPassword.text.toString().trim()

        if (username.isEmpty()) {
            tilUsername.error = "Please choose a username"
            isValid = false
        } else if (username.length < 3) {
            tilUsername.error = "Username must be at least 3 characters"
            isValid = false
        }

        if (password.isEmpty()) {
            tilPassword.error = "Please choose a password"
            isValid = false
        } else if (password.length < 6) {
            tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (confirm.isEmpty()) {
            tilConfirmPassword.error = "Please confirm your password"
            isValid = false
        } else if (password != confirm) {
            tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }
}
